# SubLearn — Product Specification (English, ID-numbered)

Status legend used in [CHECKLIST.md](CHECKLIST.md): `DONE` (implemented and verified in CI), `PARTIAL`
(real code, gaps listed), `TODO` (NOW scope not yet built), `LATER` (extension point only, see
[EXTENSION_POINTS.md](EXTENSION_POINTS.md)).

SubLearn is a free, open-source, ad-free Android video player for learning English from subtitles.
Defaults: app UI English · learning language English · native/translation language Persian (RTL).

## General

- **GEN-1** Modern, animated UI/UX with a stated design direction (see [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md)).
- **GEN-2** Everything customisable: wherever a feature has several options they are settings.
- **GEN-3** Font family, size, colour and weight are configurable separately per language role
  (learning / native) **and** per surface (app menus, learning subtitles, translation subtitles,
  translation popups, word cards, AI answers). Settings never bleed between surfaces.
- **GEN-4** RTL correctness for Persian everywhere; direction is resolved per text run, not only per app.
- **GEN-5** Android best practices: permissions only when needed, SAF / scoped storage, lifecycle,
  PiP, rotation, process death.
- **GEN-6** Modular and extensible: a new feature must not break old ones (Gradle modules + interfaces).
- **GEN-7** NOW features work fully offline once the on-device translation model is downloaded.

## App entry

- **APP-1** Opened with a video (or video URL) → straight into the player.
- **APP-2** Opened normally → tabbed navigation: Home, YouTube, Learn, Dictionary; side menu with
  Level, My Words, Quiz, Check for updates, Settings. Home = player entry + recent local videos.
- **APP-3** Opened with a PDF → learning section (LATER).

## Player (MX-Player-like; local files and URL/streams)

- **PLY-1** Top bar without background: back + file title (left), audio, subtitle, decoder
  (SW / HW / HW+), More (…) with MX-like items including PiP. Quick Actions column under the top bar, top-left.
- **PLY-2** All controls overlay the video, auto-hide after 3 s of idle; a single tap shows them.
- **PLY-3** Centre: play/pause + repeat-current-block. Bottom: seek bar (buffer indicator, elapsed left,
  total right); under it previous block / play-pause / next block. Corners: lock; Subtitle List View;
  playlist; aspect ratio and other MX-like modes.
- **PLY-4** Gestures: left vertical swipe = brightness; right vertical = volume; horizontal = seek;
  double tap anywhere except buttons/subtitle text = pause (setting can switch it to seek); two-finger
  swipe up = playback-speed shortcut. All remappable.
- **PLY-5** Landscape / portrait and rotation lock.
- **PLY-6** Subtitle List View (toggle in options): landscape = right-side panel; portrait = below the
  player; current-line highlight, auto-scroll, tap-to-seek, search. Long-press on its toggle =
  no-spoiler mode (upcoming blocks hidden).
- **PLY-7** Two independent subtitle layers: Learning (primary) and Translation (secondary). Subtitle
  options have one tab per layer; each layer accepts multiple tracks (SRT, embedded soft, external)
  with MX-like options per layer.

## Subtitle controls

- **SUB-1** Two toggle buttons (learning, translation) in Quick Actions by default: tap = toggle
  visibility; hold = temporarily invert until release. Size, transparency and position configurable per button.
- **SUB-2** Buttons are dockable: in a bar, free-floating, or disabled/hidden.
- **SUB-3** Quick Action "Layout mode": adjust height/position of each layer separately. Subtitle text
  has no gestures except translation, so adjustment never conflicts.
- **SUB-4** Tap a learning-subtitle word: 1 tap = translate word, 2 = line, 3 = block. While a
  translation shows, playback pauses; tapping elsewhere dismisses it and resumes.
- **SUB-5** Word styling by (a) part of speech, (b) My Words, (c) phrasal verbs / collocations, each
  with its own style (underline, dotted underline, box, background, bold, colour…). Styles and settings
  are NOW; POS/phrasal detection is LATER behind `WordAnalyzer`.
- **SUB-6** Subtitle management quick action: batch-remove line breaks inside blocks; max characters
  per block (split). LATER: AI re-segmentation and AI quote marking.
- **SUB-7** Known problems handled: cues split mid-sentence across blocks, stray spaces/newlines,
  punctuation, desync between layers, position mismatch.

## Shadowing

- **SHD-1** Repeat button: tap = repeat the current block once; press-and-hold then release = auto-repeat mode.
- **SHD-2** Configurable repeat count and pause between repeats; the pause is a formula over the block
  duration `D` (ms) with an adjustable multiplier `M`.
- **SHD-3** "Stop at end of block" toggle: like play/pause but auto-pauses when the block ends; hold to flip temporarily.

## Learning and dictionary

- **LRN-1** Entertainment mode: tap a word → simple translation; if part of a phrase, a phrase or
  contextual translation appears under it; multi-word selection; bookmark (My Words); an icon opens a
  full-details overlay (offline dictionary when available, otherwise a Google Translate window; the
  default is a setting). Card style inspired by proudvocab.
- **LRN-2** Learning mode: everything above, plus words/phrases of the current block above the user's
  level appear with translations as low-opacity, soft-shadow popup cards in a corner, floating up from
  the bottom and fading out. Level data comes from `WordLevelProvider`: manual-level setting plus a
  default provider; automatic level detection is LATER.
- **LRN-3** English only: phrasal verbs, collocations, idioms, POS and word level via offline
  lightweight NLP — LATER (`WordAnalyzer` stub).

## AI button

- **AI-1** For hard blocks (idioms, story/film context). Prompt editable in Settings; API key in
  Settings; providers Gemini (default), ChatGPT (OpenAI), Claude (Anthropic).
- **AI-2** Long-press opens the prompt editor. A tap sends the selected text if any, otherwise the whole
  block. Playback pauses until the result shows (the user may resume); the button shows a circular loading ring.
- **AI-3** The answer covers: tone, why it is used here, how it differs from synonyms, where else it is used.
- **AI-4** Context sent: previous N blocks (default 10) + film title + timestamps (each configurable).

## Engineering requirements

- **ENG-1** Stack: Kotlin, Jetpack Compose + Material 3, Media3 (ExoPlayer, PiP), Coroutines/Flow,
  Room (+FTS), DataStore (typed settings), Koin, OkHttp (REST), ML Kit on-device translation,
  Gradle version catalog, multi-module. Deviations are recorded in [DECISIONS.md](DECISIONS.md).
- **ENG-2** Every external capability sits behind an interface: `PlayerController` (Media3 + Fake),
  `SubtitleParser` / `SubtitleRepository`, `TranslationProvider` (ML Kit), `AiProvider` (Gemini,
  OpenAI, Anthropic), `DictionaryProvider`, `WordAnalyzer`, `WordLevelProvider`, `SpeechToText`,
  `UpdateChecker`.
- **ENG-3** Subtitles: own parsers for SRT, WebVTT and ASS/SSA (text only) into one `Cue` model
  (id, startMs, endMs, text, tokens, trackId). A normalisation pipeline (strip tags, trim, collapse
  spaces, merge/split fragments, punctuation-aware cuts, max chars) of pure, unit-tested functions.
  Two layers are rendered by our own Compose overlay so words are tappable; word hit-testing is correct
  for RTL/bidi text. Charset detection (UTF-8, UTF-16, Windows-1256 for Persian). Per-layer delay.
- **ENG-4** Decoder SW / HW / HW+ mapped to real Media3 codec selection (see DECISIONS D-012). A control
  that cannot work is hidden or labelled and logged; it is never shown as a no-op.
- **ENG-5** Gestures: one gesture layer with explicit priority (subtitle text > buttons > video
  surface), configurable actions, no clash with system gesture areas.
- **ENG-6** Settings: typed, versioned schema; searchable screen grouped by category (Player,
  Subtitles, Fonts, Gestures, Shadowing, Learning, AI, Dictionary, Appearance, About); export/import as JSON.
- **ENG-7** Secrets: API keys encrypted with a Keystore-backed key, never logged, never in the repo,
  never in settings export or backups.
- **ENG-8** Android: runtime permissions only when needed (SAF / photo picker; `READ_MEDIA_VIDEO` on
  13+ if ever needed); intent filters so the app opens video files and URLs directly in the player; PiP;
  audio focus; process-death restore (position, tracks, layout); edge-to-edge; predictive back; large-screen basics.
- **ENG-9** Performance: smooth overlay animation; cue lookup by time in O(log n); no blocking work on the main thread.
- **ENG-10** Testing: unit tests for parsers, normalizer, repeat/pause formulas, settings, repositories;
  Compose UI tests for key screens using `FakePlayerController`; RTL checks; an instrumented smoke test in CI if feasible.
- **ENG-11** Accessibility and i18n: all strings in resources (EN + FA), content descriptions, touch
  targets, font scaling, light/dark/AMOLED themes, design tokens only (no hard-coded colours, sizes,
  durations or fonts).

## Other requirements

- **OTH-1** Reference projects studied first; [REFERENCES.md](REFERENCES.md) records what was learned,
  adopted, and rejected. No GPL code is copied.
- **OTH-2** Licence audit recorded; the app licence is Apache-2.0 ([LICENSE](../LICENSE)); third-party
  notices in [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).
- **OTH-3** Translation uses the official Google ML Kit on-device Translation API; no extracted model files.
- **OTH-4** Anything using an undocumented endpoint sits behind an interface so it can be replaced.
- **OTH-5** Other UI languages are not built now; the structure allows them and
  [EXTENSION_POINTS.md](EXTENSION_POINTS.md) explains how to add one.
- **OTH-6** GitHub CI builds the debug APK on every PR and publishes a release with the APK.
- **OTH-7** Device target: POCO X3 Pro, Android 12 or newer (minSdk 31).

## LATER items (interfaces and stubs only; see EXTENSION_POINTS.md)

- **LATER-1** YouTube section (interface `VideoSourceResolver`, flag `youtube`).
- **LATER-2** PDF / browser / image learning (flag `learnSection`).
- **LATER-3** Dictionary section and offline dictionary import (`DictionaryProvider`, flag `dictionary`).
- **LATER-4** Automatic level detection (`LevelDetector`).
- **LATER-5** Quiz (`QuizEngine`, flag `quiz`).
- **LATER-6** Update checker via GitHub Releases (`UpdateChecker`, flag `updateChecker`).
- **LATER-7** AI re-segmentation of subtitles and AI quote marking (flag `aiResegmentation`).
- **LATER-8** Offline speech-to-text subtitle generation (`SpeechToText`, flag `speechToText`).
- **LATER-9** POS / phrasal / collocation / idiom / CEFR detection, offline NLP (`WordAnalyzer`, flag `wordAnalysis`).
- **LATER-10** On-device AI models (flag `onDeviceAi`).
- **LATER-11** Additional languages (flag `moreLanguages`).
