# Third-party notices and licence audit

SubLearn's own code is Apache-2.0 (see [LICENSE](LICENSE)). This file lists what the app and its build use,
and what was studied but not copied. Licences are those declared by each project; checked against the
project pages and POMs on 2026-10-08 (see DECISIONS D-002 and D-029).

## Runtime dependencies (shipped in the APK)

| Component | Licence | Notes |
|-----------|---------|-------|
| AndroidX (Core, Activity, AppCompat, Lifecycle, Navigation, Room, DataStore) | Apache-2.0 | |
| Jetpack Compose, Compose BOM, Material 3, Material Icons Extended | Apache-2.0 | Material icons are used under the same licence |
| AndroidX Media3 (ExoPlayer, UI) | Apache-2.0 | |
| Koin (koin-android, koin-androidx-compose) | Apache-2.0 | |
| OkHttp, Okio | Apache-2.0 | |
| kotlinx.serialization, kotlinx.coroutines | Apache-2.0 | |
| Google ML Kit Translation | Google terms (proprietary SDK) | Downloads its own language models on device; SubLearn never ships or extracts model files (OTH-3). See AGENT_REQUESTS AR-004. |

## Build and test only (not shipped)

| Component | Licence |
|-----------|---------|
| Gradle Wrapper (`gradle-wrapper.jar`, `gradlew`) | Apache-2.0 |
| Android Gradle Plugin, Kotlin Gradle plugin, KSP | Apache-2.0 |
| JUnit 4 | EPL-1.0 |
| Robolectric | MIT |
| MockWebServer (okhttp-mockwebserver) | Apache-2.0 |

## Studied, not copied (see docs/REFERENCES.md)

| Project | Licence | Use in SubLearn |
|---------|---------|-----------------|
| hoangkien1703/dual-sub-replay | MIT | Architecture and cue-merge heuristics were studied. No code was copied. MIT attribution is listed here as a reference. |
| kgurniak91/yall-mp | GPL-3.0 | Ideas only. No code. |
| arianneorpilla/jidoujisho | GPL-3.0 | Ideas only. No code. |
| SubX Player | Closed source | UX inspiration only. |
| melonityhub/proudvocab | No licence stated | Visual idea (panel colours, radii) re-implemented in Compose. No code or assets copied. |
| melonityhub/dictionaryproject | No licence stated | Section structure only. No schema, queries or model files used. Rights question logged (AR-002). |
| first20hours/google-10000-english | LDC terms (restricted for commercial use) | Not bundled (AR-003). |

## Data

No word-frequency or CEFR list is bundled. The core function-word list in `core:model` was written for this
project and is Apache-2.0 (see `CoreFunctionWords.kt`).

## Fonts

No fonts are bundled. The app uses system fonts (DECISIONS D-028).
