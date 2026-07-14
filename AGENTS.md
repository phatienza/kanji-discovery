# AGENTS.md — Kanji Discovery Project

Project context and instructions for AI coding agents (Codex / Codex).
Read this fully before writing any code.

## What we are building

A kanji learning tool with a "discovery game" mechanic, inspired by Infinite Craft
but grounded in real etymology:

- **Combine**: tap/drag two components (radicals or kanji) to form a real kanji
  (日 + 月 = 明) or a real word (明 + 日 = 明日).
- **Disassemble**: tap any kanji to break it into its components, with meanings.
- **Workbook**: generate printable PDF decomposition exercises (the paid product).

The emotional core is *discovery, not memorization*. 日+月=明 ("sun + moon =
bright") should feel like the user figured it out themselves.

## Who is building it

Solo developer. Strong in **Java** and experienced with **TypeScript and
React**. Implications for agents:

- Java code: assume fluency. Idiomatic, standard-library-first, no hand-holding.
- Frontend: React + TypeScript, strict mode. Assume competence — idiomatic
  hooks, no hand-holding comments needed. Keep the dependency list lean and
  justified; the animation stack below is the notable exception.

## Architecture (fixed decision — do not redesign)

Three components, split by language strength:

### 1. Data pipeline (Java, runs offline on dev machine)
- Parses open datasets and emits a single static `kanji-data.json`.
- No server. Run manually when data needs regenerating.
- Sources:
  - **KRADFILE / RADKFILE** — kanji ↔ component/radical mappings.
    NOTE: legacy files are **EUC-JP encoded** — handle encoding explicitly,
    never assume UTF-8. (A UTF-8 conversion, kradfile-u, also exists.)
  - **JMdict** (XML) — validates real words, provides readings + glosses.
  - **KanjiVG** (SVG) — stroke paths + component structure. Needed by Phase 2
    for the stroke-draw animation: the pipeline should copy per-kanji SVGs for
    the target level into the app's assets (per-world folders).
- Licensing: KRADFILE/RADKFILE/JMdict are EDRDG licensed (CC BY-SA);
  KanjiVG is CC BY-SA. Attribution is REQUIRED in the app footer and README.
- Filter scope for v1: Jōyō kanji only, prioritized by JLPT level (N5 → N1).
  Components: the ~200 most common radicals/components.

### 2. Web app (React + TypeScript, static site — no backend)
- **Platform decision (fixed): mobile-first PWA, not a native app.**
  Ship as a static web app with a manifest + service worker (offline caching
  of static assets + per-world JSON, Add-to-Home-Screen). Native wrap via
  Capacitor is a possible v3 — same codebase — do not plan for it now.
- Stack: React + TypeScript + Vite, **Framer Motion (`motion`)** for drag
  gestures, springs, and transform choreography. Still zero backend.
- Deploy target: GitHub Pages or Cloudflare Pages. Zero hosting cost is a hard
  constraint — no servers, no databases, no accounts in v1.
- All game state in memory / localStorage. (localStorage is fine here — this
  is a deployed static site, not a Codex.ai artifact.)
- **Core interaction (the product's signature): illustration → kanji →
  illustration.** Primitives appear as illustrations in the scene (the sun,
  the moon). Grabbing one transforms it into its kanji (日, 月) — for
  pictographic primitives this transformation IS the real etymology, treat it
  with care. Combining transforms the result: strokes draw in (KanjiVG paths +
  stroke-dashoffset animation), then bloom into a result illustration where
  the concept is concrete.
- **Transform implementation: choreographed crossfade + stroke-draw, NOT true
  SVG path morphing.** Path morphing (e.g. Flubber) is a polish experiment for
  1–2 hero moments at most, never a v1 requirement.
- **Drag is a core mechanic** (grab-and-combine), with tap-to-select as an
  equal, always-working fallback. Generous hit areas; test with thumbs on a
  real phone. Portrait layout. Test iOS Safari early — its PWA support has
  more quirks than Android.

#### Illustration asset strategy (tiered — the art budget is the bottleneck)
- **Tier 1** (~20–30 pictographic primitives: 日 月 木 山 川 人 火 水 雨 田
  口 目 手 ...): bespoke flat SVG illustration + grab-transform + stroke-draw.
  These coincide with "found" scene items, so effort lands on the hero moments.
- **Tier 2** (crafted kanji with concrete meanings: 森, 炎, 林...): stroke-draw
  + small result illustration. Abstract results (好, 明 as "bright") get a
  styled tile + glow effect — never force literal illustrations of abstractions.
- **Tier 3** (everything else): clean kanji tiles with spring motion only.
- All illustrations: one consistent flat style, SVG, committed to the repo.
  A missing illustration must gracefully fall back to the Tier 3 tile —
  never block a scene on art.

#### Asset production workflow (offline — never at runtime)
AI image generation is an asset-production step only. It is never part of the
app runtime, the Java pipeline, or any build step. The repo receives finished,
cleaned SVGs.

1. **Generate** raster concepts with Google's image models — via the Flow UI /
   AI Studio for small batches, or the official Gemini API (Imagen / Nano
   Banana) if scripting iteration is genuinely needed. Do NOT use third-party
   "Flow API" wrappers (unofficial account-automation services).
   Style consistency: generate 3–4 approved anchors first, then pass them as
   reference images (Nano Banana Pro multi-reference) for all remaining assets.
   Base prompt direction: "flat minimal vector-style illustration, 2–3 solid
   colors, no gradients, no outlines, simple geometric shapes, white
   background, children's picture-book style".
2. **Vectorize** — auto-trace (vtracer / Inkscape / Illustrator trace), or a
   natively-SVG generator (e.g. Recraft) if it holds the style.
3. **Clean** in Figma/Inkscape: simplify paths, snap colors to the app
   palette, and name/group layers for animation (illustrations are animation
   subjects — the grab-transform needs sensible groups and anchor points).
4. **Commit** the final SVG. Keep generation prompts + reference images in
   `art/prompts/` so the style can be reproduced for future scenes/worlds.

Licensing note: generated images are commercially usable on paid Google
tiers and carry an invisible SynthID watermark (harmless for this use).
Re-verify terms if the provider or plan changes.

### 3. Workbook generator (Java + Apache PDFBox)
- Reads `kanji-data.json`, emits printable PDF worksheets:
  - "Build the kanji from parts" exercises
  - "Break the kanji apart" exercises
  - Answer keys
- Parameterized by JLPT level / kanji set.
- This is the revenue product. Print quality matters: embed a proper Japanese
  font (e.g. Noto Sans JP), A4 layout, generous stroke-practice boxes.

## kanji-data.json schema (v1)

```json
{
  "components": [
    { "char": "日", "meaning": "sun, day", "on": "ニチ", "kun": "ひ", "strokes": 4 }
  ],
  "kanji": [
    {
      "char": "明",
      "parts": ["日", "月"],
      "recipe": ["日", "月"],
      "meaning": "bright",
      "on": "メイ",
      "kun": "あか(るい)",
      "jlpt": "N4",
      "strokes": 8
    }
  ],
  "words": [
    {
      "word": "明日",
      "parts": ["明", "日"],
      "reading": "あした",
      "meaning": "tomorrow",
      "jlpt": "N5"
    }
  ]
}
```

Notes:
- `parts` for kanji come from KRADFILE; not every decomposition is pedagogically
  clean — prefer 2-part decompositions for the game; store full decomposition
  anyway for the disassemble view.
- **`parts` vs `recipe` (honor from day one):** `parts` is the full factual
  decomposition (disassemble view); `recipe` is the crafting combination the
  game uses. At N5 they are usually identical. At N3+ many kanji nest
  (想 = 相 + 心, where 相 = 木 + 目): the recipe should use the largest
  already-learnable sub-kanji (相 + 心), never the flattened primitives.
  Recipe derivation rule: prefer the decomposition whose every part is itself
  a learnable kanji/component at or below the target level. Irregulars have
  `recipe: null` (found, not crafted).
- **Per-world files (honor from day one):** emit `data/n5.json`, `data/n4.json`,
  etc., not one monolith. The app lazy-loads a world when unlocked; the service
  worker caches each world after first load. Keeps first paint small as levels
  grow (full N5–N2 content is ~1–2 MB raw, ~300 KB gzipped — fine, but no
  reason to front-load it).
- **Future field, do not implement yet:** `phonetic` (e.g. "青" for 清/晴/精) —
  phonetic-family discovery is the planned N3+ mechanic. Schema consumers must
  tolerate unknown fields so this can be added without migration.
- `words` must be validated against JMdict (common entries only — use JMdict
  priority/frequency markers). Cap v1 at ~500 words.
- **Pipeline may use SQLite** (single-file `content.db`, JDBC) as an
  intermediate store for curation queries. JSON remains the ONLY delivery
  format; no database ever sits in the serving path.

## Progression design (fixed decisions)

The game is organized as **scenes** (themed levels: the sky, the mountain,
people, the field, ...). A scene is an illustrated backdrop with that scene's
tiles over it — a backdrop, NOT a game engine. No explorable world, no avatar,
no physics. Discovered kanji may appear visually in the backdrop (cheap,
high-delight effect) but exploration mechanics are out of scope permanently
unless explicitly re-decided.

### Three kanji buckets — each has its own discovery verb
- **Primitives** (日, 月, 木, 山, 人, ...): cannot be built. They are **found** —
  glowing/tappable in the scene backdrop.
- **Craftables** (明, 林, 森, 休, 時, 間, ...): built by combining owned tiles.
  The hero mechanic.
- **Irregulars** (ugly or out-of-level decompositions, e.g. 曜): also **found**,
  with a breakdown shown after collection. NEVER force a bad recipe to make a
  kanji craftable. This bucket is the escape valve for full JLPT coverage.

### Rules
- **Dependency before compound**: a scene may only require crafting kanji whose
  parts the player already owns (from this or any earlier scene).
- **Cumulative inventory**: components and kanji carry across scenes (this is
  the built-in review mechanism).
- **Unlock threshold**: next scene opens at ~70% completion of the current one;
  stragglers remain craftable later. Never hard-gate at 100%.
- Per-scene stars: found all / crafted all / bonus words made.
- Save data: record a per-kanji discovery timestamp (enables a future SRS
  layer without migration). No SRS in v1.

### scenes.json (hand-curated overlay; pipeline does not generate it)
```json
{
  "scenes": [
    {
      "id": "sky",
      "title": "The sky",
      "backdrop": "sky.svg",
      "find": ["日", "月", "一", "二", "三"],
      "craft": ["明"],
      "words": ["明日"],
      "unlockThreshold": 0.7
    }
  ]
}
```

### Pipeline validator (required, part of Phase 1)
A Java validator that reads `kanji-data.json` + `scenes.json` and FAILS the
build if:
- any `craft` kanji's parts are not obtainable from this or earlier scenes;
- any target-level kanji (N5 for World 1) is missing from all scenes, or
  assigned to more than one scene;
- any `words` entry's kanji are not obtainable by that scene.
It must also print a coverage report (kanji per scene, craft/find/irregular
counts).

### World 1 — N5 (~100 kanji, ~10 scenes) draft ladder
Curation may adjust, but preserve the arc: craft-heavy early, found-heavy late,
narrative payoffs (日本 in scene 2, 時間 in scene 7, 日本語 in scene 8).
1. The sky — find 日, 月, numbers; craft 明; word 明日 (first win < 60s)
2. The mountain — find 山, 木, 川; craft 林, 森, 本, 東; word 日本
3. People — find 人, 女, 子; craft 好, 休; words 女子, 休日
4. The field — find 田, 力, 口; craft 男; word 男女
5. Water and fire — find 水, 火, 雨, 天; word 火山
6. The body — find 目, 耳, 手, 足; craft 見
7. Time — find 門, 寺; craft 間, 時; words 時間, 今日
8. School — find 言, 五, 学, 生; craft 語, 話; word 日本語
9. The town — 行, 来, 出, 入, 車, 駅-related
10. Numbers and counters wrap-up — remaining found-only kanji, word remixes

### World 2 — N4 (~170 kanji)
Revisit the SAME scenes ("the mountain in winter") with new components and
deeper kanji, rather than inventing 15 new locations. N4 leans harder on the
irregular bucket — be ruthless about not forcing recipes.

## Build order (do phases in order; each phase ships something)

1. **Phase 1 — Java parser + scene validator** → produces `kanji-data.json`
   per schema above; validates the hand-written N5 `scenes.json` against it.
   Done when: JSON validates, covers JLPT N5 kanji + ~200 components,
   spot-checks correct (日+月→明, 木+木→林, 女+子→好, 田+力→男, 人+木→休),
   and the validator passes on the N5 scene ladder with full coverage.
2. **Phase 2 — Web app v1 (React)**: scene view (backdrop + found illustrations
   + craft), grab/tap-to-combine with the illustration→kanji transform for
   Tier 1 primitives, stroke-draw on craft (KanjiVG), per-scene progress +
   unlock, disassemble view, localStorage persistence, PWA manifest + service
   worker. Done when: N5 World playable offline on a phone browser with at
   least scene 1 ("the sky") fully art-complete, deployed to GitHub Pages.
3. **Phase 3 — PDF workbook generator** (Java/PDFBox), N5 workbook first.
   Done when: a teacher could print and use it as-is.
4. **Phase 4 — polish**: floating/drifting tile animation, KanjiVG-powered
   disassembly animation, sound, share-a-discovery image generation.

Do not start a later phase before the earlier one is done and reviewed.

## Conventions

- Java: 17+, Maven, standard project layout, JUnit 5 tests for all parsers
  (parsers are the highest-risk code — encoding bugs are silent).
- TypeScript: strict mode, React + Vite. Runtime deps: react, react-dom,
  framer-motion (`motion`) — anything beyond that needs a DECISIONS.md entry.
- Every data-source parser must fail loudly on unexpected input, never
  silently skip malformed entries without logging a count.
- Keep a `DECISIONS.md` log: when you (the agent) make a non-obvious choice,
  append one line explaining it.
- Japanese text: UTF-8 everywhere in our own files. Test on real devices —
  fonts/rendering differ between desktop and mobile.

## Non-goals for v1 (do not build these)

- User accounts, backend APIs, databases
- Spaced-repetition system (v2+ candidate)
- Handwriting recognition
- Native mobile apps
- Covering all 2,136 Jōyō kanji at once — N5/N4 depth beats shallow breadth

## Business context (for prioritization judgment)

- The web app is the free traffic/virality engine; the PDF workbook is the
  first paid product. When trading off effort, workbook quality > app polish.
- Developer is building this in public as part of a personal brand about
  learning Japanese in Japan — small shippable milestones are worth more
  than long invisible stretches of work.
