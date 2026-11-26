package com.worldprices.converter.data.model

enum class CurrencyCategory {
    POPULAR,
    CIS,
    FIAT,
    CRYPTO
}

data class Currency(
    val code: String,
    val nameRu: String,
    val nameEn: String,
    val symbol: String,
    val countryCode: String? = null,
    val category: CurrencyCategory,
    val colorHex: String? = null
) {
    fun localizedName(isRussian: Boolean): String = if (isRussian) nameRu else nameEn

    fun searchMatches(query: String, isRussian: Boolean): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return code.lowercase().contains(q) ||
                nameRu.lowercase().contains(q) ||
                nameEn.lowercase().contains(q) ||
                symbol.lowercase().contains(q)
    }
}

object CurrencyRegistry {
    private val allCurrencies: List<Currency> = listOf(
        // — Popular fiat —
        Currency("rub", "Российский рубль", "Russian Ruble", "₽", "ru", CurrencyCategory.POPULAR),
        Currency("usd", "Доллар США", "US Dollar", "$", "us", CurrencyCategory.POPULAR),
        Currency("eur", "Евро", "Euro", "€", "eu", CurrencyCategory.POPULAR),
        Currency("gbp", "Фунт стерлингов", "British Pound", "£", "gb", CurrencyCategory.POPULAR),
        Currency("cny", "Китайский юань", "Chinese Yuan", "¥", "cn", CurrencyCategory.POPULAR),
        Currency("jpy", "Японская иена", "Japanese Yen", "¥", "jp", CurrencyCategory.POPULAR),
        Currency("chf", "Швейцарский франк", "Swiss Franc", "Fr", "ch", CurrencyCategory.POPULAR),
        Currency("try", "Турецкая лира", "Turkish Lira", "₺", "tr", CurrencyCategory.POPULAR),
        Currency("inr", "Индийская рупия", "Indian Rupee", "₹", "in", CurrencyCategory.POPULAR),
        Currency("krw", "Южнокорейская вона", "South Korean Won", "₩", "kr", CurrencyCategory.POPULAR),

        // — CIS & neighbours —
        Currency("byn", "Белорусский рубль", "Belarusian Ruble", "Br", "by", CurrencyCategory.CIS),
        Currency("uah", "Украинская гривна", "Ukrainian Hryvnia", "₴", "ua", CurrencyCategory.CIS),
        Currency("kzt", "Казахский тенге", "Kazakhstani Tenge", "₸", "kz", CurrencyCategory.CIS),
        Currency("uzs", "Узбекский сум", "Uzbekistani Som", "сўм", "uz", CurrencyCategory.CIS),
        Currency("gel", "Грузинский лари", "Georgian Lari", "₾", "ge", CurrencyCategory.CIS),
        Currency("azn", "Азербайджанский манат", "Azerbaijani Manat", "₼", "az", CurrencyCategory.CIS),
        Currency("amd", "Армянский драм", "Armenian Dram", "֏", "am", CurrencyCategory.CIS),
        Currency("mdl", "Молдавский лей", "Moldovan Leu", "L", "md", CurrencyCategory.CIS),
        Currency("kgs", "Киргизский сом", "Kyrgyzstani Som", "С", "kg", CurrencyCategory.CIS),
        Currency("tjs", "Таджикский сомони", "Tajikistani Somoni", "SM", "tj", CurrencyCategory.CIS),

        // — Other fiat —
        Currency("cad", "Канадский доллар", "Canadian Dollar", "C$", "ca", CurrencyCategory.FIAT),
        Currency("aud", "Австралийский доллар", "Australian Dollar", "A$", "au", CurrencyCategory.FIAT),
        Currency("nzd", "Новозеландский доллар", "New Zealand Dollar", "NZ$", "nz", CurrencyCategory.FIAT),
        Currency("sgd", "Сингапурский доллар", "Singapore Dollar", "S$", "sg", CurrencyCategory.FIAT),
        Currency("hkd", "Гонконгский доллар", "Hong Kong Dollar", "HK$", "hk", CurrencyCategory.FIAT),
        Currency("sek", "Шведская крона", "Swedish Krona", "kr", "se", CurrencyCategory.FIAT),
        Currency("nok", "Норвежская крона", "Norwegian Krone", "kr", "no", CurrencyCategory.FIAT),
        Currency("dkk", "Датская крона", "Danish Krone", "kr", "dk", CurrencyCategory.FIAT),
        Currency("pln", "Польский злотый", "Polish Zloty", "zł", "pl", CurrencyCategory.FIAT),
        Currency("czk", "Чешская крона", "Czech Koruna", "Kč", "cz", CurrencyCategory.FIAT),
        Currency("huf", "Венгерский форинт", "Hungarian Forint", "Ft", "hu", CurrencyCategory.FIAT),
        Currency("ron", "Румынский лей", "Romanian Leu", "lei", "ro", CurrencyCategory.FIAT),
        Currency("bgn", "Болгарский лев", "Bulgarian Lev", "лв", "bg", CurrencyCategory.FIAT),
        Currency("ils", "Израильский шекель", "Israeli Shekel", "₪", "il", CurrencyCategory.FIAT),
        Currency("zar", "Южноафриканский рэнд", "South African Rand", "R", "za", CurrencyCategory.FIAT),
        Currency("brl", "Бразильский реал", "Brazilian Real", "R$", "br", CurrencyCategory.FIAT),
        Currency("mxn", "Мексиканское песо", "Mexican Peso", "Mex$", "mx", CurrencyCategory.FIAT),
        Currency("ars", "Аргентинское песо", "Argentine Peso", "AR$", "ar", CurrencyCategory.FIAT),
        Currency("thb", "Тайский бат", "Thai Baht", "฿", "th", CurrencyCategory.FIAT),
        Currency("idr", "Индонезийская рупия", "Indonesian Rupiah", "Rp", "id", CurrencyCategory.FIAT),
        Currency("myr", "Малайзийский ринггит", "Malaysian Ringgit", "RM", "my", CurrencyCategory.FIAT),
        Currency("php", "Филиппинское песо", "Philippine Peso", "₱", "ph", CurrencyCategory.FIAT),
        Currency("vnd", "Вьетнамский донг", "Vietnamese Dong", "₫", "vn", CurrencyCategory.FIAT),
        Currency("egp", "Египетский фунт", "Egyptian Pound", "E£", "eg", CurrencyCategory.FIAT),
        Currency("aed", "Дирхам ОАЭ", "UAE Dirham", "د.إ", "ae", CurrencyCategory.FIAT),
        Currency("sar", "Саудовский риял", "Saudi Riyal", "﷼", "sa", CurrencyCategory.FIAT),
        Currency("qar", "Катарский риял", "Qatari Riyal", "QR", "qa", CurrencyCategory.FIAT),
        Currency("twd", "Тайваньский доллар", "New Taiwan Dollar", "NT$", "tw", CurrencyCategory.FIAT),
        Currency("rsd", "Сербский динар", "Serbian Dinar", "din", "rs", CurrencyCategory.FIAT),

        // — Crypto —
        Currency("btc", "Bitcoin", "Bitcoin", "₿", null, CurrencyCategory.CRYPTO, "#F7931A"),
        Currency("eth", "Ethereum", "Ethereum", "Ξ", null, CurrencyCategory.CRYPTO, "#627EEA"),
        Currency("usdt", "Tether", "Tether", "₮", null, CurrencyCategory.CRYPTO, "#26A17B"),
        Currency("bnb", "BNB", "BNB", "BNB", null, CurrencyCategory.CRYPTO, "#F3BA2F"),
        Currency("xrp", "Ripple", "Ripple", "XRP", null, CurrencyCategory.CRYPTO, "#00AAE4"),
        Currency("sol", "Solana", "Solana", "SOL", null, CurrencyCategory.CRYPTO, "#9945FF"),
        Currency("ada", "Cardano", "Cardano", "ADA", null, CurrencyCategory.CRYPTO, "#0033AD"),
        Currency("doge", "Dogecoin", "Dogecoin", "Ð", null, CurrencyCategory.CRYPTO, "#C2A633"),
        Currency("dot", "Polkadot", "Polkadot", "DOT", null, CurrencyCategory.CRYPTO, "#E6007A"),
        Currency("ltc", "Litecoin", "Litecoin", "Ł", null, CurrencyCategory.CRYPTO, "#345D9D"),
        Currency("ton", "Toncoin", "Toncoin", "TON", null, CurrencyCategory.CRYPTO, "#0098EA"),
        Currency("trx", "TRON", "TRON", "TRX", null, CurrencyCategory.CRYPTO, "#FF0013")
    )

    private val currencyMap = allCurrencies.associateBy { it.code.lowercase() }

    fun getAll(): List<Currency> = allCurrencies

    fun getByCode(code: String): Currency? = currencyMap[code.lowercase()]

    fun getByCategory(category: CurrencyCategory): List<Currency> =
        allCurrencies.filter { it.category == category }

    val defaultSource: Currency get() = getByCode("rub") ?: allCurrencies.first()
    val defaultTargets: List<Currency> get() = listOfNotNull(
        getByCode("usd"),
        getByCode("eur"),
        getByCode("byn")
    )
}
