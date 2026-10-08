# Checklist: every spec ID, its phase, status and verification

Status values: **DONE** (built and verified by automated tests or CI; device check pending where noted),
**PARTIAL** (real code with named gaps in KNOWN_ISSUES), **TODO** (NOW scope, not built yet),
**STUB** (LATER: interface, NotImplemented implementation, flag, and a visible disabled entry where relevant).

Verification keys: **UT** = unit test (pure JVM or Robolectric), **CI** = built and linted in GitHub Actions,
**DEV** = manual check on a device (not yet recorded; see PROGRESS.md).

| ID | Requirement (short) | Phase | Status | Verification / notes |
|----|---------------------|-------|--------|----------------------|
| GEN-1 | Modern animated UI with design direction | 0–1 | PARTIAL | Tokens, themes, glass panel (DESIGN_SYSTEM.md). CI. Animation polish in Phase 9. DEV |
| GEN-2 | Everything customisable | 1–9 | PARTIAL | Typed settings for appearance, player, subtitles, fonts, gestures, shadowing, learning, AI, dictionary. UT (SettingsTest). Some options not wired (KNOWN_ISSUES) |
| GEN-3 | Fonts per language role and surface | 1 | PARTIAL | FontSettings per (surface, role); 3 of 6 surfaces wired. UT (`fontsAreIndependentPerRoleAndSurface`) |
| GEN-4 | Per-run RTL correctness for Persian | 3 | PARTIAL | TextDirection.Content, logical hit-testing, bidi stripping. UT (normaliser, ZWNJ). DEV for rendering |
| GEN-5 | Android best practices | 1–9 | PARTIAL | SAF, scoped intents, edge-to-edge, predictive back flag, no backup. CI. DEV |
| GEN-6 | Modular and extensible | 0 | DONE | 14 Gradle modules, interfaces per capability (ARCHITECTURE.md). CI |
| GEN-7 | NOW works offline after model download | 4 | PARTIAL | ML Kit on-device translation with LRU cache; subtitles and My Words are local. DEV (airplane mode) |
| APP-1 | Video opens straight into the player | 1 | PARTIAL | Intent filters for content/file video and http(s) VIEW; `MainActivity` routes to player. CI. DEV |
| APP-2 | Tabs Home, YouTube, Learn, Dictionary + side menu | 1 | PARTIAL | Tabs (YouTube/Learn/Dictionary are Coming soon), drawer (Level, My Words, Quiz, Updates, Settings). Level opens Settings. CI |
| APP-3 | PDF opens learning | 1 | STUB | LATER-2; tab only |
| PLY-1 | Top bar, quick actions column | 2 | PARTIAL | Back, title, decoder label, subtitle list, subtitle sheet, PiP, lock. Audio/subtitle/more menus missing. CI |
| PLY-2 | Auto-hide controls after 3 s | 2 | DONE | `controlsAutoHideMs` default 3000, clamped 1–15 s; LaunchedEffect in PlayerScreen. UT (settings clamp). DEV |
| PLY-3 | Centre and bottom controls | 2 | PARTIAL | Play/pause, previous/next block, seek slider with times. Lock, playlist, aspect via menu missing. CI |
| PLY-4 | Gestures (brightness, volume, seek, double tap, two-finger speed) | 2 | PARTIAL | Brightness, volume, horizontal seek, double-tap pause or seek. Two-finger speed missing. Remap only for double tap. DEV |
| PLY-5 | Orientation and rotation lock | 2 | PARTIAL | Auto/portrait/landscape setting, lock setting, `requestedOrientation`. DEV |
| PLY-6 | Subtitle List View, no-spoiler on long-press | 3 | PARTIAL | Bottom sheet, tap to seek, active line highlight, no-spoiler toggle by long-press. Landscape panel and search missing. CI |
| PLY-7 | Two layers with multi-track each | 3 | PARTIAL | Two independent layers, one external track each, embedded track for learning. Multi-track selection missing (AR-007). CI |
| SUB-1 | Toggle buttons with tap and hold | 3 | PARTIAL | Tap toggles, hold peeks (inverts while held). Size, transparency and position settings not applied to the buttons. CI |
| SUB-2 | Dockable buttons | 3 | TODO | Dock enum and settings exist; no docking UI |
| SUB-3 | Layout mode | 3 | TODO | Not built |
| SUB-4 | Tap-translate 1/2/3 taps with pause/resume | 4 | DONE | `TapSequencer` (320 ms window), `translateTapped`, pause on popup, resume on dismiss. UT (translation cache, normaliser). DEV for timing |
| SUB-5 | Word styling by POS, My Words, phrasal | 4–6 | PARTIAL | My Words underline applied. POS and phrasal styles need LATER-9. Settings stored |
| SUB-6 | Batch tools (remove breaks, max chars) | 8 | PARTIAL | `SubtitleTools.removeLineBreaks`, `limitCharacters` (UT). UI TODO |
| SUB-7 | Known subtitle problems handled | 3 | PARTIAL | Cue merging by sentence, gap and length (UT); whitespace and punctuation cleanup (UT); per-layer delay. Desync and position fixes limited to delay |
| SHD-1 | Repeat button tap and hold | 5 | PARTIAL | Tap = repeat with settings count; hold = auto-repeat; tap again stops. CI. DEV |
| SHD-2 | Repeat count and pause formula | 5 | DONE | `ShadowingFormula` (D, M, N, clamp), `RepeatController`; UT (ShadowingTest, FormulaTest); validation in settings |
| SHD-3 | Stop-at-end-of-block | 5 | PARTIAL | Toggle in quick actions and default setting; hold-to-flip missing |
| LRN-1 | Entertainment mode (cards, bookmark, details) | 4 | PARTIAL | Translation card with save-to-My-Words and details (Google Translate page). Phrase and multi-word selection missing |
| LRN-2 | Learning mode popups from WordLevelProvider | 6 | TODO | `WordLevelProvider` and `CoreWordLevelProvider` built and tested (UT). Popup pipeline not built |
| LRN-3 | English-only NLP (POS, phrasal, idioms) | — | STUB | LATER-9 (`WordAnalyzer`, `NotImplementedWordAnalyzer`) |
| AI-1 | Gemini default, ChatGPT, Claude; prompt editor; providers | 7 | PARTIAL | Providers, keys (Keystore), provider choice, prompt template, settings UI. UT with MockWebServer (ProvidersTest). Button in player TODO |
| AI-2 | Long-press prompt editor; pause and loading ring | 7 | TODO | Not wired |
| AI-3 | Answer covers tone, usage, synonyms difference, other usage | 7 | PARTIAL | Default prompt template covers the four points (SettingsSection). Answer UI TODO |
| AI-4 | Context = previous N blocks, default 10, title, timestamps | 7 | PARTIAL | `AiContextBuilder` (UT: N blocks, title, timestamps). Settings for N, title, timestamps. UI integration TODO |
| ENG-1 | Stack per brief | 0 | DONE | Kotlin, Compose M3, Media3, Coroutines/Flow, Room+FTS, DataStore, Koin, OkHttp, ML Kit, version catalog. CI |
| ENG-2 | Interfaces for each capability | 0–6 | PARTIAL | Built: PlayerController (+Fake), TranslationProvider, AiProvider (3), DictionaryProvider (stub), WordAnalyzer (stub), WordLevelProvider, SpeechToText (stub), UpdateChecker (stub). SubtitleParser is `SubtitleParsers`; SubtitleRepository not separate yet |
| ENG-3 | SRT, VTT, ASS; one Cue model; normaliser; charsets; per-layer delay | 3 | PARTIAL | Parsers, Cue model, normaliser, Windows-1256, delay (UT, 20+ tests). Auto-load same-name subtitle TODO (KNOWN_ISSUES) |
| ENG-4 | Decoder SW/HW/HW+ mapping, no fake controls | 2 | PARTIAL | `DecoderSelectors` (Media3 codec selection); rebuild on change. Documented in D-012. DEV on the POCO for HW |
| ENG-5 | Single gesture layer with priority | 2 | PARTIAL | Layer order: subtitles and buttons consume taps first; gesture layer below. Configurable actions limited (double tap) |
| ENG-6 | Typed, versioned, searchable settings, JSON export/import | 1 | DONE | Schema v1 with migration mechanism, codec, export/import through SAF, sanitised values. UT (SettingsTest: round trip, legacy, newer schema refused, clamps, no secrets). Search by category keyword |
| ENG-7 | API keys Keystore-encrypted, never logged or exported | 7 | DONE | `SecretStore` (AES-256-GCM, Keystore), backups excluded, export excludes keys (UT `exportNeverContainsSecretFields`). DEV to confirm Keystore on device |
| ENG-8 | Permissions only when needed, intents, PiP, audio focus, process-death restore, edge-to-edge, predictive back, large screens | 1–9 | PARTIAL | SAF with persistable grant, intents, PiP auto-enter, ExoPlayer audio focus, position restore, edge-to-edge, predictive back flag. Track restore and large-screen layouts missing |
| ENG-9 | Smooth overlay, O(log n) cue lookup, no main-thread blocking | 3 | PARTIAL | `CueIndex` with prefix max (UT, 500-cue overlap test). Parsing on IO. Smooth-animation check DEV |
| ENG-10 | Unit, UI, RTL tests; instrumented smoke | 0–9 | PARTIAL | UT across core modules; Robolectric Room FTS test; TranslationCache test. Compose UI tests and instrumented smoke not written yet |
| ENG-11 | Strings EN and FA, content descriptions, touch targets, font scaling, themes, tokens | 0–9 | PARTIAL | EN and FA resources for all screens; content descriptions on main controls; 48 dp targets; sp fonts. FA text reviewed by reading only; DEV for layout |
| OTH-1 | Reference study recorded | 0 | DONE | REFERENCES.md (learned, adopted, rejected) |
| OTH-2 | Licence audit recorded; app licence | 0 | DONE | Apache-2.0 (LICENSE), THIRD_PARTY_NOTICES.md, D-002, D-029 |
| OTH-3 | ML Kit official API only; no extracted models | 4 | DONE | `MlKitTranslationProvider` uses `Translation.getClient` only; AR-004 logged |
| OTH-4 | Undocumented endpoints behind an interface | 0 | DONE | None used. Rule written in AGENTS.md and EXTENSION_POINTS.md |
| OTH-5 | Other UI languages allowed by structure, docs explain how | 0 | DONE | EXTENSION_POINTS.md section "Adding a UI language" |
| OTH-6 | CI builds APK; release with APK | 0–9 | PARTIAL | `android.yml` builds and uploads the debug APK; `release.yml` publishes on `v*` tags. Tag v0.1.0 not created yet (AR-006) |
| OTH-7 | Device: POCO X3 Pro, Android 12+ (minSdk 31) | 0 | PARTIAL | minSdk 31 in convention plugin (D-004). DEV on the device still to do |
| LATER-1 | YouTube section | — | STUB | `VideoSourceResolver`, flag `YOUTUBE`, tab "Coming soon" |
| LATER-2 | PDF / browser / image learning | — | STUB | Flag `LEARN_SECTION`, tab "Coming soon" |
| LATER-3 | Dictionary section, offline import | — | STUB | `DictionaryProvider` stub, flag `DICTIONARY`; Settings shows offline option disabled |
| LATER-4 | Automatic level detection | — | STUB | `LevelDetector` stub, flag `LEVEL_DETECTION` |
| LATER-5 | Quiz | — | STUB | `QuizEngine` stub, flag `QUIZ`, drawer entry "Coming soon" |
| LATER-6 | Update checker | — | STUB | `UpdateChecker` stub, flag `UPDATE_CHECKER`, drawer entry "Coming soon" |
| LATER-7 | AI re-segmentation and quote marking | — | STUB | `SubtitleReSegmenter` stub, flag `AI_RESEGMENTATION` |
| LATER-8 | Offline speech-to-text | — | STUB | `SpeechToText` stub, flag `SPEECH_TO_TEXT` |
| LATER-9 | POS, phrasal, collocation, idiom, CEFR detection | — | STUB | `WordAnalyzer` stub, flag `WORD_ANALYSIS` |
| LATER-10 | On-device AI models | — | STUB | Flag `ON_DEVICE_AI` |
| LATER-11 | Additional languages | — | STUB | Flag `MORE_LANGUAGES`, structure in EXTENSION_POINTS.md |
