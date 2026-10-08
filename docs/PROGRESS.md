# Progress, how to run, and final report

_Last updated: 2026-10-08 (session on branch `arena/9fe4f214-sublearn`). The live status of every spec ID is in
[CHECKLIST.md](CHECKLIST.md)._

## State at a glance

- **Phase 0 (foundations): done.** Gradle multi-module build, version catalog, convention plugins, CI (tests,
  lint, debug APK artifact), release workflow, docs set, licence audit, reference study.
- **Phase 1 (app shell): mostly done.** Navigation, tabs, side menu, intents, Home with recents, settings with
  JSON export and import, My Words screen.
- **Phase 2 (player core): mostly done.** Media3 player, MX-style overlay, gestures, orientation and lock, PiP,
  decoder mapping. Device checks pending.
- **Phase 3 (subtitle engine): mostly done.** Parsers, normaliser, blocks, cue index, two layers, subtitle list.
  Docking and layout mode pending.
- **Phase 4 (interaction): mostly done.** Tap-translate with ML Kit, translation card, My Words save and underline.
- **Phase 5 (shadowing): mostly done.** Repeat controller, formulas, tap and hold, stop at block end.
- **Phase 6 (learning mode): not started in UI.** `WordLevelProvider` default exists and is tested.
- **Phase 7 (AI button): providers and settings done, UI not started.**
- **Phase 8 (subtitle tools): core functions done, UI not started.**
- **Phase 9 (hardening): not started.** Compose UI tests, animation polish, README screenshots, v0.1.0 tag.

## How CI results are read (verification without local Gradle)

The sandbox used for this session cannot reach Google Maven or Gradle services, so every build runs in
GitHub Actions. Logs are not always downloadable, so `.github/scripts/report_failures.py` writes Gradle errors,
compile errors and failing test names as **check-run annotations**, which can be read through the API:

```bash
gh run list --branch arena/9fe4f214-sublearn --limit 1
gh api repos/melonityhub/SubLearn/check-runs/<job-id>/annotations --jq '.[] | "\(.title) | \(.message)"'
```

## How to build, run and test

Requirements: JDK 17, Android SDK platform 36 and build-tools 36.0.0 (or Android Studio with those installed).

```bash
./gradlew test                  # all unit tests (JVM and Robolectric)
./gradlew lint                  # Android lint
./gradlew assembleDebug         # app/build/outputs/apk/debug/app-debug.apk
./gradlew :core:subtitle:test   # one module
```

Install the APK on a device with Android 12 or newer, or use Android Studio's Run button. Open a video from
Home (Open video), by pasting a video link (Open URL), or from a file manager ("Open with SubLearn").

## Device checklist (POCO X3 Pro, Android 12 and 13) — to run

- [ ] Open a local MP4 from Home; playback starts; controls hide after 3 s; tap shows them again.
- [ ] Load an SRT and a WebVTT file for the learning layer; lines show; delay changes timing.
- [ ] Tap a word once, twice (line) and three times (block); the card translates; playback pauses and resumes on close.
- [ ] Repeat button: tap repeats the block; hold auto-repeats; tap stops.
- [ ] Brightness (left drag), volume (right drag), seek (horizontal drag), double tap.
- [ ] Rotate; lock rotation; enter picture-in-picture by leaving the app while playing.
- [ ] Switch decoder SW / HW / HW+ and play the same file.
- [ ] Airplane mode after the first translation: translation of the same pair still works.
- [ ] Light, dark and AMOLED themes; English and Persian UI; Persian subtitle lines render right-to-left.
- [ ] Export settings to a file, change a value, import the file, confirm the value is restored.

## Decisions taken this session

See [DECISIONS.md](DECISIONS.md). The main ones: branch model (D-027), minSdk 31 (D-004), Koin (D-007),
Apache-2.0 (D-002), ML Kit only (D-018), word-level default without a third-party list (D-020),
debug-signed release (D-025).

## Final report (for the owner)

### What was built

- Gradle build with 14 modules, a version catalog and convention plugins, and a CI workflow that runs tests,
  lint, and builds the debug APK, plus a release workflow for tags.
- Pure, tested subtitle engine (SRT, WebVTT, ASS/SSA; charsets; normaliser; blocks; cue index; tokenizer; batch tools).
- Typed, versioned settings with JSON export and import; Keystore-encrypted API keys.
- Media3 player with decoder mapping, a MX-style overlay, gestures, orientation lock, PiP, two subtitle layers,
  a subtitle list with no-spoiler mode, tap-translate with ML Kit, shadowing controls.
- My Words with full-text search; Home with recents; settings screens; coming-soon sections for LATER items.
- Three AI providers with tests (UI not yet connected).

### What was not built, and why (AGENT_REQUESTS and KNOWN_ISSUES)

- AI button and prompt editor in the player, learning popups, dock and layout mode, batch tool UI, track
  menus, auto-load of same-name subtitles, Compose UI tests — time in this session. Planned in Phases 6–9.
- Rights and licence questions (AR-002 dictionaryproject provenance; AR-003 word lists; AR-004 ML Kit terms).
- Local Gradle execution in the sandbox (AR-001); verification is through CI only.

### Key decisions and assumptions

See DECISIONS.md, each tagged ASSUMPTION where applicable. In particular: D-004 (minSdk 31), D-007 (Koin),
D-012 (decoder mapping), D-013 (embedded track limit), D-017 (blocks), D-020 (no third-party word list),
D-025 (debug-signed releases), D-027 (phase commits instead of phase branches).

### License findings and open rights questions

- App: Apache-2.0. All runtime dependencies are Apache-2.0, MIT or EPL-1.0 (test only), and Google ML Kit under
  Google's terms. THIRD_PARTY_NOTICES lists them.
- No GPL code. GPL projects were studied for ideas only.
- proudvocab and dictionaryproject have no licence: only visual and structural ideas were used.
- google-10000-english is LDC-restricted for commercial use: not bundled.
- dictionaryproject's README states that its SQL and model loader came from third-party sources: logged (AR-002).

### Risks, and what to review first

1. **The first CI-green build of the Android modules.** The code has been compiled only through CI runs; review
   the latest `android.yml` run before trusting the APK.
2. **Player behaviour on the POCO X3 Pro:** decoder modes, PiP, brightness and volume gestures, and orientation.
3. **Tap-translate timing** (320 ms window) and pause/resume.
4. **Subtitle parsing on real files** (encodings, ASS overrides). Use files from different sources.
5. **Repeat state machine** at block boundaries (`awaitingBlockRestart` logic) — review before the shadowing demo.

### Suggested next steps

1. Record the device checklist above and fix what it finds.
2. Phase 6: popup pipeline and learning-mode cards using `WordLevelProvider`.
3. Phase 7: AI button, long-press prompt editor, loading ring, answer sheet, pause and resume.
4. Phase 8: batch tools UI and subtitle search; landscape subtitle panel.
5. Phase 9: Compose UI tests with `FakePlayerController`, animation and accessibility pass, README screenshots,
   then tag v0.1.0 (AR-006) once the owner confirms.
6. Stretch items, in order and only after NOW is green: offline dictionary (LATER-3), My Words quiz (LATER-5),
   update checker (LATER-6). Stop after item 3.
