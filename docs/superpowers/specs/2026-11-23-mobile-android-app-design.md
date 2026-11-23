# Design Document: Standalone Native Android Currency Converter

**Date:** 2026-11-23  
**Status:** Draft for Review  
**Branch:** `feature/mobile`  
**Target:** Android Application (`android/` project directory)  

---

## 1. Intent & Context

The project is an existing, highly functional web-based currency and crypto converter with 50+ fiat currencies and top cryptocurrencies, custom exchange rates, multi-currency live calculations, and theme/i18n support.

The user wants a **standalone, lightweight native Android application** written in a modern native stack (Kotlin + Jetpack Compose) running locally on their physical phone. It must feature:
- An animated, concise onboarding flow upon first launch (2-3 slides) combining feature showcase and initial currency setup.
- Full feature parity with the web version (real-time rates, custom rates, reordering, dark/light theme, RU/EN language).
- Image export / sharing via native Android sharing sheet.
- Clean project structure in `android/` with complete Gradle files, resources, and source code.
- Strict constraint: **No downloading or background tool execution** (no Gradle downloads, no Android Studio downloads, no emulator runs) in this environment. Clear instructions for the user to download Android Studio and run/build the app themselves.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
- **Native Experience:** Pure Kotlin + Jetpack Compose with Material 3 design, smooth 60/120fps animations, native keyboard handling, haptic feedback, and modern edge-to-edge support.
- **Animated Onboarding (Hybrid Options A + B):**
  - **Slide 1:** Welcome & real-time fiat and crypto exchange rates with animated graphics/icons.
  - **Slide 2:** Custom rates, offline cache, card reordering with smooth interactive preview.
  - **Slide 3:** Interactive setup screen right inside the onboarding: choose source currency and initial target currencies, then tap "Get Started 🚀" to transition directly to the main screen.
- **Converter Features:**
  - Multi-currency live calculations from 1 source currency to $N$ target currencies.
  - Custom exchange rate modal: reciprocal inputs (1 USD = X RUB and 1 RUB = Y USD), market rate reference, "Use Market" and "Reset" buttons, live calculation preview for current amount.
  - Reordering of currency cards (drag to reorder / move up/down) and quick deletion.
  - Currency search and categorization: Popular, CIS & Neighbours, Other Fiat, Cryptocurrencies.
  - Dual language support (Russian and English), dynamically switchable in Settings.
  - Dark and Light theme with automatic system default and manual override.
  - Tip author action ("Купить автору кофе" opening external link).
- **Export / Share:**
  - Render current conversion summary card to an image (`Bitmap`) and trigger Android's native `Intent.ACTION_SEND` (share via Telegram, WhatsApp, Save to Gallery, etc.).
- **Offline First:**
  - Cached exchange rates and user preferences stored via Jetpack DataStore / Room/File cache. The app functions completely without internet using cached rates.
- **Self-Contained Android Project:**
  - Complete Gradle Kotlin DSL project (`settings.gradle.kts`, `build.gradle.kts`, `libs.versions.toml`, `gradle-wrapper.properties`).
  - Detailed, beginner-friendly Markdown guide `android/README.md` and in-chat instructions on how to install Android Studio, open the project, and run it on a smartphone via USB or build a release/debug APK.

### Non-Goals
- Publishing to Google Play Store / automated CI/CD pipeline (can be added later).
- Heavy third-party ad networks or tracking libraries.
- Running Gradle builds or emulator instances inside the agent container.

### Anti-Goals
- **No WebView / Hybrid wrapper:** Must not be a webview embedding `index.html`. It must be a genuine native Android app.
- **No placeholder stubs:** All calculations, currencies, flags, networking, and dialogs must be fully implemented.

---

## 3. Approaches Considered

| Approach | Pros | Cons | Verdict |
| :--- | :--- | :--- | :--- |
| **1. WebView / Capacitor / Cordova** | Quick to wrap existing HTML/JS | Sluggish animations, non-native input feel, poor offline mobile lifecycle, user explicitly rejected hybrid wrappers | **Rejected** |
| **2. Flutter / React Native** | Cross-platform | Heavy runtime, requires extra non-Kotlin toolchains and SDKs, not native Kotlin | **Rejected** |
| **3. Kotlin + Jetpack Compose (Material 3)** | 100% native, lightweight, modern Android standard, declarative reactive UI, rich animation APIs, clean MVVM, full control over device capabilities | Requires complete Kotlin implementation | **Selected** |

---

## 4. Architecture & Technical Specification

### 4.1 Project Layout
Located under `android/`:
```text
android/
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md                          # User instructions for Android Studio & APK
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/worldprices/converter/
            │   ├── MainActivity.kt
            │   ├── ConverterApp.kt
            │   ├── data/
            │   │   ├── model/
            │   │   │   ├── Currency.kt
            │   │   │   ├── ExchangeRates.kt
            │   │   │   └── UserPreferences.kt
            │   │   ├── local/
            │   │   │   ├── CurrencyDatabase.kt
            │   │   │   └── PreferencesDataStore.kt
            │   │   ├── remote/
            │   │   │   ├── ExchangeRateApi.kt
            │   │   │   └── BinanceApi.kt
            │   │   └── repository/
            │   │       └── CurrencyRepository.kt
            │   ├── ui/
            │   │   ├── theme/
            │   │   │   ├── Color.kt
            │   │   │   ├── Theme.kt
            │   │   │   └── Type.kt
            │   │   ├── components/
            │   │   │   ├── CurrencyFlag.kt
            │   │   │   ├── CurrencyCard.kt
            │   │   │   ├── AmountInputField.kt
            │   │   │   └── AppHeader.kt
            │   │   ├── dialogs/
            │   │   │   ├── CustomRateDialog.kt
            │   │   │   ├── CurrencyPickerSheet.kt
            │   │   │   └── SettingsSheet.kt
            │   │   ├── onboarding/
            │   │   │   ├── OnboardingScreen.kt
            │   │   │   └── OnboardingPages.kt
            │   │   ├── main/
            │   │   │   ├── MainScreen.kt
            │   │   │   └── MainViewModel.kt
            │   │   └── utils/
            │   │       ├── ShareHelper.kt
            │   │       └── Formatters.kt
            └── res/
                ├── drawable/          # App icons & vector flag assets
                ├── values/            # strings.xml, colors.xml
                └── values-ru/         # Russian translations
```

### 4.2 Data & Networking Layer
- **Fiat API:** `https://open.er-api.com/v6/latest/{BASE}` (hourly spot rates, unlimited, free).
- **Crypto API:** `https://api.binance.com/api/v3/ticker/price` (Binance public spot tickers for BTC, ETH, SOL, TON, etc.).
- **Offline Caching:**
  - In-memory cache with 15-minute validity timestamp.
  - Persistent fallback cache in DataStore: if offline, the app uses the latest stored rates and displays an indicator: *"Курсы на [дата] (офлайн)"*.
- **Currency Registry:** Complete parity with the web version:
  - 50+ fiat currencies (RUB, USD, EUR, GBP, CNY, TRY, BYN, KZT, UZS, GEL, AMD, etc.)
  - 12 top cryptos (BTC, ETH, USDT, BNB, XRP, SOL, ADA, DOGE, DOT, LTC, TON, TRX)
  - Categories: `Popular`, `CIS & Neighbours`, `Other Fiat`, `Crypto`.

### 4.3 UI & Animations
- **Onboarding Flow:**
  - `HorizontalPager` with 3 pages.
  - Page 1: Animated currency globe / ticker animation with floating crypto & fiat icons.
  - Page 2: Animated custom exchange rate toggle & card reorder graphic.
  - Page 3: Instant setup: select base currency and select initial target currencies with live search and quick chips, ending with a prominent animated "Get Started 🚀" button.
  - Pager dots with dynamic width transition (`animateDpAsState`).
- **Main Screen:**
  - Top app bar: Logo, Refresh button (with rotating animation during fetch), Theme toggle (sun/moon animation), Share button, Settings button.
  - Source currency card with flag, code, drop-down arrow, and large formatted input field.
  - Target currency cards:
    - Left: Flag, currency code, localized full name, reciprocal unit rate ($1\text{ USD} = 96.50\text{ RUB}$).
    - Right: Converted total amount in bold with currency symbol.
    - Custom rate indicator: "Свой" / "Custom" badge with tap-to-edit.
    - Long-press / reorder mode or reorder handles.
- **Custom Rate Dialog:**
  - Bidirectional editing: changes in Target-to-Source automatically update Source-to-Target and vice-versa.
  - Shows current market rate with a one-tap "Use Market" button.
  - Real-time preview showing calculation for the currently entered amount.
  - "Reset" button to revert to live market rate.
- **Export to Image:**
  - Uses Android `Picture` / Canvas recording from Compose or native view snapshot to render a high-resolution summary card with title, date, source amount, and all converted values.
  - Writes PNG to app's cache directory and fires `Intent.ACTION_SEND` with `FileProvider` URI.

---

## 5. User Instructions Plan (Android Studio & Phone Run)
A step-by-step Russian guide will be provided:
1. Installing Android Studio (free from developer.android.com).
2. Opening the `android` folder in Android Studio.
3. Gradle Sync (automatic one-click).
4. Running directly on phone via USB Debugging (Developer Options) or Wireless Debugging.
5. Alternatively, generating a standalone `.apk` via `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`.

---

## 6. Review & Verification Checklist
- [x] Branch `feature/mobile` created.
- [ ] Complete Android project structure in `android/`.
- [ ] All 50+ fiat currencies and 12 cryptos ported to Kotlin data structures.
- [ ] Onboarding with 3 animated slides (features + interactive currency setup).
- [ ] Full converter functionality with custom rate modal and reciprocal updates.
- [ ] Local persistence via DataStore Preferences.
- [ ] Share/Export functionality via FileProvider.
- [ ] Bilingual strings (Russian & English).
- [ ] Clear Android Studio instruction document in `android/README.md`.
