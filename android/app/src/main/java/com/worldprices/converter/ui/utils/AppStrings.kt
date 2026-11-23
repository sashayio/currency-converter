package com.worldprices.converter.ui.utils

object AppStrings {
    fun appName(): String = "Currency Converter"

    fun headerTitle(isRussian: Boolean): String =
        if (isRussian) "Конвертер валют" else "Currency Converter"

    fun buyCoffee(isRussian: Boolean): String =
        if (isRussian) "Купить автору кофе ☕" else "Buy author a coffee ☕"

    fun settings(isRussian: Boolean): String =
        if (isRussian) "Настройки" else "Settings"

    fun language(isRussian: Boolean): String =
        if (isRussian) "Язык интерфейса" else "Language"

    fun sourceCurrency(isRussian: Boolean): String =
        if (isRussian) "Исходная валюта" else "Source Currency"

    fun targetCurrencies(isRussian: Boolean): String =
        if (isRussian) "Целевые валюты" else "Target Currencies"

    fun customRateTitle(isRussian: Boolean): String =
        if (isRussian) "Свой курс обмена" else "Custom Exchange Rate"

    fun customRateBadge(isRussian: Boolean): String =
        if (isRussian) "Свой" else "Custom"

    fun marketRate(isRussian: Boolean): String =
        if (isRussian) "Рыночный курс:" else "Market rate:"

    fun useMarket(isRussian: Boolean): String =
        if (isRussian) "Взять рыночный" else "Use market"

    fun calculationForAmount(isRussian: Boolean): String =
        if (isRussian) "Расчет для текущей суммы:" else "Calculation for current amount:"

    fun apply(isRussian: Boolean): String =
        if (isRussian) "Применить" else "Apply"

    fun reset(isRussian: Boolean): String =
        if (isRussian) "Сбросить" else "Reset"

    fun ratesUpdated(isRussian: Boolean, date: String): String =
        if (isRussian) "Курсы обновлены: $date" else "Rates updated: $date"

    fun ratesOffline(isRussian: Boolean, date: String): String =
        if (isRussian) "Курсы на $date (Офлайн)" else "Rates as of $date (Offline)"

    fun ratesFailed(isRussian: Boolean): String =
        if (isRussian) "Не удалось загрузить курсы" else "Failed to load rates"

    fun addCurrency(isRussian: Boolean): String =
        if (isRussian) "+ Добавить валюту" else "+ Add currency"

    fun delete(isRussian: Boolean): String =
        if (isRussian) "Удалить" else "Delete"

    fun searchHint(isRussian: Boolean): String =
        if (isRussian) "Поиск..." else "Search..."

    fun searchCurrency(isRussian: Boolean): String =
        if (isRussian) "Поиск валюты или крипты..." else "Search currency or crypto..."

    fun catPopular(isRussian: Boolean): String =
        if (isRussian) "Популярные" else "Popular"

    fun catCis(isRussian: Boolean): String =
        if (isRussian) "СНГ и соседи" else "CIS & Neighbours"

    fun catFiat(isRussian: Boolean): String =
        if (isRussian) "Другие валюты" else "Other Currencies"

    fun catCrypto(isRussian: Boolean): String =
        if (isRussian) "Криптовалюты" else "Cryptocurrencies"

    fun toggleTheme(isRussian: Boolean): String =
        if (isRussian) "Сменить тему" else "Toggle theme"

    fun refreshRates(isRussian: Boolean): String =
        if (isRussian) "Обновить курсы" else "Refresh rates"

    fun shareSummary(isRussian: Boolean): String =
        if (isRussian) "Поделиться расчетом" else "Share summary"

    fun retry(isRussian: Boolean): String =
        if (isRussian) "Повторить" else "Retry"

    fun next(isRussian: Boolean): String =
        if (isRussian) "Далее →" else "Next →"

    fun skip(isRussian: Boolean): String =
        if (isRussian) "Пропустить" else "Skip"

    fun getStarted(isRussian: Boolean, count: Int): String =
        if (isRussian) "Начать ($count) 🚀" else "Get started ($count) 🚀"

    fun onboardingTitle1(isRussian: Boolean): String =
        if (isRussian) "Все валюты и крипта" else "All Currencies & Crypto"

    fun onboardingDesc1(isRussian: Boolean): String =
        if (isRussian) "Мгновенный расчет между 50+ мировыми валютами и популярными криптоактивами по живым курсам." else "Convert between 50+ global currencies and top crypto assets with live market rates."

    fun onboardingTitle2(isRussian: Boolean): String =
        if (isRussian) "Свои курсы и офлайн" else "Custom Rates & Offline"

    fun onboardingDesc2(isRussian: Boolean): String =
        if (isRussian) "Задавайте свои курсы обмена, меняйте порядок карточек и считайте без интернета в поездках." else "Set your own exchange rates for cash deals, reorder your favorite list, and calculate offline anywhere."

    fun onboardingTitle3(isRussian: Boolean): String =
        if (isRussian) "Быстрый старт" else "Quick Setup"

    fun onboardingDesc3(isRussian: Boolean): String =
        if (isRussian) "Выберите основную валюту и добавьте несколько валют для быстрого доступа." else "Select your base currency and choose your favorite targets to get started."

    fun offlineTravelHint(isRussian: Boolean): String =
        if (isRussian) "Автономный расчет в поездках" else "Offline calculations while traveling"

    fun invalidNumberError(isRussian: Boolean): String =
        if (isRussian) "Введите корректное число > 0" else "Please enter a valid number > 0"
}
