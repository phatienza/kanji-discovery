# Phase 1 Data Pipeline Design

## Scope

Phase 1 is an offline Java 17 Maven pipeline. It parses KANJIDIC2, legacy EUC-JP KRADFILE, and JMdict; selects KanjiVG SVG assets; writes `data/n5.json`; and validates the hand-curated N5 scene ladder. It does not create frontend or workbook code.

## Architecture

The root Maven aggregator initially contains one `pipeline` module. Source parsers return immutable records and parser statistics. Curation remains outside Java in UTF-8 TSV/JSON files: radical variants, explicit game recipes, and scene assignments. Generation joins source records with those curation files, while validation independently reads the emitted JSON and scene file so it tests the delivery contract rather than generator internals.

KANJIDIC2 legacy JLPT level 4 is the N5 target set because the supplied source contains 103 such kanji, matching the project specification's approximately 100-kanji scope. Scene support kanji may come from higher levels. Full normalized KRADFILE decompositions become `parts`; explicit curated combinations become `recipe`; found and irregular kanji use `recipe: null`.

## Error handling

XML uses StAX with external entities disabled and internal DTD entities enabled. Malformed records, missing required fields, invalid EUC-JP input, unknown curation references, duplicate scene assignments, unavailable recipes, missing words, and missing KanjiVG files fail the command with a non-zero status. Parsers report total, selected, and rejected counts; they never silently discard malformed records.

## Testing

Each parser has a minimal fixture suite, including an actual EUC-JP KRADFILE fixture. Tests are written before production classes and exercise valid data plus malformed input. Integration tests cover all five required decompositions, generator schema shape, scene dependency rules, duplicate/missing coverage, word availability, and the coverage report. Final verification runs the entire Maven test suite, generates `data/n5.json`, validates the real N5 scene ladder, and checks selected KanjiVG assets.
