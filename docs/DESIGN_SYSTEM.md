# Design system

## Direction (GEN-1)

SubLearn should feel calm, modern and focused on the video. Controls float over the picture and fade away.
Subtitles sit on translucent dark panels with a thin border. The highlight colour is amber, so the current
line and saved words stand out without shouting.

The palette and card language were re-implemented from the visual idea of proudvocab (dark translucent
panels, 12 px panel radius, amber highlight, light text ramp). No code or assets were copied (REFERENCES.md).

## Tokens

All values live in `core:design/SublearnTokens.kt`. Feature code must not hard-code colours, sizes,
radii, durations or fonts (ENG-11).

| Group | Tokens |
|-------|--------|
| Dark | `DarkBackground` `#0E1322`, `DarkPanel` `#131A2C`, `DarkCard` `#1B2438`, `DarkBorder` `#2B3752`, `DarkOnSurface` `#E8ECF7`, `DarkOnSurfaceMuted` `#9AA6C4` |
| AMOLED | `AmoledBackground` `#000000` (surfaces stay panel-coloured) |
| Light | `LightBackground` `#F6F7FB`, `LightPanel` `#FFFFFF`, `LightBorder` `#D6DCEB`, `LightOnSurface` `#1A2236`, `LightOnSurfaceMuted` `#55607C` |
| Accent | `Accent` `#FFC878`, `AccentSoft` 20 % alpha, `AccentStrongLight` `#9A5B00` (contrast on light) |
| Radius | small 8 dp, medium 12 dp, large 20 dp |
| Spacing | 4, 8, 12, 16, 24 dp |
| Touch target | 48 dp minimum (`MinTouchTarget`) |
| Motion | fast 150 ms, medium 260 ms, slow 420 ms; standard easing `cubic-bezier(0.2, 0, 0, 1)`; popup rise 900 ms |

## Themes (ThemeMode)

`SYSTEM` follows the device, `LIGHT`, `DARK`, and `AMOLED` (dark with a pure black background). The theme is
applied in `SublearnTheme`.

## Fonts per role and surface (GEN-3)

`FontSettings` stores one `TextStyleSpec` per (surface, language role). `FontResolver` converts it to a
Compose `TextStyle`. Defaults:

- Learning role (English) uses sans-serif by default; native role (Persian) uses the system font, which
  handles Persian shaping on the device.
- Sizes and weights differ per surface (for example learning subtitles 20 sp / 600, translation subtitles 16 sp / 500).

Surfaces wired today: learning subtitles, translation subtitles, translation popups. Word cards, app menus and
AI answers are listed in KNOWN_ISSUES until their screens exist. Settings never bleed between surfaces: a
test checks that changing one (surface, role) pair leaves the others unchanged.

## Right-to-left and bidi (GEN-4)

- Subtitle text uses `TextDirection.Content`, so each paragraph follows its own first strong character:
  Persian lines are right-to-left and English lines left-to-right.
- Word hit-testing uses logical offsets from `TextLayoutResult`, so taps land on the right word in RTL text.
- Bidi embedding and override controls are stripped from subtitle text (D-016); ZWNJ is kept.
- The app UI mirrors through the layout direction of the selected language (`AppCompatDelegate` per-app locale).

## Motion and accessibility

- Controls fade in and out (`AnimatedVisibility`) and auto-hide after the configured time (default 3 s).
- Touch targets are at least 48 dp, quick-action buttons 48 dp.
- Icons carry content descriptions; labels use string resources in English and Persian.
- Text scales with the system font size (sp everywhere).
- `reduceMotion` was removed from settings until animations are configurable (see KNOWN_ISSUES).

## Components (core:design)

- `GlassPanel`: translucent dark panel with a hairline border; used by the translation card.
- `ComingSoonBadge`: amber badge for disabled LATER entries.
- `SublearnTheme`: light, dark, AMOLED.
