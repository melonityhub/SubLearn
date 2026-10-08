# Changelog

All notable changes are listed here. The format follows [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added
- Project foundations: Gradle multi-module build with a version catalog and convention plugins; CI that
  runs unit tests, lint and builds the debug APK; a release workflow for tags.
- Subtitle engine: SRT, WebVTT and ASS/SSA parsers, charset decoding, normaliser, block builder, cue
  splitter, O(log n) cue index, tokenizer, batch tools (remove line breaks, max characters, search).
- Typed, versioned settings with JSON export and import and migrations.
- Shadowing formulas and repeat controller (unit tested).
- AI providers (Gemini, OpenAI, Anthropic) with a context builder and prompt renderer (unit tested with
  a mock server).
- Room database for recent videos, playback state and My Words (FTS4), DataStore settings, Keystore-
  encrypted API keys.
- Media3 player with decoder mapping (SW / HW / HW+) and a fake player for tests.
- ML Kit on-device translation behind `TranslationProvider`.
- Player screen with two subtitle layers, tap-translate, repeat controls, subtitle list and gestures.
- Home, My Words, Settings and coming-soon sections; EN and FA resources.
