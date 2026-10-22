'use strict';

// Development-only: reads local Wiki snapshots. Source rank is never an Alch or replacement rule.
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');

const compare = (a, b) => a < b ? -1 : a > b ? 1 : 0;
const unique = values => [...new Set(values)];
const decode = value => String(value).replace(/&(?:amp|quot|apos|nbsp|#\d+|#x[0-9a-f]+);/gi, entity => {
  const named = { '&amp;': '&', '&quot;': '"', '&apos;': "'", '&nbsp;': ' ' };
  const key = entity.toLowerCase();
  if (Object.hasOwn(named, key)) return named[key];
  const code = key.startsWith('&#x') ? parseInt(key.slice(3, -1), 16) : parseInt(key.slice(2, -1), 10);
  return code >= 0 && code <= 0x10ffff ? String.fromCodePoint(code) : entity;
});
const titleKey = value => decode(value).replace(/_/g, ' ').trim().replace(/\s+/g, ' ').toLowerCase();

function linkParts(link) {
  let title = decode(link).trim().replace(/^:/, '');
  try { title = decodeURIComponent(title); } catch { /* Preserve damaged targets for review. */ }
  const marker = title.indexOf('#');
  return { title: marker < 0 ? title : title.slice(0, marker),
    fragment: marker < 0 ? null : title.slice(marker + 1),
    explicitFragment: marker >= 0, key: titleKey(title) };
}

function extractLinks(cell) {
  const links = new Map();
  for (const match of cell.matchAll(/\[\[([^\[\]]+)\]\]/g)) {
    const fields = match[1].split('|');
    const image = /^(?:file|image)\s*:/i.test(fields[0].trim());
    const option = image ? fields.slice(1).find(field => /^\s*link\s*=/i.test(field)) : null;
    const target = image ? option?.replace(/^\s*link\s*=/i, '').trim() : fields[0].trim();
    if (!target) continue;
    const parts = linkParts(target);
    if (!links.has(parts.key)) links.set(parts.key, { link: target, ...parts, sources: [] });
    const source = image ? 'image' : 'text';
    if (!links.get(parts.key).sources.includes(source)) links.get(parts.key).sources.push(source);
  }
  return [...links.values()];
}

function createResolver(infoboxRows) {
  const pages = new Map(), variants = new Map();
  const add = (index, key, fact) => {
    if (!index.has(key)) index.set(key, []);
    if (!index.get(key).some(prior => JSON.stringify(prior) === JSON.stringify(fact))) index.get(key).push(fact);
  };
  for (const row of infoboxRows) {
    if (!row || typeof row.page_name !== 'string') throw new Error('Invalid infobox row: missing page_name');
    const rawIds = Array.isArray(row.item_id) ? row.item_id : [row.item_id];
    const ids = unique(rawIds.filter(value => /^\d+$/.test(String(value)) && Number(value) > 0)
      .map(Number)).sort((a, b) => a - b);
    const fact = { page: row.page_name, variant: row.page_name_sub || row.page_name,
      itemName: row.item_name || '', ids,
      defaultVersion: row.default_version === true || row.default_version === 1 || row.default_version === '1' };
    add(pages, titleKey(fact.page), fact);
    add(variants, titleKey(fact.variant), fact);
  }
  return link => {
    const parts = linkParts(link);
    let facts = (parts.explicitFragment ? variants.get(parts.key) : pages.get(titleKey(parts.title))) || [];
    const allFacts = facts;
    if (!parts.explicitFragment && facts.length > 1) {
      const defaults = facts.filter(fact => fact.defaultVersion);
      if (defaults.length) facts = defaults;
    }
    const candidateIds = unique(allFacts.flatMap(fact => fact.ids)).sort((a, b) => a - b);
    let status = 'unknown-page';
    if (parts.explicitFragment) status = 'unknown-fragment';
    if (facts.length > 1) status = 'ambiguous';
    if (facts.length === 1) status = facts[0].ids.length
      ? parts.explicitFragment ? 'resolved-fragment' : facts[0].defaultVersion ? 'resolved-default' : 'resolved-single'
      : 'missing-item-id';
    return { status, ids: status.startsWith('resolved-') ? facts[0].ids : [], candidateIds,
      facts: facts.slice().sort((a, b) => compare(a.variant, b.variant) || compare(a.ids.join(','), b.ids.join(','))) };
  };
}

function parseRegistry(text) {
  const registry = new Map();
  for (const line of text.replace(/^\uFEFF/, '').split(/\r?\n/)) {
    if (!line || line.startsWith('#')) continue;
    const [id, name, category, constantName] = line.split('\t');
    if (!/^\d+$/.test(id) || !name || !category || registry.has(Number(id)))
      throw new Error(`Invalid or duplicate registry row: ${line}`);
    registry.set(Number(id), { name, category, constantName: constantName || '' });
  }
  if (!registry.size) throw new Error('Empty item registry');
  return registry;
}

function buildCorpus(recommendedRows, infoboxRows, registry, sourcePages = []) {
  if (!Array.isArray(recommendedRows) || !recommendedRows.length || !Array.isArray(infoboxRows) || !infoboxRows.length)
    throw new Error('Both Wiki snapshots must be nonempty arrays');
  if (!Array.isArray(sourcePages)) throw new Error('Source pages must be a local MediaWiki page array');
  const pagesByTitle = new Map();
  for (const page of sourcePages) {
    const revision = page.revisions?.[0], content = revision?.slots?.main?.content;
    if (typeof page.title !== 'string') throw new Error('Source page is missing its title');
    const available = Number.isInteger(revision?.revid) && typeof content === 'string';
    pagesByTitle.set(titleKey(page.title), { title: page.title, pageId: page.pageid ?? null,
      revisionId: revision?.revid ?? null, timestamp: revision?.timestamp ?? null,
      contentSha256: available ? crypto.createHash('sha256').update(content).digest('hex') : null,
      conditionReview: available ? 'source-captured-manual-review-required' : 'source-unavailable-review-required' });
  }
  const resolve = createResolver(infoboxRows);
  const occurrences = new Map(), contexts = [], recommendations = [];
  let cells = 0;
  for (const [sourceIndex, row] of recommendedRows.entries()) {
    if (!row || typeof row.page_name !== 'string') throw new Error(`Invalid recommendation row ${sourceIndex + 1}`);
    let data;
    try { data = typeof row.json === 'string' ? JSON.parse(row.json) : row.json; }
    catch { throw new Error(`Invalid recommendation JSON at source row ${sourceIndex + 1}: ${row.page_name}`); }
    if (!data || !data['Recommended Equipment'] || typeof data['Recommended Equipment'] !== 'object'
      || Array.isArray(data['Recommended Equipment'])) throw new Error(`Missing equipment at source row ${sourceIndex + 1}`);
    const page = row.page_name, style = String(data.style || '');
    const occurrenceKey = JSON.stringify([page, style]);
    const occurrence = (occurrences.get(occurrenceKey) || 0) + 1;
    occurrences.set(occurrenceKey, occurrence);
    const contextId = JSON.stringify([page, style, occurrence]);
    const metadata = Object.fromEntries(Object.entries(data).filter(([key]) => key !== 'Recommended Equipment'));
    const sourcePage = pagesByTitle.get(titleKey(page)) || null;
    const context = { contextId, page, style, occurrence, sourceRowIndex: sourceIndex + 1,
      reviewRequired: true, metadata, sourcePage, conditionReviewRequired: true,
      cells: 0, links: 0, resolvedLinks: 0, ids: [] };
    const ids = new Set();
    for (const slot of Object.keys(data['Recommended Equipment']).sort(compare)) {
      const value = data['Recommended Equipment'][slot];
      const ranks = Array.isArray(value) ? value : [value];
      for (const [rankIndex, raw] of ranks.entries()) {
        if (typeof raw !== 'string') throw new Error(`Non-text equipment cell in ${contextId}/${slot}/${rankIndex + 1}`);
        cells++; context.cells++;
        const links = extractLinks(raw);
        const footnotes = [...raw.matchAll(/<ref\b[^>]*(?:\/>|>[\s\S]*?<\/ref\s*>)|<sup\b[\s\S]*?<\/sup\s*>/gi)]
          .map(match => match[0]);
        const conditionText = decode(raw.replace(/\[\[[^\[\]]+\]\]/g, '').replace(/<[^>]*>/g, ' '))
          .replace(/\s+/g, ' ').trim();
        for (const [referenceIndex, link] of (links.length ? links : [null]).entries()) {
          const resolution = link ? resolve(link.link) : { status: 'no-link', ids: [], candidateIds: [], facts: [] };
          context.links += link ? 1 : 0;
          context.resolvedLinks += resolution.ids.length ? 1 : 0;
          resolution.ids.forEach(id => ids.add(id));
          recommendations.push({ contextId, page, style, occurrence, sourceRowIndex: sourceIndex + 1,
            slot, rank: rankIndex + 1, cellReference: link ? referenceIndex + 1 : null,
            linkRole: link ? 'unclassified-cell-reference' : null,
            link: link?.link || '', title: link?.title || '', fragment: link?.fragment ?? null,
            linkSources: link?.sources || [], resolutionStatus: resolution.status,
            resolvedIds: resolution.ids, candidateIds: resolution.candidateIds,
            registry: resolution.ids.map(id => ({ id, present: registry.has(id), ...(registry.get(id) || {}) })),
            variantFacts: resolution.facts, footnotes, conditionText, rawCell: raw,
            sourcePageRevisionId: sourcePage?.revisionId ?? null,
            sourcePageContentSha256: sourcePage?.contentSha256 ?? null,
            conditionReviewRequired: true, reviewRequired: true });
        }
      }
    }
    context.ids = [...ids].sort((a, b) => a - b);
    contexts.push(context);
  }
  contexts.sort((a, b) => compare(a.page, b.page) || compare(a.style, b.style) || a.occurrence - b.occurrence);
  recommendations.sort((a, b) => compare(a.page, b.page) || compare(a.style, b.style)
    || a.occurrence - b.occurrence || compare(a.slot, b.slot) || a.rank - b.rank || a.cellReference - b.cellReference);
  const ids = unique(recommendations.flatMap(row => row.resolvedIds)).sort((a, b) => a - b);
  const statuses = {}, unresolved = new Map(), activities = new Map();
  for (const row of recommendations) {
    statuses[row.resolutionStatus] = (statuses[row.resolutionStatus] || 0) + 1;
    if (row.link && !row.resolvedIds.length) unresolved.set(row.link, (unresolved.get(row.link) || 0) + 1);
  }
  for (const context of contexts) activities.set(context.page, (activities.get(context.page) || 0) + 1);
  const summary = { sourceRows: recommendedRows.length, contexts: contexts.length, cells,
    pages: activities.size, pageModes: occurrences.size, styles: new Set(contexts.map(context => context.style)).size,
    pagesWithCapturedSource: unique(contexts.filter(context => context.sourcePage?.contentSha256).map(context => context.page)).length,
    contextsWithCapturedSource: contexts.filter(context => context.sourcePage?.contentSha256).length,
    linkOccurrences: recommendations.filter(row => row.link).length,
    resolvedItemIds: ids.length, registryMatchedIds: ids.filter(id => registry.has(id)).length,
    registryMissingIds: ids.filter(id => !registry.has(id)), resolutionStatuses: statuses,
    unresolvedLinks: [...unresolved].map(([link, count]) => ({ link, count }))
      .sort((a, b) => b.count - a.count || compare(a.link, b.link)),
    topActivities: [...activities].map(([page, contexts]) => ({ page, contexts }))
      .sort((a, b) => b.contexts - a.contexts || compare(a.page, b.page)).slice(0, 30) };
  return { schemaVersion: 1, reviewRequired: true,
    rankMeaning: 'Source cell position within this page, method and slot; never an automatic replacement or Alch rule.',
    automaticAlchRules: false, automaticReplacementRules: false, summary, contexts, recommendations };
}

function tsv(rows, columns) {
  const cell = value => {
    const text = value == null ? '' : typeof value === 'object' ? JSON.stringify(value) : String(value);
    return /[\t\r\n"]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  };
  return [columns.join('\t'), ...rows.map(row => columns.map(column => cell(row[column])).join('\t'))].join('\n') + '\n';
}

function exportContexts(options = {}) {
  const defaults = { recommendedPath: path.join(__dirname, 'cache/recommended-equipment.bucket.json'),
    infoboxPath: path.join(__dirname, 'cache/infobox-item.bucket.json'),
    registryPath: path.resolve(__dirname, '../../../src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv'),
    outputDir: path.join(__dirname, 'cache/contexts') };
  const settings = { ...defaults, ...options }, sourceSnapshots = {};
  const read = key => {
    const file = settings[key];
    if (!fs.existsSync(file)) throw new Error(`Missing local input ${key}: ${file}`);
    const bytes = fs.readFileSync(file);
    sourceSnapshots[key] = { file: path.basename(file), sha256: crypto.createHash('sha256').update(bytes).digest('hex') };
    return bytes.toString('utf8').replace(/^\uFEFF/, '');
  };
  const recommended = JSON.parse(read('recommendedPath')), infobox = JSON.parse(read('infoboxPath'));
  const registry = parseRegistry(read('registryPath'));
  const sourcePages = settings.sourcePagesPath ? JSON.parse(read('sourcePagesPath')) : [];
  const corpus = buildCorpus(recommended, infobox, registry, sourcePages);
  corpus.sourceSnapshots = sourceSnapshots;
  fs.mkdirSync(settings.outputDir, { recursive: true });
  const writeJson = (name, data) => fs.writeFileSync(path.join(settings.outputDir, name), JSON.stringify(data, null, 2) + '\n');
  writeJson('strategy-contexts.json', corpus);
  fs.writeFileSync(path.join(settings.outputDir, 'strategy-contexts.tsv'), tsv(corpus.recommendations,
    ['contextId', 'page', 'style', 'occurrence', 'sourceRowIndex', 'slot', 'rank', 'cellReference', 'linkRole', 'link', 'title',
      'fragment', 'linkSources', 'resolutionStatus', 'resolvedIds', 'candidateIds', 'registry', 'variantFacts',
      'footnotes', 'conditionText', 'rawCell', 'sourcePageRevisionId', 'sourcePageContentSha256',
      'conditionReviewRequired', 'reviewRequired']));
  writeJson('context-inventory.json', { schemaVersion: 1, reviewRequired: true, sourceSnapshots,
    summary: corpus.summary, contexts: corpus.contexts });
  fs.writeFileSync(path.join(settings.outputDir, 'context-inventory.tsv'), tsv(corpus.contexts,
    ['contextId', 'page', 'style', 'occurrence', 'sourceRowIndex', 'cells', 'links', 'resolvedLinks', 'ids',
      'sourcePage', 'conditionReviewRequired', 'reviewRequired']));
  return corpus;
}

function main(args) {
  const flags = { '--recommended': 'recommendedPath', '--infobox': 'infoboxPath', '--registry': 'registryPath',
    '--pages': 'sourcePagesPath', '--output': 'outputDir', '--output-dir': 'outputDir' }, options = {};
  if (args.includes('--help')) {
    console.log('Usage: node export-strategy-contexts.cjs [--recommended local.json] [--infobox local.json] [--registry local.tsv] [--pages source-pages.json] [--output-dir directory]');
    return;
  }
  for (let index = 0; index < args.length; index++) {
    const key = flags[args[index]];
    if (!key || !args[index + 1] || args[index + 1].startsWith('--')) throw new Error(`Invalid option: ${args[index]}`);
    options[key] = args[++index];
  }
  console.log(JSON.stringify(exportContexts(options).summary, null, 2));
}

module.exports = { buildCorpus, createResolver, exportContexts, extractLinks, main, parseRegistry };
if (require.main === module) {
  try { main(process.argv.slice(2)); } catch (error) { console.error(error.message); process.exitCode = 1; }
}
