# SubLearn

**A free, open-source, ad-free Android video player for learning English from dual subtitles.**

Watch a film with two subtitle layers at once: English on the bottom and Persian (or another
translation) above it. Tap any word to translate it on the device, save it to *My Words*, and repeat
a sentence for shadowing. Everything in the NOW scope works offline after the translation model is
downloaded once.

> Status: pre-release (v0.1.0 in progress). See [docs/CHECKLIST.md](docs/CHECKLIST.md) for what is
> built, partial or planned, and [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) for what does not work yet.

## Features (NOW)

- Open local videos (Storage Access Framework), video links, or files from other apps.
- MX-style player: auto-hiding controls, gestures for brightness, volume and seek, orientation lock,
  picture-in-picture, decoder choice SW / HW / HW+.
- Two independent subtitle layers (learning and translation), SRT, WebVTT and ASS/SSA (text),
  Windows-1256 for legacy Persian subtitles.
- Tap a word (1 tap), a line (2) or a block (3) to translate it with Google ML Kit on the device.
- Shadowing: repeat the current block, auto-repeat on hold, pause between repeats from a formula, stop
  at the end of a block.
- My Words: save, search (full-text) and remove words; saved words are underlined in subtitles.
- Settings: typed and searchable, with JSON export and import (API keys are never exported).
- AI providers (Gemini, ChatGPT, Claude) through an interface, with keys stored in the Android Keystore.
- Light, dark and AMOLED themes; English and Persian (right-to-left) UI.

Later items (YouTube, PDF/browser learning, dictionary, level detection, quiz, update checker, AI
re-segmentation, speech-to-text, word analysis, on-device AI, more languages) are visible as
"Coming soon" and have stable extension points. See [docs/EXTENSION_POINTS.md](docs/EXTENSION_POINTS.md).

## Install

Download the latest debug APK from the [Releases page](../../releases) and install it on Android 12 or
newer. Allow installs from your browser or file manager when Android asks.

Debug builds are signed with the debug key. Updates install over earlier builds from the same CI
signing key. Uninstall first if you switch between a local build and a CI build.

## Build and test

```bash
./gradlew test lint assembleDebug
```

Requires JDK 17 and the Android SDK (platform 36, build-tools 36.0.0). CI runs the same commands on every
push and pull request. Details: [docs/PROGRESS.md](docs/PROGRESS.md#how-to-build-run-and-test).

## Documentation

Start at [AGENTS.md](AGENTS.md). The product spec is in [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md).
Architecture: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Decisions: [docs/DECISIONS.md](docs/DECISIONS.md).

## Licence

Apache-2.0. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Google ML Kit is
used under Google's terms and downloads its own translation models.
