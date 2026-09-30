# Review-size estimate

Development tool only; nothing here is bundled with or executed by the plugin.

```powershell
npm ci --prefix tools/review-size --ignore-scripts
npm test --prefix tools/review-size
npm run check --prefix tools/review-size
```

Compares the working tree's main Java sources against `def1e85`, for which a Hub
maintainer reported **200,414 tokens** on 2026-09-30. Includes new Java files and
deletions, removes comments without stripping strings, and measures the difference
using two tokenizers with preserved and collapsed whitespace. No source is sent
over the network. Dependency installation requires network access once.

The reported total plus that difference is an **estimate**, not RuneLite's count:
their tokenizer, preprocessing and input scope are unknown. Changes to resources,
tests and other files are listed but not counted. Inspect those separately. The
normalizer handles Java 11 string/character literals; Unicode-escaped comment
delimiters are not decoded. Keep several thousand tokens of headroom and use the
largest estimate when planning changes. A passed build is not a token-limit check.

After a maintainer supplies a count for another revision, pass both values:

```powershell
npm run check --prefix tools/review-size -- <source-commit> <reported-count>
```

The tool deliberately does not certify or gate a release on an estimated total.
