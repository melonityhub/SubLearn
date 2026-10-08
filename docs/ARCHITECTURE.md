# Architecture

## Layers

```
app  (activity, navigation, DI graph)
 ├─ feature:player   feature:home   feature:words   feature:settings     (Compose screens + ViewModels)
 │
 ├─ core:design      (tokens, theme, font resolver, components)           (Android library)
 ├─ core:data        (Room, DataStore settings store, Keystore secrets)   (Android library)
 ├─ core:player      (PlayerController, Media3 impl, FakePlayerController) (Android library)
 ├─ core:translation (TranslationProvider, ML Kit impl, LRU cache)        (Android library)
 │
 ├─ core:ai          (AiProvider impls, context and prompt builders)      (pure JVM)
 ├─ core:subtitle    (parsers, normaliser, blocks, cue index, tokenizer)  (pure JVM)
 ├─ core:settings    (typed schema, codec, migrations, store interface)  (pure JVM)
 ├─ core:later       (LATER interfaces, stubs, feature flags)            (pure JVM)
 └─ core:model       (Cue, Block, Token, formulas, repeat, word level)    (pure JVM)
```

Rules:

- Pure JVM modules have no Android imports, so their tests run fast with `./gradlew test` on any JDK 17.
- Feature modules depend on core modules, never on each other (except tests that use the player fixtures).
- Every external capability is behind an interface: `PlayerController`, `TranslationProvider`,
  `AiProvider`, `SettingsStore`, `DictionaryProvider`, `WordAnalyzer`, `WordLevelProvider`, `SpeechToText`,
  `UpdateChecker`, `VideoSourceResolver`, `QuizEngine`, `LevelDetector`, `SubtitleReSegmenter`.
- Dependency injection is Koin (D-007). The graph is in `app/.../di/AppModule.kt`.

## Key flows

**Opening a video.** Home (SAF picker) or an external intent (`ACTION_VIEW`) produces a `MediaSource`.
The navigation route `player/{uri}/{title}` opens `PlayerScreen`. `PlayerViewModel` sets the decoder mode,
reads the saved position (`PlaybackStateRepository`), loads the media into `Media3PlayerController`, and
records the video as recent.

**Subtitle pipeline.** Bytes → `SubtitleDecoder` (BOM, strict UTF-8, then the legacy charset) →
`SubtitleParsers.parseAuto` (format from content, then extension) → `Cue` list → `BlockBuilder` (merge into
blocks) → `SubtitleTrack` (cues, blocks, `CueIndex`). Per frame, `CueIndex.activeAt(position - delay)` gives
the line, in O(log n). This work runs on `Dispatchers.IO`.

**Tap-translate.** The overlay (`TappableLine`) maps a tap to a char offset through `TextLayoutResult`. `Tokenizer`
gives the word. `TapSequencer` counts taps in a 320 ms window: 1 = word, 2 = line, 3 = block. The ViewModel
pauses playback if the setting says so, asks `TranslationProvider` (ML Kit, cached), and shows the card.
Dismissing the card resumes playback only if it paused it.

**Shadowing.** `RepeatController` (pure) decides, when a block ends, whether to replay now, replay after a pause
(`ShadowingFormula` with D, M, N), or finish. The ViewModel seeks and plays from the block start.

**Settings.** `SettingsStore` persists one versioned JSON document in DataStore. Every write goes through
`SettingsCodec`, so export, import and storage use the same schema and migrations. API keys are not part of
the document.

**Secrets.** `SecretStore` encrypts each API key with an AES-256-GCM key held in the Android Keystore and stores
only the ciphertext in DataStore.

**Persistence.** Room (`sublearn.db`): recent videos, playback state per video, and My Words with an FTS4
index (`my_words_fts`, content table `my_words`). Search uses quoted prefix queries built by the repository.

## Threading

- Parsing and file IO: `Dispatchers.IO`.
- ExoPlayer: main thread (its requirement). Position polling runs in a coroutine every 200 ms while playing.
- ML Kit: its `Task` results are awaited with `kotlinx.coroutines.tasks.await`.

## Testing layers

| Layer | Where | Tooling |
|-------|-------|---------|
| Pure logic | `core:model`, `core:subtitle`, `core:settings`, `core:ai` | JUnit 4, MockWebServer |
| Android data | `core:data` | Robolectric + in-memory Room (FTS) |
| Android misc | `core:translation` | JUnit (cache) |
| UI | (to add) `feature:player` with `FakePlayerController` | Compose UI test + Robolectric |
