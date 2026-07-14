# Kanji Discovery

Kanji Discovery is a kanji learning project centered on finding, combining, and disassembling real kanji components. This repository currently implements **Phase 1 only**: the offline Java data pipeline and N5 scene validator.

## Phase 1 prerequisites

- Java 17 or newer
- Maven 3.9 or newer
- `curl`, `gzip`, and `unzip` for source acquisition

## Source data

Raw upstream data is intentionally excluded from version control. Download the checksum-pinned snapshots:

```bash
./scripts/download-sources.sh
```

This installs:

- `data/raw/kanjidic2.xml`
- `data/raw/kradfile` in its original EUC-JP encoding
- `data/raw/JMdict_e`
- `data/raw/kanjivg/*.svg`

## Build and test

```bash
mvn test
```

Generate `data/n5.json`, select the per-world KanjiVG SVGs, and validate the N5 scene ladder:

```bash
mvn -pl pipeline exec:java -Dexec.args="generate ."
```

Validate an existing generated file without regenerating it:

```bash
mvn -pl pipeline exec:java -Dexec.args="validate ."
```

## Curation inputs

- `curation/radical-variants.tsv` normalizes component variants and KRADFILE placeholders. Rows marked `TODO` require owner review.
- `curation/n5-recipes.tsv` keeps game recipes separate from factual KRADFILE `parts`.
- `scenes/n5-scenes.json` is the hand-curated game overlay.
- `scenes/n5-scenes.review.md` lists every scene assignment currently considered uncertain.

## Generated outputs

- `data/n5.json`: 103 target N5 kanji plus scene support kanji, approximately 200 common components, and common JMdict-validated words.
- `app/public/assets/kanjivg/n5/`: per-kanji SVGs required by the future Phase 2 web app. No frontend code is part of Phase 1.

## Attribution

This project uses KANJIDIC2, KRADFILE, and JMdict from the [Electronic Dictionary Research and Development Group](https://www.edrdg.org/), and SVG stroke data from [KanjiVG](https://kanjivg.tagaini.net/). See `THIRD_PARTY_NOTICES.md` for license details. Retain attribution when redistributing generated JSON, worksheets, or application assets.
