const { test } = require('node:test');
const assert = require('node:assert/strict');
const { normalize } = require('./normalize.cjs');

test('removes comments but preserves literals and their whitespace', () => {
  const source = '/* docs */ String s = "https://host/a  b/*x*/\\\""; // note\r\nchar c = \'/\';';
  const expected = '  String s = "https://host/a  b/*x*/\\\"";  \nchar c = \'/\';';
  assert.equal(normalize(source), expected);
  assert.ok(normalize(source, true).includes('"https://host/a  b/*x*/\\\""'));
  assert.ok(!normalize(source, true).includes('\n'));
});

test('comment removal keeps neighboring tokens separate', () => {
  assert.equal(normalize('int/* note */count;'), 'int count;');
  assert.equal(normalize(''), '');
});
