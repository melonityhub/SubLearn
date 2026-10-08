# Decisions and assumptions

Every decision is numbered. Items tagged **ASSUMPTION** were decided without owner input, as the brief
requires. Each one can be revisited by a later PR that updates this file.

## Process and repository

- **D-001 Repository state.** `main` contained one placeholder file (`hello world`) from commit `9fc768e`.
  The project was built alongside it and the placeholder was left untouched. *ASSUMPTION*
- **D-008 Verification gate.** The sandbox cannot reach Google Maven, Maven Central or services.gradle.org,
  so local builds are impossible. CI on GitHub-hosted runners is the gate. *Fact*, see AGENT_REQUESTS AR-001.
- **D-025 Release model.** `v*` tags trigger a workflow that builds the debug APK and attaches it to a
  GitHub Release. There is no release keystore in the repo (repo hygiene). The APK is debug-signed, which
  is acceptable for sideloading. Play distribution is out of scope. *ASSUMPTION*
- **D-027 Branch model.** Phase branches (`phase/N-name`) conflict with the session rule that all work
  stays on `arena/9fe4f214-sublearn`. Phases are commit groups prefixed `phase N:` plus PROGRESS sections.
  One PR to `main`, merged once CI is green. The owner can change this. *ASSUMPTION*, see AR-005.

## Licence and provenance

- **D-002 App licence: Apache-2.0.** Permissive, includes a patent grant, and is compatible with every
  dependency we ship (Apache-2.0 / MIT / BSD / OFL). No GPL code is copied (GPL-3.0 references are
  studied only). The Google ML Kit SDK is under Google's terms, not an OSS licence; it is listed in
  THIRD_PARTY_NOTICES. *ASSUMPTION* (brief default).
- **D-029 No GPL and no unlicensed code.** dual-sub-replay (MIT) is studied; no code was copied, only
  ideas (see REFERENCES.md). proudvocab and dictionaryproject have no licence, so only visual and
  structural ideas are used.
- **D-020 Word-level data.** `google-10000-english` (LDC-derived data, restricted for commercial use per
  its LICENSE.md) is not bundled. `hermitdave/FrequencyWords` is labelled MIT on GitHub, but its data
  derives from OpenSubtitles, so provenance is not verified; it is not bundled. `openlanguageprofiles/olp-en-cefrj`
  has no licence. The NOW default provider therefore uses an authored list of core function words
  (written for this project, Apache-2.0) plus user-marked known words. See AR-003.

## Stack

- **D-003 Identity.** `applicationId` / namespace root `com.melonityhub.sublearn` (owner: melonityhub).
- **D-004 minSdk 31 (Android 12).** The brief requires Android 12 compatibility, and the POCO X3 Pro
  supports it. API 31 gives `MediaCodecInfo` hardware/software flags (API 29+) for decoder modes,
  `PictureInPictureParams.setAutoEnterEnabled` (API 31), and a simpler platform matrix. ASSUMPTION.
- **D-005 compileSdk / targetSdk 36.** Current stable Android SDK; matches a known-green public
  project. Edge-to-edge is enforced by default at target 35+ and is handled.
- **D-006 Versions (verified 2026-10-08).** Gradle 9.5.0 wrapper; AGP 9.3.2; Kotlin 2.3.21 (Compose and
  serialization plugins); KSP 2.3.12; Compose BOM 2026.09.00; Activity Compose 1.13.0; Navigation Compose
  2.10.2; AppCompat 1.8.0; Media3 1.11.1; Room 2.8.5; DataStore 1.2.1; Koin 4.2.2; OkHttp 4.12.0;
  kotlinx.serialization 1.10.0; kotlinx.coroutines 1.10.2; ML Kit translate 17.0.3; Robolectric 4.17;
  JUnit 4.13.2. The AGP/Gradle/Kotlin trio mirrors the public reference project dual-sub-replay, which
  builds on GitHub today. Versions were checked against Google Maven and Maven Central metadata.
- **D-007 DI: Koin.** Allowed by the brief. Koin needs no annotation processing, so it has fewer build
  breakpoints than Hilt, which matters while building without a local compiler. ASSUMPTION.
- **D-009 Serialization.** kotlinx.serialization for settings JSON and AI request/response payloads.
- **D-010 Typed settings.** A versioned `AppSettings` data model stored as JSON inside Preferences
  DataStore. Migrations are plain functions keyed by `schemaVersion`. Proto DataStore was rejected to
  avoid the protobuf plugin. ASSUMPTION.
- **D-011 Secrets.** API keys are encrypted with an AES-256-GCM key in the Android Keystore
  (non-exportable). Ciphertext lives in DataStore. `allowBackup=false` plus data-extraction rules keep it
  out of backups. Keys are excluded from settings export. Keys are never logged.
- **D-012 Decoder modes.** Media3 ExoPlayer with the platform codecs, no FFmpeg extension (its build
  needs NDK work, and FFmpeg licensing needs review).
  - **SW** = only software-only decoders (`MediaCodecInfo.softwareOnly`).
  - **HW** = only hardware-accelerated decoders. If none exist for a format, playback fails with a clear
    error (logged), so the control is never a silent no-op.
  - **HW+** = hardware decoders first, then software fallback (the platform default order).
  Changing the mode re-prepares the player at the same position. ASSUMPTION.
- **D-013 Embedded subtitles and layers.** Media3 decodes one text track per player, so an embedded
  soft track can feed one layer at a time. Two embedded tracks simultaneously need a second decode path.
  NOW: the Learning layer may use an embedded track; the Translation layer uses external tracks.
  Logged as AR-007. ASSUMPTION.
- **D-014 Subtitle rendering.** Our own Compose overlay (not PlayerView's subtitle view), so words are
  tappable and styled per surface. The word hit-test uses `TextLayoutResult.getOffsetForPosition` on the
  logical text, which is correct for RTL/bidi because offsets are logical. Tokens come from
  `java.text.BreakIterator` (word instance).
- **D-015 Charsets.** BOM detection (UTF-8, UTF-16LE/BE) → strict UTF-8 → the configured legacy
  charset (default Windows-1256 for Persian, selectable). ASSUMPTION.
- **D-016 Normalisation rules.** ZWNJ (U+200C) and ZWJ (U+200D) are preserved (Persian needs ZWNJ).
  Bidi embedding/override controls are removed from subtitle text and direction is set per run.
- **D-017 Block model.** A block is one or more consecutive cues merged by the normaliser (sentence-ending
  punctuation, maximum gap, maximum duration and maximum characters; the thresholds are settings
  defaults). This is the unit for block repeat, block navigation and AI context. ASSUMPTION.
- **D-018 Translation.** ML Kit on-device Translation. It downloads its own models, and we never use
  extracted model files (brief, rights rule 2). No online fallback in NOW (optional per the brief).
- **D-019 Dictionary details.** NOW: the details overlay opens a Google Translate page in a Custom Tab
  (online). The offline dictionary option is shown disabled and labelled Coming soon. LATER-3.
- **D-021 AI providers.** Gemini (`generateContent`, header auth), OpenAI (Chat Completions, bearer),
  Anthropic (Messages API, `x-api-key`, `anthropic-version`). Model IDs are settings, so they can change
  without a release. ASSUMPTION.
- **D-022 Navigation.** Navigation Compose with string routes (no serialization codegen). ASSUMPTION.
- **D-023 Quality tooling.** Android Lint (`abortOnError`) plus unit tests. Detekt/ktlint were skipped to
  limit configuration risk. ASSUMPTION.
- **D-024 CI.** GitHub Actions with major-version action tags. The Gradle wrapper is validated with
  `gradle/actions/wrapper-validation`. ASSUMPTION.
- **D-026 Localisation.** Per-app language through AppCompat (`AppCompatDelegate.setApplicationLocales`),
  which works on Android 12. EN is the default; FA is complete. ASSUMPTION.
- **D-028 Persian font.** Device fonts for NOW. Bundling Vazirmatn (OFL-1.1) is considered for a later
  phase, once the license file is vendored. ASSUMPTION.
- **D-030 Stretch scope.** The brief's scope switch is respected: no LATER item is implemented before NOW
  is green.

## Additions made during the build (phase 0–1)

- **D-031 Build verification by annotations.** Failing CI runs are summarised as check-run annotations by
  `.github/scripts/report_failures.py` because raw log downloads are not always available. *ASSUMPTION*
- **D-032 Explicit project paths.** Modules use `project(":core:model")` instead of type-safe accessors. The
  accessors did not resolve in this layout, and the explicit form is equally standard. *ASSUMPTION*
- **D-033 Pinned UI stack.** Compose BOM 2026.06.01 and Activity 1.11.0, the versions the reference project builds
  with on AGP 9.3.2 / compileSdk 36. Newer BOM/Activity releases raised the AAR minCompileSdk check. *ASSUMPTION*
- **D-034 Tap counting.** One tap = word, two = line, three or more = block, in a 320 ms window. *ASSUMPTION*
- **D-035 Shadowing tap and hold.** Tap runs a repeat session with the configured count (default 2). Hold runs
  auto-repeat until tapped again. *ASSUMPTION*
- **D-036 Learning-level default-known set.** Manual level A1/A2 treats the authored core function words as known;
  B1 and above use only user marks (`CoreWordLevelProvider`). *ASSUMPTION*, see AR-003.
- **D-037 Release signing.** Release APKs use the debug signing config until a release keystore is owned by the
  maintainer (D-025 extended). *ASSUMPTION*
- **D-038 Honest controls.** A setting or button is shown as working only when the code applies it. Otherwise it is
  labelled "pending" in Settings and listed in KNOWN_ISSUES.
- **D-039 Media3 unstable APIs.** Media3 codec and renderer APIs (`MediaCodecUtil`, `MediaCodecSelector`, the renderers
  factory) are `@UnstableApi`. The decoder modes need them, so they are used on purpose with declaration-level opt-in.
  Lint's `UnsafeOptInUsageError` is a warning in `core:player` and `feature:player` (visible, not build-breaking). *ASSUMPTION*
- **D-040 CI annotation limit.** GitHub keeps at most 10 error annotations per step, so `report_failures.py` writes at
  most five annotations per step (lint grouped by rule and file, compile errors, test failures, build summary). *Fact*, verified in CI.
- **D-041 Pinned AndroidX versions for compileSdk 36.** navigation-compose 2.8.9 and AppCompat 1.7.1 are pinned; newer
  releases declare a higher minimum compile SDK and fail `checkAarMetadata`. Reassess when compileSdk moves to 37. *ASSUMPTION*
