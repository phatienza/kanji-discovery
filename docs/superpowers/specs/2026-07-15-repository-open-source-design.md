# Repository Open-Source Design

## Goal

Establish a reviewable local Git workflow and document Kanji Discovery as an open-source project without changing the Phase 1 implementation or starting Phase 2.

## Branch workflow

- Preserve the completed Phase 1 MVP as the initial commit on `main`.
- Create `develop` from that baseline.
- Make the licensing and README changes on `develop` so they remain reviewable before merging to `main`.
- Do not create a GitHub repository, configure a remote, push branches, or open a pull request; the project owner will handle GitHub setup.

## Licensing boundaries

- License original project source code and documentation under the MIT License.
- Keep derived EDRDG data subject to its Creative Commons Attribution-ShareAlike 4.0 terms.
- Keep copied KanjiVG SVGs subject to Creative Commons Attribution-ShareAlike 3.0.
- Retain `THIRD_PARTY_NOTICES.md` and make the distinction between project code and third-party data/assets explicit in the README.
- Do not claim ownership of upstream or derived dictionary material.

## README changes

Keep the existing Phase 1 build and curation instructions, and add:

- the discovery-game value proposition;
- an explicit Phase 1 project-status statement;
- the `main`/`develop` review workflow;
- the MIT code license and third-party data-license boundaries;
- a link to `THIRD_PARTY_NOTICES.md`;
- a reminder that raw upstream source files are downloaded locally and excluded from Git.

## Files changed on `develop`

- Add `LICENSE` with the standard MIT License text.
- Update `README.md`.
- Append the approved branch and licensing choices to `DECISIONS.md`.

No Java behavior, generated world data, scene curation, frontend files, or workbook files change as part of this work.

## Verification

- Confirm the active branch is `develop` after the baseline commit.
- Confirm `git diff main...develop` contains only `LICENSE`, `README.md`, and `DECISIONS.md`.
- Run `mvn test` to verify the documentation-only change did not disturb the Phase 1 baseline.
- Scan the README and license files for contradictory licensing claims and unfinished placeholders.
