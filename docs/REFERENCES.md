# References — what was learned, adopted, rejected

Each reference was studied before design (PHASES Phase 0). Only ideas were taken from GPL-3.0 projects.
MIT code was not copied either: the same behaviour was re-implemented from the idea.
Repositories were cloned to a temporary directory outside the repo for study and are not vendored.

## hoangkien1703/dual-sub-replay — MIT (Kotlin + Compose)

Studied: `README.md`, `AGENTS.md`, `app/build.gradle.kts`, `.github/workflows/*.yml`,
`data/SubtitleMerger.kt`, `full/.../OnDeviceTranslator.kt`, `THIRD_PARTY_NOTICES.md`.

- **Learned:** AGP 9.3.2 / Kotlin 2.3.21 / Gradle 9.5.0 / compile SDK 36 is a known-green combination in
  2026 CI. The CI runs `formatCheck complexityCheck testDebugUnitTest lintDebug assembleDebug` and checks
  the APK package name. The app keeps caption retrieval behind one class because it uses undocumented
  endpoints. The translator wraps ML Kit with a model-download pre-step and a cache.
- **Adopted (idea):** merging short cues into readable blocks, with thresholds for gap (1.2 s), duration
  (6 s), characters (96) and sentence-ending punctuation. We implemented this as our own pure function in
  `core:subtitle`. Also adopted: ML Kit model prepare before playback, and undocumented-endpoint isolation
  (OTH-4).
- **Rejected:** the fdroid Bergamot native engine (large native build, MPL/Marian licences for a
  non-goal), YouTube caption scraping (not in NOW scope), Japanese morphology (not our language pair).
- **Notice:** MIT notice is kept in THIRD_PARTY_NOTICES as a reference-only entry (no code copied).

## kgurniak91/yall-mp — GPL-3.0 (TypeScript/Electron, mpv) — ideas only

- **Learned (README):** an interactive subtitle timeline (drag clip edges, split, merge, retime),
  listening/speaking study presets, context-aware speed control, tokenisation with `Intl.Segmenter`
  or dictionary scanning, multi-track subtitle switching, sentence-mining export.
- **Adopted (idea):** the subtitle list with tap-to-seek and editing ideas, two study presets as settings
  (LEARNING/SHADOWING), tokenisation by word boundaries.
- **Rejected:** Electron/mpv architecture, Anki export (not NOW), code (GPL; never copied).

## arianneorpilla/jidoujisho — GPL-3.0 (Flutter) — ideas only

- **Learned (README):** tap-and-drag selection of subtitle text for instant lookups; horizontal swipe to
  repeat the current subtitle; swipe or volume button to dismiss a lookup popup; external subtitles with
  the same file name load by default; subtitle delay setting; transcript view; a chat helper for grammar.
- **Adopted (idea):** horizontal swipe = repeat current block (configurable gesture), dismiss a popup by
  tapping elsewhere, same-name subtitle auto-load (for folders we can read), per-layer delay, transcript
  (Subtitle List View), AI chat helper concept (AI button).
- **Rejected:** Flutter/Isar/Hive stack, Jellyfin and Anki integrations (not NOW), code (GPL).

## SubX Player — closed source — UX inspiration only

- **Adopted (UX ideas only):** dual subtitles, subtitle list, gesture resize/reposition of subtitles,
  repeat with delay and count, auto skip, auto pause, seek by subtitle, folder playlists. No code or assets used.

## melonityhub/proudvocab — no licence (own repo) — visual idea only

- **Learned (CSS of `proudvocab-desktop/src/renderer/desktop/desktop-panel.css`):** dark translucent panels
  (`rgba(19,26,44,.72)`), 12 px panel radius, 10 px card radius, soft amber highlight (`rgba(255,200,120,.07)`
  with a `.22` border), a light text ramp (`#e8ecf7`, `#cfd6e8`, `#9aa6c4`), and a font stack starting with
  a geometric sans with Arabic fallbacks.
- **Adopted (visual idea, re-implemented natively in Compose):** word/translation cards with the translucent
  dark panel, amber accent for highlights, and the light text ramp, as tokens in `core:design`.
- **Rejected:** copying the stylesheet and JS code (no licence).

## melonityhub/dictionaryproject — no licence (own repo) — schema/layout ideas only

- **Learned (`docs/sample.sqlite`, `README.md`):** the result sections are meanings with CEFR and part of
  speech, synonyms/antonyms, phrasal verbs, collocations, idioms, word family, categories, sentences,
  and US/UK audio entries. Persian meanings sit next to English headwords.
- **Adopted (structure only):** our dictionary schema (for LATER-3) uses the same section split. The
  schema is re-designed as our own, not copied from that database.
- **Rights concern (logged, AR-002):** that README says its SQL, sorting, and category logic came from a
  third-party Android app's Java source, and that the translation loader was reverse-engineered from
  `.bipe` model files. SubLearn uses none of that. Translation uses the official ML Kit API.

## google-10000-english — word-frequency list (data)

- **Checked licence:** `LICENSE.md` says the data is derived from the LDC Google Web Trillion Word Corpus,
  permitted for educational and personal/research use, and recommends a licence from the LDC for any
  commercial use. **Not bundled** (D-020). See AR-003.
