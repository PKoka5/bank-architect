'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { test } = require('node:test');
const { buildCorpus, createResolver, exportContexts, extractLinks, main, parseRegistry } = require('./export-strategy-contexts.cjs');

const fact = (page, id, variant = page, defaultVersion = true) => ({ page_name: page, page_name_sub: variant,
  item_name: page, item_id: id == null ? [] : [String(id)], default_version: defaultVersion });
const equipment = (page, style, slots, extra = {}) => ({ page_name: page,
  json: JSON.stringify({ 'Recommended Equipment': slots, style, ...extra }) });
const registryText = '1\tFirst item\tGEAR\tFIRST\n2\tSecond item\tGEAR\tSECOND\n';
const registry = () => parseRegistry(registryText);

function cleanupTemporary(dir, prefix) {
  const tempRoot = path.resolve(os.tmpdir()), target = path.resolve(dir);
  const relative = path.relative(tempRoot, target);
  assert.ok(relative && relative !== '..' && !relative.startsWith('..' + path.sep) && !path.isAbsolute(relative));
  assert.equal(path.dirname(target), tempRoot);
  assert.ok(path.basename(target).startsWith(prefix));
  fs.rmSync(target, { recursive: true, force: true });
}

test('image and text links deduplicate within a cell without merging explicit variants', () => {
  const links = extractLinks('[[File:Bow.png|20px|link=Bow#Inactive]] [[Bow#Inactive|Uncharged bow]] / [[Bow|Charged bow]] [[File:No item.png|20px]]');
  assert.deepEqual(links.map(row => row.link), ['Bow#Inactive', 'Bow']);
  assert.deepEqual(links[0].sources, ['image', 'text']);
  assert.equal(links[0].fragment, 'Inactive');
  assert.equal(links[1].fragment, null);
});

test('resolver preserves explicit inactive version and never substitutes the default for an unknown fragment', () => {
  const resolve = createResolver([fact('Bow', 1, 'Bow#Charged'), fact('Bow', 2, 'Bow#Inactive', false)]);
  assert.deepEqual(resolve('Bow').ids, [1]);
  assert.equal(resolve('Bow').status, 'resolved-default');
  assert.deepEqual(resolve('Bow#Inactive').ids, [2]);
  assert.equal(resolve('Bow#Inactive').status, 'resolved-fragment');
  assert.deepEqual(resolve('Bow#Unknown').ids, []);
  assert.equal(resolve('Bow#Unknown').status, 'unknown-fragment');
  assert.equal(resolve('Bow#').status, 'unknown-fragment');
});

test('resolver keeps ambiguous pages and fragments unresolved rather than unioning IDs', () => {
  const resolve = createResolver([fact('Bow', 1, 'Bow#Charged'), fact('Bow', 2, 'Bow#Charged'),
    fact('Cape', 3, 'Cape#Trimmed', false), fact('Cape', 4, 'Cape#Untrimmed', false)]);
  for (const link of ['Bow', 'Bow#Charged', 'Cape']) {
    assert.equal(resolve(link).status, 'ambiguous');
    assert.deepEqual(resolve(link).ids, []);
    assert.equal(resolve(link).candidateIds.length, 2);
  }
});

test('a sole version may resolve without a default but an item display alias cannot invent an ID', () => {
  const row = fact('Canonical page', 1, 'Canonical page#Only', false);
  row.item_name = 'Display alias';
  const resolve = createResolver([row, fact('No ID', null)]);
  assert.equal(resolve('Canonical_page').status, 'resolved-single');
  assert.deepEqual(resolve('Canonical page').ids, [1]);
  assert.equal(resolve('Display alias').status, 'unknown-page');
  assert.equal(resolve('No ID').status, 'missing-item-id');
});

test('ranks are source cell positions and unclassified cell references preserve raw cells and conditions', () => {
  const cell = '[[First]] / [[File:Second.png|link=Second]][[Second|Other option]] (only with [[Crystal armour]])<ref name="set">Needs the whole set</ref><sup>[b]</sup>';
  const corpus = buildCorpus([equipment('Boss/Strategies', 'Ranged (Bowfa)',
    { head: ['[[First]]', cell, 'No helmet if unavailable'] }, { note: 'Use Crystal armour with Bowfa.' })],
  [fact('First', 1), fact('Second', 2)], registry());
  const references = corpus.recommendations.filter(row => row.rank === 2);
  assert.deepEqual(references.map(row => row.link), ['First', 'Second', 'Crystal armour']);
  assert.deepEqual(references.map(row => row.cellReference), [1, 2, 3]);
  for (const row of references) {
    assert.equal(row.linkRole, 'unclassified-cell-reference');
    assert.equal(row.rawCell, cell);
    assert.equal(row.reviewRequired, true);
    assert.match(row.conditionText, /only with/);
    assert.equal(row.footnotes.length, 2);
  }
  assert.equal(corpus.recommendations.at(-1).resolutionStatus, 'no-link');
  assert.equal(corpus.contexts[0].metadata.note, 'Use Crystal armour with Bowfa.');
  assert.equal(corpus.automaticAlchRules, false);
  assert.equal(corpus.automaticReplacementRules, false);
  assert.equal(corpus.summary.cells, 3);
});

test('gear linked inside conditions and references never becomes classified equipment or automatic proof', () => {
  const cell = '[[First]] (only with [[Crystal armour]])<ref>Compare [[Second]] for this method.</ref>';
  const corpus = buildCorpus([equipment('Boss', 'Conditional method', { body: [cell] })],
    [fact('First', 1), fact('Crystal armour', 2), fact('Second', 3)], registry());
  assert.deepEqual(corpus.recommendations.map(row => row.link), ['First', 'Crystal armour', 'Second']);
  for (const row of corpus.recommendations) {
    assert.equal(row.linkRole, 'unclassified-cell-reference');
    assert.equal(row.reviewRequired, true);
    assert.equal(row.conditionReviewRequired, true);
    assert.equal(Object.hasOwn(row, 'alternative'), false);
    assert.equal(row.rawCell, cell);
  }
  assert.equal(corpus.automaticAlchRules, false);
  assert.equal(corpus.automaticReplacementRules, false);
});

test('repeated page and style rows get independent deterministic occurrence contexts', () => {
  const rows = [equipment('Boss', 'Melee', { weapon: ['[[First]]'] }),
    equipment('Boss', 'Melee', { weapon: ['[[Second]]'] }),
    equipment('Boss', 'Ranged', { weapon: ['[[First]]'] })];
  const corpus = buildCorpus(rows, [fact('First', 1), fact('Second', 2)], registry());
  assert.equal(corpus.contexts.length, 3);
  assert.equal(corpus.summary.pages, 1);
  assert.equal(corpus.summary.pageModes, 2);
  assert.deepEqual(corpus.contexts.map(row => [row.style, row.occurrence]), [['Melee', 1], ['Melee', 2], ['Ranged', 1]]);
  assert.equal(new Set(corpus.contexts.map(row => row.contextId)).size, 3);
  assert.deepEqual(corpus, buildCorpus(rows, [fact('First', 1), fact('Second', 2)], registry()));
});

test('registry membership records absent exact IDs separately from unknown links', () => {
  const corpus = buildCorpus([equipment('Boss', '', { head: ['[[Third]] / [[Unknown]]'] })],
    [fact('Third', 3)], registry());
  assert.deepEqual(corpus.recommendations[0].registry, [{ id: 3, present: false }]);
  assert.equal(corpus.summary.resolvedItemIds, 1);
  assert.deepEqual(corpus.summary.registryMissingIds, [3]);
  assert.equal(corpus.summary.unresolvedLinks[0].link, 'Unknown');
});

test('optional full-page snapshots attach exact revisions and content hashes while retaining manual condition review', () => {
  const pages = [{ title: 'Boss/Strategies', pageid: 99,
    revisions: [{ revid: 12345, timestamp: '2026-10-22T10:00:00Z', slots: { main: { content: 'Full page with {{Note|Only with a set}}' } } }] }];
  const corpus = buildCorpus([equipment('Boss/Strategies', 'Solo', { body: ['[[First]]'] })],
    [fact('First', 1)], registry(), pages);
  assert.equal(corpus.contexts[0].sourcePage.revisionId, 12345);
  assert.match(corpus.contexts[0].sourcePage.contentSha256, /^[a-f0-9]{64}$/);
  assert.equal(corpus.contexts[0].sourcePage.conditionReview, 'source-captured-manual-review-required');
  assert.equal(corpus.recommendations[0].sourcePageRevisionId, 12345);
  assert.equal(corpus.recommendations[0].conditionReviewRequired, true);
  assert.equal(corpus.summary.pagesWithCapturedSource, 1);
  assert.equal(corpus.summary.contextsWithCapturedSource, 1);
});

test('invalid snapshots, malformed cells and duplicate registry rows fail clearly', () => {
  assert.throws(() => buildCorpus([], [fact('First', 1)], registry()), /nonempty arrays/);
  assert.throws(() => buildCorpus([{ page_name: 'Boss', json: '{broken' }], [fact('First', 1)], registry()), /source row 1/);
  assert.throws(() => buildCorpus([{ page_name: 'Boss', json: '{}' }], [fact('First', 1)], registry()), /Missing equipment/);
  assert.throws(() => buildCorpus([equipment('Boss', '', { head: [null] })], [fact('First', 1)], registry()), /Non-text/);
  assert.throws(() => parseRegistry(registryText + registryText), /duplicate/);
});

test('export writes deterministic local JSON and TSV, with hashes and review flags, without network', t => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'bank-strategy-contexts-'));
  t.after(() => cleanupTemporary(dir, 'bank-strategy-contexts-'));
  const recommendedPath = path.join(dir, 'recommended.json'), infoboxPath = path.join(dir, 'infobox.json');
  const registryPath = path.join(dir, 'registry.tsv');
  fs.writeFileSync(recommendedPath, '\uFEFF' + JSON.stringify([equipment('Boss', 'Melee',
    { weapon: ['[[First]] (only if ready)\n<ref>source</ref>'] })]));
  fs.writeFileSync(infoboxPath, JSON.stringify([fact('First', 1)]));
  fs.writeFileSync(registryPath, registryText);
  const options = { recommendedPath, infoboxPath, registryPath, outputDir: path.join(dir, 'output') };
  const previousFetch = globalThis.fetch;
  globalThis.fetch = () => { throw new Error('Network must never be used'); };
  t.after(() => { globalThis.fetch = previousFetch; });
  const corpus = exportContexts(options);
  const files = fs.readdirSync(options.outputDir).sort();
  const before = files.map(file => fs.readFileSync(path.join(options.outputDir, file), 'utf8'));
  assert.equal(files.length, 4);
  assert.match(corpus.sourceSnapshots.recommendedPath.sha256, /^[a-f0-9]{64}$/);
  assert.deepEqual(corpus, exportContexts(options));
  assert.deepEqual(before, files.map(file => fs.readFileSync(path.join(options.outputDir, file), 'utf8')));
  const source = fs.readFileSync(require.resolve('./export-strategy-contexts.cjs'), 'utf8');
  assert.deepEqual([...source.matchAll(/require\('([^']+)'\)/g)].map(match => match[1]), ['node:fs', 'node:path', 'node:crypto']);
  const tsv = fs.readFileSync(path.join(options.outputDir, 'strategy-contexts.tsv'), 'utf8');
  assert.match(tsv, /reviewRequired/);
  assert.match(tsv, /only if ready/);
});

test('missing local inputs fail before creating any output and CLI rejects malformed flags', t => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'bank-strategy-missing-'));
  t.after(() => cleanupTemporary(dir, 'bank-strategy-missing-'));
  const outputDir = path.join(dir, 'output');
  assert.throws(() => exportContexts({ recommendedPath: path.join(dir, 'absent.json'), outputDir }), /Missing local input recommendedPath/);
  assert.equal(fs.existsSync(outputDir), false);
  assert.throws(() => main(['--unknown', 'value']), /Invalid option/);
  assert.throws(() => main(['--output-dir']), /Invalid option/);
});
