# Known issues and gaps (honest list)

Items are grouped by spec area. Each item says what is missing and where the work is planned.

## Not built yet (NOW scope)

- **AI button in the player (AI-1, AI-2, AI-3, AI-4 UI).** Providers, the context builder, the prompt renderer,
  the key store and the settings are built and tested. The button, the long-press prompt editor, the loading
  ring, the answer sheet and pause/resume are not wired into the player yet. Planned in Phase 7.
- **Learning popups (LRN-2).** `WordLevelProvider` (default `CoreWordLevelProvider`) exists. The popup pipeline
  (low-opacity cards, floating and fading) is not built. The learning-mode and popup-count settings are saved
  but not applied. Phase 6.
- **Word-colouring for POS and phrasal (SUB-5).** Only the My Words underline is applied. POS and phrasal
  styles need LATER-9.
- **Quick-action docking and layout mode (SUB-2, SUB-3).** The dock settings exist; no docking UI or layout
  mode yet. Phase 3 follow-up.
- **Subtitle batch tools UI (SUB-6).** `SubtitleTools` (remove line breaks, max characters, search) is
  implemented and tested. The UI is not wired. Phase 8.
- **Subtitle Layer multi-track (PLY-7).** One external track per layer is supported. Selecting between several
  tracks in each layer is not built.
- **Embedded dual-layer subtitles (AR-007).** An embedded track feeds only the learning layer.
- **Landscape subtitle list panel (PLY-6).** The list is a bottom sheet in both orientations. The landscape
  side panel and search in the list are not built.
- **Two-finger swipe speed gesture (PLY-4).** Not implemented. Horizontal seek, vertical brightness and volume,
  and double-tap pause or seek work.
- **Shadowing hold-to-flip (SHD-3).** Tap toggle works; hold-to-flip does not.
- **Word cards, app menus and AI answer fonts (GEN-3).** Only the learning subtitle, translation subtitle and
  translation popup surfaces are wired. The others are listed in Settings → Fonts as pending.
- **Quick-action icons and top bar menus (PLY-1).** Audio and subtitle track menus and the "more" menu are
  not built. Track selection in ExoPlayer is available in `core:player` but not exposed.
- **Auto-load same-name subtitles (ENG-3).** Needs folder access (tree URI). Not built.
- **Restore of selected subtitle tracks after process death (ENG-8).** Position restore works; track
  selection restore needs persisted subtitle URIs. Not built.
- **Compose UI tests for the player (ENG-10).** Unit tests cover the logic; UI tests with `FakePlayerController`
  are not written yet.
- **Play-from-recent after the SAF grant is revoked.** Recents show the last title; if the grant is gone, opening
  fails with an error message.

## Limitations of what is built

- **Translation needs a model download once.** ML Kit downloads the language pair on first use. After that it
  works offline (GEN-7). The first use of a pair needs network access.
- **Decoder HW mode can fail on devices without a hardware decoder for a format.** The player shows the error
  (it is not hidden). Use HW+ to fall back to software.
- **Persian rendering depends on the device's fonts** (D-028).
- **Debug-signed release APK (D-025).** Installing a build from a different CI runner over another signed build
  can fail with a signature mismatch; uninstall first.
- **Google Translate details (LRN-1).** The details button opens the Google Translate page in a browser. The
  offline dictionary is LATER-3.
- **Minimum API 31.** Older Android versions are not supported (D-004).

## Build pins

- navigation-compose 2.8.9 and AppCompat 1.7.1 are pinned because newer releases need compileSdk 37 (AR-010, D-041).

## Verification gaps

- No device run has been recorded yet. All verification so far is through CI (unit tests, lint and build).
  Device checks for the POCO X3 Pro are listed in PROGRESS.md.
