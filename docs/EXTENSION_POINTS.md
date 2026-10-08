# Extension points (LATER items and how to add things)

Every LATER item has an interface in `core:later` (or a named core interface), a `NotImplemented` implementation
that reports `isAvailable = false`, and a flag in `FeatureFlags`. The UI shows a disabled "Coming soon" entry
where the feature will appear. Nothing behind a false flag runs.

| ID | Feature | Interface / stub | Flag | Visible entry |
|----|---------|------------------|------|---------------|
| LATER-1 | YouTube section | `VideoSourceResolver` / `NotImplementedVideoSourceResolver` | `YOUTUBE` | Tab "YouTube" (Coming soon) |
| LATER-2 | PDF, browser, image learning | (tab only) | `LEARN_SECTION` | Tab "Learn" (Coming soon) |
| LATER-3 | Dictionary section, offline import | `DictionaryProvider` / `NotImplementedDictionaryProvider` | `DICTIONARY` | Tab "Dictionary"; Settings → Dictionary "Offline dictionary: Coming soon" |
| LATER-4 | Automatic level detection | `LevelDetector` / stub | `LEVEL_DETECTION` | Side menu "Level" explains manual setting |
| LATER-5 | Quiz | `QuizEngine` / stub | `QUIZ` | Side menu "Quiz" (Coming soon) |
| LATER-6 | Update checker (GitHub Releases) | `UpdateChecker` / stub | `UPDATE_CHECKER` | Side menu "Check for updates" (Coming soon) |
| LATER-7 | AI re-segmentation and quote marking | `SubtitleReSegmenter` / stub | `AI_RESEGMENTATION` | none yet |
| LATER-8 | Offline speech-to-text | `SpeechToText` / stub | `SPEECH_TO_TEXT` | none yet |
| LATER-9 | POS, phrasal, collocation, idiom, CEFR detection | `WordAnalyzer` / stub, `WordAnnotation` | `WORD_ANALYSIS` | Word styling settings for POS/phrasal stored; not applied yet |
| LATER-10 | On-device AI models | (flag only) | `ON_DEVICE_AI` | none |
| LATER-11 | Additional languages | `UiLanguage`, `TranslationProvider` | `MORE_LANGUAGES` | none |

## Adding a provider behind an interface

1. Implement the interface in its module (for example `AiProvider` in `core:ai`).
2. Add the id to the enum that the settings use (for example `AiProviderId`) and to the factory in `app`.
3. Write a MockWebServer test for the request shape and error mapping, as `ProvidersTest` does.
4. If the provider uses an undocumented endpoint, keep it in its own class behind the interface (OTH-4) and log it in AGENT_REQUESTS.

## Adding a LATER feature

1. Confirm the interface in `core:later` (or the core interface) matches the spec ID.
2. Replace the `NotImplemented*` object with a real class; keep `isAvailable` tied to real capability.
3. Flip the flag in `FeatureFlags` and the UI entry from "Coming soon" to the real screen.
4. Add tests and update CHECKLIST, DECISIONS and PROGRESS in the same change.

## Adding a UI language (LATER-11 / OTH-5)

1. Add the tag to `UiLanguage` in `core:settings` (for example `FRENCH("fr")`) and bump nothing else; the enum is persisted by name.
2. Add `app/src/main/res/values-xx/strings.xml` and the same file for each feature module (`feature/*/src/main/res/values-xx`).
3. Add the locale to `app/src/main/res/xml/locales_config.xml`.
4. Add the ML Kit language tag mapping (ML Kit uses `TranslateLanguage.fromLanguageTag`), and verify the pair in a test.
5. Check RTL: `values-xx` with a right-to-left script needs no code change, because direction comes from the locale.

## Adding a learning-word level list (LRN-2)

Implement `WordLevelProvider` (core:model) with a list you may legally bundle, and pass it to the learning
popup pipeline in place of `CoreWordLevelProvider`. See AGENT_REQUESTS AR-003 for the licence review.
