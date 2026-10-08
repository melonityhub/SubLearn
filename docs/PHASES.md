# Phases — final plan (written before coding)

This plan refines the phases in the project brief. The order is kept. Each phase ends with CI green
(`./gradlew assembleDebug testDebugUnitTest lint`), updated docs and a CHECKLIST pass.

## Git workflow (decision D-027)

- The session is bound to branch `arena/9fe4f214-sublearn`. Phase work is committed on that branch
  with a `phase N:` commit prefix instead of separate `phase/N-name` branches.
- Each phase is a logical commit group and a section of [PROGRESS.md](PROGRESS.md).
- One PR (`arena/9fe4f214-sublearn` → `main`) carries the work. It is merged only when CI is green.
- Releases: a tag `v*` triggers the release workflow, which attaches the debug APK (see D-025).

## Verification model (decision D-008)

The sandbox cannot reach Google Maven, Maven Central or the Gradle distribution host, so local Gradle
builds are impossible here. GitHub-hosted runners can reach them. CI is therefore the verification gate:
push → read the workflow log (`gh run view --log-failed`) → fix → repeat. The same CI commands are
documented in [AGENTS.md](../AGENTS.md).

## Phases

| # | Phase | Goal | Exit criteria |
|---|-------|------|---------------|
| 0 | Foundations | Repo audit, reference study, licence audit, docs skeleton, Gradle modules, CI, design tokens + motion specs, typed settings, EN/FA + RTL base, LATER stubs | Docs complete; CI green; settings tests pass |
| 1 | App shell | Theme, navigation (tabs + side menu), settings screens, Home with recent videos (Room), open video / URL intents | Intent opens player; Home lists recents; settings persist |
| 2 | Player core | Media3 local + URL playback, MX-style overlay with auto-hide, gestures, orientation + lock, decoder option, track selection, PiP, aspect ratio, speed, lock | Player overlay Compose tests pass with FakePlayer; manual checklist in PROGRESS |
| 3 | Subtitle engine | SRT/VTT/ASS parsers, normalizer, two layers, multi-track, delay, styling, Subtitle List View, layout mode, toggle buttons, dockable quick actions | Parser + normalizer unit tests; overlay tests |
| 4 | Interaction | Tap-translate (word/line/block), pause/resume rule, ML Kit `TranslationProvider`, entertainment cards, My Words (Room) + screen | Translation provider tests (fake); My Words repository tests |
| 5 | Shadowing | Repeat block, auto-repeat, stop at end of block, formulas and settings | Formula + planner unit tests |
| 6 | Learning mode | Popup pipeline, `WordLevelProvider`, word-colouring styles | Pipeline unit tests |
| 7 | AI button | Providers, settings, prompt editor, context builder, loading and pause/resume | Provider tests with MockWebServer; context builder tests |
| 8 | Subtitle tools | Batch line-break removal, max-char split, search, no-spoiler mode | Tool unit tests |
| 9 | Hardening | Animation polish, accessibility, performance, process death, test gaps, README with screenshots, release v0.1.0 | CI green; APK artifact; release notes |

## Stretch work (only after all NOW work is green and merged, in this order, one at a time)

1. Offline dictionary import + lookup (LATER-3).
2. My Words quiz (LATER-5).
3. Update checker via GitHub Releases (LATER-6). Stop after this item.

## Status

Live status is in [CHECKLIST.md](CHECKLIST.md) and [PROGRESS.md](PROGRESS.md).
