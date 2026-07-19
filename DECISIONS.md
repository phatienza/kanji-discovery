# Decisions

- 2026-07-15: Use `dev.paulatienza.kanjidiscovery` as the Maven group and Java package prefix, as approved by the project owner.
- 2026-07-15: Pin exact SHA-256 hashes for the mutable EDRDG daily distributions and fail on drift; upstream does not publish SHA-256 sidecars, so accepting a refreshed snapshot must be an explicit maintainer action.
- 2026-07-15: Use KanjiVG `r20250816`'s `main` archive because it is the upstream-recommended per-kanji non-variant SVG set and retains component/stroke attributes required by the project.
- 2026-07-15: Map KANJIDIC2 legacy JLPT level 4 to the N5 target set for the MVP; the supplied source contains 103 such kanji, matching the specification's approximately 100-kanji World 1 scope.
- 2026-07-15: Keep parser/generator/validator in one Maven module while preserving JSON as the only delivery contract; split a shared Java module only if the Phase 3 workbook demonstrates a real need.
- 2026-07-15: Parse JMdict with internal DTD support but without replacing entity references; the pipeline does not consume POS/tag entity values, this avoids the JDK expansion limit, and external entities remain disabled.
- 2026-07-15: Treat 東 as found/irregular in the N5 draft instead of crafting 日 + 木 because that visual split is not a trustworthy etymology; preserve the scene-2 narrative payoff through 日本.
- 2026-07-15: Rank common components by normalized KRADFILE occurrence across KANJIDIC2 grade 1-8 kanji, take the top 200, then union every part and recipe dependency used by World 1; the current output is 204 components.
- 2026-07-15: Preserve source legacy levels on support kanji with the explicit mapping 3 to N4, 2 to N2, and 1 to N1; do not invent an N3 classification from a source that predates the modern five-level JLPT.
- 2026-07-15: Use `main` for reviewed milestones and `develop` for incoming work so the solo maintainer can practice pull-request review before merging.
- 2026-07-15: License original project code and documentation under MIT while retaining the applicable EDRDG CC BY-SA 4.0 and KanjiVG CC BY-SA 3.0 terms for derived data and copied assets.
- 2026-07-19: Keep factual component shapes distinct from crafting semantics: retain `儿` as legs, normalize KRADFILE `杰` to `灬` and `込` to `辶`, use `网` for the net radical, and reserve semantic substitutions such as `火` for explicitly curated recipes.
