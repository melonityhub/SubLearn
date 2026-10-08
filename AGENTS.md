# AGENTS.md — start here

This is the single entry point for people and coding agents working on SubLearn.
`CLAUDE.md` and `.github/copilot-instructions.md` point here.

## What this repo is

SubLearn is a free, open-source, ad-free Android video player for learning English from dual subtitles.
Read [docs/PROJECT_BRIEF.md](docs/PROJECT_BRIEF.md) for the one-page goal and
[docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md) for the ID-numbered requirements (GEN, APP, PLY, SUB, SHD, LRN, AI, ENG, OTH, LATER).

## Map of the docs

| File | Read it when |
|------|--------------|
| [docs/PROJECT_BRIEF.md](docs/PROJECT_BRIEF.md) | You need the goal, audience and scope in one page |
| [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md) | You implement or review a feature (the spec IDs) |
| [docs/PHASES.md](docs/PHASES.md) | You plan work or need the phase order |
| [docs/CHECKLIST.md](docs/CHECKLIST.md) | You need the status and verification of every spec ID |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | You add a module, move code, or wire a dependency |
| [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md) | You touch colours, fonts, spacing, motion or RTL |
| [docs/DECISIONS.md](docs/DECISIONS.md) | You wonder why something is the way it is (D-numbers) |
| [docs/EXTENSION_POINTS.md](docs/EXTENSION_POINTS.md) | You add a LATER feature, a provider, or a language |
| [docs/REFERENCES.md](docs/REFERENCES.md) | You want to know what was learned from which project |
| [docs/AGENT_REQUESTS.md](docs/AGENT_REQUESTS.md) | You need to know which blockers and rights questions are open |
| [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) | Something does not work yet (honest list) |
| [docs/PROGRESS.md](docs/PROGRESS.md) | You need the current state, how to build, run and test, and the final report |
| [CHANGELOG.md](CHANGELOG.md) | You publish a release |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | You add a dependency or reuse code (licence audit) |

## Rules that always apply

1. **Verify through CI.** The sandbox cannot reach Google Maven, so `./gradlew` only runs in GitHub Actions
   (D-008). Push, read the result with `gh run view`, and use the annotations written by
   `.github/scripts/report_failures.py` when logs are unavailable.
2. **Never fake success.** No demo data, no no-op controls, no silent TODOs. A feature that is not built is
   a LATER stub with a disabled "Coming soon" entry, or it is listed in KNOWN_ISSUES.
3. **Spec IDs.** Every change names the spec IDs it implements. Update [CHECKLIST.md](docs/CHECKLIST.md) in the
   same change.
4. **Docs with code.** Each PR updates the docs it affects (at minimum CHECKLIST, PROGRESS, DECISIONS).
5. **Licences.** Never copy GPL code. MIT reuse needs an entry in THIRD_PARTY_NOTICES. Translation uses only
   the official ML Kit API; never extract model files (see AGENT_REQUESTS AR-002, AR-003, AR-004).
6. **Secrets.** API keys are stored only through `SecretStore` (Keystore-encrypted). Never log them, never
   put them in the settings document or the repo.
7. **Strings and tokens.** All user-visible text goes in `res/values` and `res/values-fa`. Colours, sizes,
   radii and durations come from `core:design` tokens only.
8. **Branch.** Work on `arena/9fe4f214-sublearn` only (D-027). Open PRs into `main`.

## Module map (short)

- `app` — Application, Koin graph, activity, navigation, tabs, side menu.
- `core:model` (JVM) — Cue, Block, Token, formulas, shadowing repeat controller, word level provider.
- `core:subtitle` (JVM) — SRT/VTT/ASS parsers, charset decoding, normaliser, blocks, cue index, tokenizer.
- `core:settings` (JVM) — typed versioned settings, JSON codec, migrations, store interface.
- `core:ai` (JVM) — Gemini, OpenAI and Anthropic providers over OkHttp; context and prompt builders.
- `core:later` (JVM) — LATER interfaces, NotImplemented stubs and feature flags.
- `core:design` — tokens, themes, font resolver, glass panel, coming-soon badge.
- `core:data` — Room (recents, playback state, My Words with FTS4), DataStore settings, Keystore secrets.
- `core:player` — `PlayerController`, Media3 implementation, decoder selectors, `FakePlayerController`.
- `core:translation` — `TranslationProvider` with ML Kit on-device translation and an LRU cache.
- `feature:home`, `feature:player`, `feature:words`, `feature:settings` — Compose screens and ViewModels.

## Commands

```bash
./gradlew test              # JVM and Robolectric unit tests (CI)
./gradlew lint assembleDebug
./gradlew :core:subtitle:test   # one module
```

Build, run and test steps for people are in [docs/PROGRESS.md](docs/PROGRESS.md#how-to-build-run-and-test).
