// Java 11 literals must survive comment removal, including URLs and escaped quotes.
function normalize(source, collapseWhitespace = false) {
  const parts = source.replace(/\r\n/g, '\n').match(
    /"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\/[^\n]*|\/\*[\s\S]*?\*\/|\s+|[^\s"'/]+|./g
  ) || [];
  return parts.map(part => {
    if (part.startsWith('//') || part.startsWith('/*')) return ' ';
    return collapseWhitespace && /^\s+$/.test(part) ? ' ' : part;
  }).join('');
}

module.exports = { normalize };
