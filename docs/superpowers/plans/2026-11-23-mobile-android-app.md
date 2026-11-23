# Mobile Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, lightweight, standalone native Android currency & crypto converter app in Kotlin + Jetpack Compose with animated 3-slide onboarding, custom rates, offline support, and native image sharing.

**Architecture:** Modern Clean Architecture / MVVM pattern using Jetpack Compose (Material 3), StateFlow, OkHttp with Kotlinx Serialization, and Preferences DataStore for offline persistence. UI layer cleanly observes reactive repository states.

**Tech Stack:** Kotlin 2.0+, Jetpack Compose (Material 3, Animations, Accompanist/Foundation Pager), AndroidX DataStore Preferences, OkHttp 4.12, Kotlinx Serialization, Kotlin Coroutines.

**Spec:** `docs/superpowers/specs/2026-11-23-mobile-android-app-design.md`

## Global Constraints

- Standalone project located completely inside `android/` directory.
- No Gradle downloads, Android Studio downloads, or emulator runs performed on the agent environment.
- 100% native Kotlin + Compose; no WebView wrapper or hybrid shells.
- Min Android SDK: 24 (Android 7.0 Nougat) for 99%+ device compatibility, Target SDK: 34 (Android 14).
- Offline-first: must open and calculate with cached rates when no internet connection is available.
- Bilingual support: Russian (`values-ru/strings.xml`) and English (`values/strings.xml`).

## Review Focus

1. **Zero / Empty / Non-numeric amount input:** When user types empty string, commas instead of dots, or invalid characters, parser must default gracefully to 0 or 1 without crashing.
2. **Offline network launch:** If launched with airplane mode / no Wi-Fi, the app must load cached rates from DataStore and display an offline badge without blocking the UI.
3. **Custom exchange rate zero or negative:** Entering 0.0 or negative values in custom rate inputs must be blocked or validated with an error message instead of causing division by zero ($NaN / \infty$).
4. **Reciprocal rate precision drift:** When typing in 1 USD = X RUB, the reciprocal 1 RUB = (1/X) USD must not enter an infinite reactive update loop between the two text fields.
5. **Image sharing without crash on scoped storage:** Saving the exported PNG must write to app-specific cache directory and use `FileProvider` so Android 10+ scoped storage permissions do not throw `FileUriExposedException`.

---

### Task 1: Android Project Scaffolding & Gradle Configuration

**Files:**
- Create: `android/settings.gradle.kts`
- Create: `android/build.gradle.kts`
- Create: `android/gradle.properties`
- Create: `android/gradle/wrapper/gradle-wrapper.properties`
- Create: `android/app/build.gradle.kts`
- Create: `android/app/proguard-rules.pro`
- Create: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/res/values/strings.xml`
- Create: `android/app/src/main/res/values-ru/strings.xml`
- Create: `android/app/src/main/res/values/colors.xml`
- Create: `android/app/src/main/res/xml/file_paths.xml`

**Interfaces:**
- Produces: Gradle project structure ready to open in Android Studio with package `com.worldprices.converter`, dependencies (Compose, OkHttp, Kotlinx Serialization, DataStore), permissions (`INTERNET`), and `FileProvider` for sharing.

- [ ] **Step 1: Create root Gradle files (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, wrapper)**
- [ ] **Step 2: Create `android/app/build.gradle.kts` with dependencies and Android configuration**
- [ ] **Step 3: Create `AndroidManifest.xml` declaring `MainActivity`, `INTERNET` permission, and `FileProvider`**
- [ ] **Step 4: Create XML resource files (strings in EN/RU, colors, and `file_paths.xml`)**
- [ ] **Step 5: Commit scaffolding**

```bash
git add android/
git commit -m "chore: scaffold native android project structure"
```

---

### Task 2: Data Models & Currencies Registry

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/data/model/Currency.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/data/model/ExchangeRates.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/data/model/UserPreferences.kt`

**Interfaces:**
- Produces:
  - `data class Currency(val code: String, val nameRu: String, val nameEn: String, val symbol: String, val countryCode: String?, val category: CurrencyCategory, val colorHex: String? = null)`
  - `enum class CurrencyCategory { POPULAR, CIS, FIAT, CRYPTO }`
  - `object CurrencyRegistry`: contains all 50+ fiat currencies and 12 cryptocurrencies ported from `js/app.js` with search helper `searchMatches(query: String, isRussian: Boolean)`.
  - `data class RatesResponse`: model for open.er-api.com and Binance responses.

- [ ] **Step 1: Implement `Currency.kt` and `CurrencyRegistry` containing all currencies from web app**
- [ ] **Step 2: Implement `ExchangeRates.kt` and `UserPreferences.kt` data structures**
- [ ] **Step 3: Commit currency data models**

```bash
git add android/app/src/main/java/com/worldprices/converter/data/model/
git commit -m "feat(data): add currency registry and data models"
```

---

### Task 3: Remote Network Client & Local DataStore Persistence

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/data/remote/ExchangeRateApi.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/data/remote/BinanceApi.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/data/local/PreferencesDataStore.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/data/repository/CurrencyRepository.kt`

**Interfaces:**
- Consumes: Models from Task 2.
- Produces:
  - `class CurrencyRepository(private val context: Context)`
  - Methods:
    - `val preferencesFlow: Flow<UserPreferences>`
    - `suspend fun getRates(base: String, forceRefresh: Boolean): Result<Map<String, Double>>`
    - `suspend fun setCustomRate(sourceCode: String, targetCode: String, rate: Double)`
    - `suspend fun resetCustomRate(sourceCode: String, targetCode: String)`
    - `suspend fun updateSourceCurrency(code: String)`
    - `suspend fun updateTargetCurrencies(codes: List<String>)`
    - `suspend fun setOnboardingCompleted(completed: Boolean)`
    - `suspend fun setThemeMode(mode: ThemeMode)`
    - `suspend fun setLanguage(lang: String)`

- [ ] **Step 1: Implement `ExchangeRateApi.kt` (fiat) and `BinanceApi.kt` (crypto) with OkHttp**
- [ ] **Step 2: Implement `PreferencesDataStore.kt` for saving favorites, custom rates, theme, onboarding state, and cached rates**
- [ ] **Step 3: Implement `CurrencyRepository.kt` coordinating network fetch, cache fallback, and custom rates**
- [ ] **Step 4: Commit data layer and repository**

```bash
git add android/app/src/main/java/com/worldprices/converter/data/
git commit -m "feat(data): implement network apis, datastore and repository"
```

---

### Task 4: UI Theme & Core Reusable Components

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/ui/theme/Color.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/theme/Theme.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/theme/Type.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/components/CurrencyFlag.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/components/AmountInputField.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/components/AppHeader.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/components/CurrencyCard.kt`

**Interfaces:**
- Consumes: Models from Task 2.
- Produces:
  - Material 3 theme supporting dark/light mode matching the web app styles.
  - `@Composable fun CurrencyFlag(currency: Currency, modifier: Modifier)`
  - `@Composable fun AmountInputField(value: String, onValueChange: (String) -> Unit)`
  - `@Composable fun CurrencyCard(currency: Currency, sourceAmount: Double, rate: Double, isCustomRate: Boolean, onEditRate: () -> Unit, onRemove: () -> Unit, onMoveUp: () -> Unit, onMoveDown: () -> Unit)`

- [ ] **Step 1: Implement Material 3 Theme (`Color.kt`, `Theme.kt`, `Type.kt`)**
- [ ] **Step 2: Implement `CurrencyFlag.kt` with SVG/vector flags and crypto symbol badges**
- [ ] **Step 3: Implement `AmountInputField.kt` and `AppHeader.kt`**
- [ ] **Step 4: Implement `CurrencyCard.kt` with animations, custom rate badge, and action handlers**
- [ ] **Step 5: Commit UI components**

```bash
git add android/app/src/main/java/com/worldprices/converter/ui/
git commit -m "feat(ui): add theme and core compose components"
```

---

### Task 5: Modal Dialogs (Custom Rate, Currency Picker, Settings)

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/ui/dialogs/CustomRateDialog.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/dialogs/CurrencyPickerSheet.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/dialogs/SettingsSheet.kt`

**Interfaces:**
- Consumes: Models and UI components from Tasks 2 & 4.
- Produces:
  - `@Composable fun CustomRateDialog(...)`: bidirectional reciprocal editing (Target to Source vs Source to Target), market rate hint, "Use Market" button, live preview calculation, and "Reset" button.
  - `@Composable fun CurrencyPickerSheet(...)`: search box, category tabs (Popular, CIS, Fiat, Crypto), single/multi-selection.
  - `@Composable fun SettingsSheet(...)`: language switch, theme switch, buy coffee tip link.

- [ ] **Step 1: Implement `CustomRateDialog.kt` with reciprocal calculation and live preview**
- [ ] **Step 2: Implement `CurrencyPickerSheet.kt` with search and categorized lists**
- [ ] **Step 3: Implement `SettingsSheet.kt` with language/theme controls and tip link**
- [ ] **Step 4: Commit dialogs and sheets**

```bash
git add android/app/src/main/java/com/worldprices/converter/ui/dialogs/
git commit -m "feat(ui): implement custom rate, picker and settings dialogs"
```

---

### Task 6: Animated Onboarding Screen (Hybrid Options A + B)

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/ui/onboarding/OnboardingScreen.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/onboarding/OnboardingPages.kt`

**Interfaces:**
- Consumes: `CurrencyRegistry`, `PreferencesDataStore`.
- Produces:
  - `@Composable fun OnboardingScreen(onFinish: (source: String, targets: List<String>) -> Unit)`
  - 3 Animated slides:
    1. Slide 1: Welcome & Real-time Rates (animated ticker & floating currency particles).
    2. Slide 2: Custom Rates & Flexible Reorder (interactive visual showcase).
    3. Slide 3: Quick Setup (select base currency, select initial target chips, tap "Get Started 🚀").
  - Animated pager indicator with expanding dot animations.

- [ ] **Step 1: Implement `OnboardingPages.kt` with animated canvas graphics and illustrations**
- [ ] **Step 2: Implement `OnboardingScreen.kt` with `HorizontalPager`, slide transitions, and setup controls**
- [ ] **Step 3: Commit onboarding flow**

```bash
git add android/app/src/main/java/com/worldprices/converter/ui/onboarding/
git commit -m "feat(ui): create animated 3-slide onboarding screen"
```

---

### Task 7: Main Screen, ViewModel & Native Image Sharing

**Files:**
- Create: `android/app/src/main/java/com/worldprices/converter/ui/utils/ShareHelper.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/main/MainViewModel.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/ui/main/MainScreen.kt`
- Create: `android/app/src/main/java/com/worldprices/converter/MainActivity.kt`

**Interfaces:**
- Consumes: All components from Tasks 2-6.
- Produces:
  - `MainViewModel`: manages `uiState`, rate refreshing, reordering, custom rate overrides, amount changes.
  - `ShareHelper`: creates a styled summary card Bitmap and fires `Intent.ACTION_SEND` with `FileProvider`.
  - `MainScreen`: integrates header, amount input, card list, dialogs, and offline notice.
  - `MainActivity`: root Compose activity switching between Onboarding and MainScreen based on DataStore.

- [ ] **Step 1: Implement `ShareHelper.kt` with styled bitmap generator and FileProvider share intent**
- [ ] **Step 2: Implement `MainViewModel.kt` with state management and currency calculations**
- [ ] **Step 3: Implement `MainScreen.kt` with reordering, pull-to-refresh, and dialog triggers**
- [ ] **Step 4: Implement `MainActivity.kt` with edge-to-edge Compose container**
- [ ] **Step 5: Commit main screen and activity**

```bash
git add android/app/src/main/java/com/worldprices/converter/
git commit -m "feat(app): implement main screen, viewmodel and native sharing"
```

---

### Task 8: User Documentation & Android Studio Guide

**Files:**
- Create: `android/README.md`

**Interfaces:**
- Produces: Complete, user-friendly step-by-step guide in Russian explaining:
  1. How to download and install Android Studio.
  2. How to open the `android` folder.
  3. Automatic Gradle sync.
  4. How to enable "Developer Options" & "USB Debugging" on an Android phone.
  5. How to run the app directly on the phone or build an APK file (`Build > Build Bundle(s) / APK(s) > Build APK(s)`).
  6. Offline and troubleshooting tips.

- [ ] **Step 1: Write `android/README.md` with detailed instructions and screenshots/hints**
- [ ] **Step 2: Commit documentation**

```bash
git add android/README.md
git commit -m "docs: add detailed android studio and installation guide"
```
