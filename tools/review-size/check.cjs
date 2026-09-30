const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const { normalize } = require('./normalize.cjs');

const root = path.resolve(__dirname, '../..');
const git = args => execFileSync('git', args, { cwd: root, encoding: 'utf8', maxBuffer: 32 * 1024 * 1024 });
const [baseline = 'def1e856e101ff0e57dd96adccab2cf1d886b076', reportedText = '200414'] = process.argv.slice(2);
const reported = Number(reportedText);
if (!Number.isSafeInteger(reported) || reported <= 0) throw Error('Reported count must be a positive integer.');
const files = text => text.trim().split('\n').filter(Boolean);
const beforeFiles = files(git(['ls-tree', '-r', '--name-only', baseline, '--', 'src/main/java']))
  .filter(file => file.endsWith('.java'));
const afterFiles = [...new Set(files(git(['ls-files', '--cached', '--others', '--exclude-standard', '--', 'src/main/java'])))]
  .filter(file => file.endsWith('.java') && fs.existsSync(path.join(root, file)));
const before = beforeFiles.map(file => git(['show', `${baseline}:${file}`])).join('\n');
const after = afterFiles.sort().map(file => fs.readFileSync(path.join(root, file), 'utf8')).join('\n');
console.log(`Baseline: ${baseline}, maintainer-reported count: ${reported}`);
console.log('ESTIMATES ONLY: main Java source, comments removed. Hub tokenizer and preprocessing are unknown.');
const rows = [];
for (const encoding of ['cl100k_base', 'o200k_base']) {
  const { encode } = require(`gpt-tokenizer/encoding/${encoding}`);
  for (const compact of [false, true]) {
    const baselineSourceTokens = encode(normalize(before, compact)).length;
    const currentSourceTokens = encode(normalize(after, compact)).length;
    const delta = currentSourceTokens - baselineSourceTokens;
    rows.push({ encoding, whitespace: compact ? 'collapsed outside literals' : 'preserved',
      baselineSourceTokens, currentSourceTokens, sourceDelta: delta,
      estimatedTotal: reported + delta, estimatedHeadroom: 200000 - reported - delta });
  }
}
console.table(rows);
const otherChanges = files(git(['diff', '--name-only', baseline, '--', '.', ':!src/main/java']));
const untracked = files(git(['ls-files', '--others', '--exclude-standard', '--', '.', ':!src/main/java']));
if (otherChanges.length || untracked.length) {
  console.log('Not included in these estimates (tests, resources, tooling and other files):');
  console.log([...new Set([...otherChanges, ...untracked])].sort().join('\n'));
}
console.log('Use the highest estimate for planning; confirm the official count before claiming Hub eligibility.');
