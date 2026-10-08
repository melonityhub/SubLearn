# Project brief

**SubLearn** is a free, open-source, ad-free Android video player for learning English from dual subtitles.

- **Who:** adult learners who watch films and series to improve English, with Persian as their native language.
- **Core idea:** two subtitle layers at once (English and translation), tap any word to translate or save it,
  repeat a line to practise speaking (shadowing), and keep a list of words.
- **Defaults:** app UI English; learning language English; translation language Persian (right-to-left).
  Other languages are not built yet; the structure allows them (EXTENSION_POINTS.md).
- **Platform:** Android 12 (API 31) and newer. The target device is a POCO X3 Pro.
- **Delivery:** GitHub builds the debug APK on every change and publishes releases with the APK.
- **Principles:** offline after one-time model download (GEN-7); no demo data; no non-working controls;
  permissive licensing (Apache-2.0); no GPL code; official ML Kit only for translation.
- **Scope:** NOW items are real and tested; LATER items are stubs with flags and "Coming soon" entries.
  The full list is in PRODUCT_SPEC.md.
