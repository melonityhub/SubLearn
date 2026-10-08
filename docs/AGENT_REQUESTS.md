# Agent requests — blockers, rights questions, open items for the owner

Format: ID · phase/task · what was needed · what was tried and the result · suspected cause · next step · severity.

## AR-001 · Phase 0 · Local Android build is impossible in the sandbox

- **Needed:** `./gradlew assembleDebug testDebugUnitTest lint` run locally.
- **Tried:** services.gradle.org, dl.google.com, maven.google.com, repo1.maven.org, plugins.gradle.org,
  objects.githubusercontent.com, deb.debian.org — all unreachable (HTTP 000). Only github.com, api.github.com,
  codeload.github.com, registry.npmjs.org, pypi.org and files.pythonhosted.org respond. No JDK or Android SDK is installed.
- **Suspected cause:** sandbox egress allowlist.
- **Next step / workaround:** CI on GitHub-hosted runners is the gate (see D-008). Versions were checked
  with the page fetcher against Google Maven and Maven Central metadata. Each change is pushed and the
  workflow log is read with `gh run view --log-failed`.
- **Severity:** medium (slows iteration; does not block delivery).

## AR-002 · Rights question · dictionaryproject (owner's repo) provenance

- **Needed:** owner decision on whether `melonityhub/dictionaryproject` may be used as a design reference.
- **Facts:** its README says the SQL schema, queries, sorting and category logic were taken from the Java
  source of a third-party Android dictionary app, and that the `.bipe` translation loader was
  reverse-engineered from model files taken from that app. The repo has no licence.
- **What SubLearn does:** no code, queries, schema text or model files are used. The section split is the
  only idea taken, and the schema is our own design for LATER-3. Translation uses the official Google ML
  Kit Translation API (it downloads its own models).
- **Next step:** owner confirms the rights position for the reference repo, and whether a licence should be added.
- **Severity:** high for the owner's dictionary project; low for SubLearn (not used).

## AR-003 · Permissive word-frequency / CEFR list not verified

- **Needed:** a permissively licensed frequency or CEFR list for LRN-2 default levels.
- **Tried:** `first20hours/google-10000-english` → LDC restrictions for commercial use (rejected).
  `hermitdave/FrequencyWords` → GitHub says MIT, but the data derives from OpenSubtitles, provenance not
  verified (not bundled). `openlanguageprofiles/olp-en-cefrj` → no licence (rejected).
- **Result:** default provider uses an authored list of core function words (Apache-2.0, ours) for the A1-A2 default-known set, plus user-marked known words (D-020).
- **Next step:** owner decides whether to accept CC-BY-SA or verify a list; then add a `WordLevelProvider` implementation (no interface change).
- **Severity:** medium (LRN-2 quality).

## AR-004 · Google ML Kit licence / terms

- **Facts:** the ML Kit Translation SDK is a proprietary Google SDK under Google's terms (not an OSS licence).
  The model is downloaded by ML Kit. The app remains Apache-2.0 for its own code.
- **Impact:** the app cannot be listed on F-Droid as a fully free build. Not a NOW goal.
- **Next step:** owner confirms acceptance. An offline translation alternative (e.g. Bergamot) is LATER.
- **Severity:** low.

## AR-005 · Branch naming conflicts with session rules

- **Requested:** phase branches `phase/N-name`.
- **Constraint:** all work stays on `arena/9fe4f214-sublearn`; other branches are not created.
- **Resolution:** `phase N:` commit prefixes plus PROGRESS sections (D-027).
- **Severity:** low. Owner may confirm or change this.

## AR-006 · v0.1.0 tag and release

- **Requested:** v0.1.0 tagged with a debug APK artifact.
- **Plan:** the release workflow runs on a `v*` tag (D-025). Tag creation is a repository action the owner
  should confirm before it is pushed.
- **Severity:** low.

## AR-007 · Embedded dual-layer subtitles

- **Limitation:** Media3 decodes one text track per player, so two embedded tracks cannot feed the two
  layers at the same time (D-013).
- **Options:** (a) a second, muted decode path for the Translation layer; (b) extract the embedded track
  with `MediaExtractor` and parse it ourselves.
- **Next step:** NOW ships option "Learning layer may use the embedded track". Implement (a) or (b) in a
  later phase.
- **Severity:** medium (PLY-7 partial).

## AR-008 · Persian rendering depends on device fonts

- **Facts:** NOW uses system fonts (D-028). Some Persian glyph shaping may differ by device.
- **Next step:** consider bundling Vazirmatn (OFL-1.1) after licence vendoring.
- **Severity:** low.

## AR-009 · CI · Raw job logs are not downloadable from the sandbox

- **Facts:** `gh run view --log-failed` and the job-logs API failed with EOF / blob-storage errors. Check-run annotations
  are readable.
- **Workaround:** `.github/scripts/report_failures.py` writes Gradle, compile, lint and test failures as annotations
  (max five per step, D-040).
- **Severity:** low (workaround in place).

## AR-010 · Build · AndroidX releases that require compileSdk 37

- **Facts:** the newest navigation-compose (2.10.2) and AppCompat (1.8.0) releases failed `checkAarMetadata` with
  compileSdk 36. The pins in `gradle/libs.versions.toml` (navigation 2.8.9, AppCompat 1.7.1, Compose BOM 2026.06.01,
  Activity 1.11.0) build with compileSdk 36 (D-041).
- **Next step:** when the owner moves compileSdk to 37, refresh these pins and re-run CI. The owner may prefer that.
- **Severity:** low (pins are documented and CI is green).

## AR-006 (status update) · Tag v0.1.0

- **Status:** not tagged. The NOW scope is not complete (Phases 6–9 are open, see CHECKLIST), so the tag waits until the
  owner decides it. `release.yml` is ready and publishes the debug APK when a `v*` tag is pushed.
