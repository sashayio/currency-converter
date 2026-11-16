/* ============================================================
   World Prices — Currency Converter
   Free, unlimited exchange rates API: Open ExchangeRate-API + live crypto
   ============================================================ */

// ============ API URLS ============
// Primary: Open ExchangeRate-API (hourly spot rates, 100% free, unlimited, no key needed)
const API_OPEN_EXCHANGE = 'https://open.er-api.com/v6/latest';
// Fallback 1: Cloudflare Pages Currency-API mirror
const API_PAGES_DEV     = 'https://latest.currency-api.pages.dev/v1/currencies';
// Fallback 2: jsDelivr CDN
const API_JSDELIVR      = 'https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies';

// Crypto spot prices API (Binance public tickers, real-time live prices, no key)
const API_BINANCE       = 'https://api.binance.com/api/v3/ticker/price';

const FLAG_URL          = 'assets/flags';
const STORAGE_KEY       = 'worldprices_v2';
const THEME_STORAGE_KEY = 'worldprices_theme';
const LANG_STORAGE_KEY  = 'worldprices_lang';
const CACHE_MAX_AGE     = 15 * 60 * 1000; // 15 minutes fresh cache

// ============ I18N (INTERNATIONALIZATION) ============
function getInitialLanguage() {
  try {
    const saved = localStorage.getItem(LANG_STORAGE_KEY);
    if (saved === 'ru' || saved === 'en') return saved;
  } catch (e) {}
  // Default is English
  return 'en';
}

let currentLang = getInitialLanguage();

const I18N = {
  ru: {
    appTitle: 'Currency converter',
    headerTitle: 'Конвертер валют',
    appSubtitle: 'Конвертер валют и криптовалют',
    setupStep1Title: 'Из какой валюты конвертировать?',
    setupStep2Title: 'В какие валюты конвертировать?',
    setupStep2Hint: 'Выберите одну или несколько',
    searchCurrency: 'Поиск валюты...',
    searchShort: 'Поиск...',
    nextBtn: 'Далее →',
    startBtn: 'Начать 🚀',
    startBtnCount: (n) => `Начать (${n}) 🚀`,
    refreshRates: 'Обновить курсы',
    toggleTheme: 'Сменить тему',
    exportPNG: 'Экспорт в PNG',
    settings: 'Настройки',
    language: 'Язык интерфейса',
    sourceCurrency: 'Исходная валюта',
    targetCurrencies: 'Целевые валюты',
    save: 'Сохранить',
    cancel: 'Отменить',
    reorderInfo: 'Порядок валют (перетащите)',
    holdToDrag: 'Зажмите для перемещения',
    deleteCurrency: 'Удалить',
    loadingRates: 'Загрузка курсов...',
    ratesNotLoaded: 'Курсы не загружены',
    ratesUpdated: (date, time) => `Курсы обновлены: ${date}, ${time}`,
    ratesAsOf: (date) => `Курсы на ${date}`,
    unitRate: (src, unit) => `1 ${src} = ${unit}`,
    buyCoffee: 'Купить автору кофе',
    switchToDark: 'Переключить на тёмную тему',
    switchToLight: 'Переключить на светлую тему',
    toastDarkTheme: '🌙 Тёмная тема',
    toastLightTheme: '☀️ Светлая тема',
    toastReorderMode: 'Режим изменения порядка: перетащите карточки',
    toastReorderSaved: '✓ Порядок сохранён',
    toastReorderCancelled: 'Изменения отменены',
    toastCurrencyRemoved: (c) => `${c} удалён`,
    toastSettingsSaved: '✓ Настройки сохранены',
    toastExportStart: '📸 Генерация изображения...',
    toastExportDone: '✓ Изображение сохранено!',
    toastExportError: (e) => `⚠️ Ошибка экспорта: ${e}`,
    toastRatesUpdating: '🔄 Обновление курсов...',
    toastRatesUpdated: '✓ Курсы обновлены',
    toastRatesError: '⚠️ Не удалось обновить',
    toastRatesCheckNet: '⚠️ Не удалось загрузить курсы. Проверьте интернет.',
    catPopular: '⭐ Популярные',
    catCis: '🌍 СНГ и соседи',
    catFiat: '💵 Другие валюты',
    catCrypto: '🪙 Криптовалюты',
  },
  en: {
    appTitle: 'Currency converter',
    headerTitle: 'Currency converter',
    appSubtitle: 'Currency & crypto converter',
    setupStep1Title: 'Select source currency',
    setupStep2Title: 'Select target currencies',
    setupStep2Hint: 'Select one or more currencies',
    searchCurrency: 'Search currency...',
    searchShort: 'Search...',
    nextBtn: 'Next →',
    startBtn: 'Get started 🚀',
    startBtnCount: (n) => `Get started (${n}) 🚀`,
    refreshRates: 'Refresh rates',
    toggleTheme: 'Toggle theme',
    exportPNG: 'Export to PNG',
    settings: 'Settings',
    language: 'Language',
    sourceCurrency: 'Source currency',
    targetCurrencies: 'Target currencies',
    save: 'Save',
    cancel: 'Cancel',
    reorderInfo: 'Currency order (drag to reorder)',
    holdToDrag: 'Hold to drag',
    deleteCurrency: 'Remove',
    loadingRates: 'Loading exchange rates...',
    ratesNotLoaded: 'Rates not loaded',
    ratesUpdated: (date, time) => `Rates updated: ${date}, ${time}`,
    ratesAsOf: (date) => `Rates as of ${date}`,
    unitRate: (src, unit) => `1 ${src} = ${unit}`,
    buyCoffee: 'Buy me a coffee',
    switchToDark: 'Switch to dark theme',
    switchToLight: 'Switch to light theme',
    toastDarkTheme: '🌙 Dark theme',
    toastLightTheme: '☀️ Light theme',
    toastReorderMode: 'Reorder mode: drag cards to reorder',
    toastReorderSaved: '✓ Order saved',
    toastReorderCancelled: 'Changes cancelled',
    toastCurrencyRemoved: (c) => `${c} removed`,
    toastSettingsSaved: '✓ Settings saved',
    toastExportStart: '📸 Generating image...',
    toastExportDone: '✓ Image saved!',
    toastExportError: (e) => `⚠️ Export error: ${e}`,
    toastRatesUpdating: '🔄 Refreshing rates...',
    toastRatesUpdated: '✓ Rates updated',
    toastRatesError: '⚠️ Failed to update',
    toastRatesCheckNet: '⚠️ Failed to load rates. Check connection.',
    catPopular: '⭐ Popular',
    catCis: '🌍 CIS & Neighbours',
    catFiat: '💵 Other Currencies',
    catCrypto: '🪙 Cryptocurrencies',
  }
};

function t(key, ...args) {
  const dict = I18N[currentLang] || I18N.ru;
  const val = dict[key] !== undefined ? dict[key] : (I18N.ru[key] !== undefined ? I18N.ru[key] : key);
  return typeof val === 'function' ? val(...args) : val;
}

// ============ CURRENCY DATABASE ============
const CURRENCIES = {
  // — Popular fiat —
  rub: { nameRu: 'Российский рубль',     nameEn: 'Russian Ruble',       symbol: '₽',  cc: 'ru',  cat: 'popular' },
  usd: { nameRu: 'Доллар США',           nameEn: 'US Dollar',           symbol: '$',  cc: 'us',  cat: 'popular' },
  eur: { nameRu: 'Евро',                 nameEn: 'Euro',                symbol: '€',  cc: 'eu',  cat: 'popular' },
  gbp: { nameRu: 'Фунт стерлингов',      nameEn: 'British Pound',       symbol: '£',  cc: 'gb',  cat: 'popular' },
  cny: { nameRu: 'Китайский юань',       nameEn: 'Chinese Yuan',        symbol: '¥',  cc: 'cn',  cat: 'popular' },
  jpy: { nameRu: 'Японская иена',        nameEn: 'Japanese Yen',        symbol: '¥',  cc: 'jp',  cat: 'popular' },
  chf: { nameRu: 'Швейцарский франк',    nameEn: 'Swiss Franc',         symbol: 'Fr', cc: 'ch',  cat: 'popular' },
  try: { nameRu: 'Турецкая лира',        nameEn: 'Turkish Lira',        symbol: '₺',  cc: 'tr',  cat: 'popular' },
  inr: { nameRu: 'Индийская рупия',      nameEn: 'Indian Rupee',        symbol: '₹',  cc: 'in',  cat: 'popular' },
  krw: { nameRu: 'Южнокорейская вона',   nameEn: 'South Korean Won',    symbol: '₩',  cc: 'kr',  cat: 'popular' },

  // — CIS & neighbours —
  byn: { nameRu: 'Белорусский рубль',    nameEn: 'Belarusian Ruble',    symbol: 'Br', cc: 'by',  cat: 'cis' },
  uah: { nameRu: 'Украинская гривна',    nameEn: 'Ukrainian Hryvnia',   symbol: '₴',  cc: 'ua',  cat: 'cis' },
  kzt: { nameRu: 'Казахский тенге',      nameEn: 'Kazakhstani Tenge',   symbol: '₸',  cc: 'kz',  cat: 'cis' },
  uzs: { nameRu: 'Узбекский сум',        nameEn: 'Uzbekistani Som',     symbol: 'сўм',cc: 'uz',  cat: 'cis' },
  gel: { nameRu: 'Грузинский лари',      nameEn: 'Georgian Lari',       symbol: '₾',  cc: 'ge',  cat: 'cis' },
  azn: { nameRu: 'Азербайджанский манат', nameEn: 'Azerbaijani Manat',   symbol: '₼',  cc: 'az',  cat: 'cis' },
  amd: { nameRu: 'Армянский драм',       nameEn: 'Armenian Dram',       symbol: '֏',  cc: 'am',  cat: 'cis' },
  mdl: { nameRu: 'Молдавский лей',       nameEn: 'Moldovan Leu',        symbol: 'L',  cc: 'md',  cat: 'cis' },
  kgs: { nameRu: 'Киргизский сом',       nameEn: 'Kyrgyzstani Som',     symbol: 'С',  cc: 'kg',  cat: 'cis' },
  tjs: { nameRu: 'Таджикский сомони',    nameEn: 'Tajikistani Somoni',  symbol: 'SM', cc: 'tj',  cat: 'cis' },

  // — Other fiat —
  cad: { nameRu: 'Канадский доллар',     nameEn: 'Canadian Dollar',     symbol: 'C$', cc: 'ca',  cat: 'fiat' },
  aud: { nameRu: 'Австралийский доллар',  nameEn: 'Australian Dollar',   symbol: 'A$', cc: 'au',  cat: 'fiat' },
  nzd: { nameRu: 'Новозеландский доллар', nameEn: 'New Zealand Dollar',  symbol: 'NZ$',cc: 'nz', cat: 'fiat' },
  sgd: { nameRu: 'Сингапурский доллар',  nameEn: 'Singapore Dollar',    symbol: 'S$', cc: 'sg',  cat: 'fiat' },
  hkd: { nameRu: 'Гонконгский доллар',   nameEn: 'Hong Kong Dollar',    symbol: 'HK$',cc: 'hk', cat: 'fiat' },
  sek: { nameRu: 'Шведская крона',       nameEn: 'Swedish Krona',       symbol: 'kr', cc: 'se',  cat: 'fiat' },
  nok: { nameRu: 'Норвежская крона',     nameEn: 'Norwegian Krone',     symbol: 'kr', cc: 'no',  cat: 'fiat' },
  dkk: { nameRu: 'Датская крона',        nameEn: 'Danish Krone',        symbol: 'kr', cc: 'dk',  cat: 'fiat' },
  pln: { nameRu: 'Польский злотый',      nameEn: 'Polish Zloty',        symbol: 'zł', cc: 'pl',  cat: 'fiat' },
  czk: { nameRu: 'Чешская крона',        nameEn: 'Czech Koruna',        symbol: 'Kč', cc: 'cz',  cat: 'fiat' },
  huf: { nameRu: 'Венгерский форинт',    nameEn: 'Hungarian Forint',    symbol: 'Ft', cc: 'hu',  cat: 'fiat' },
  ron: { nameRu: 'Румынский лей',        nameEn: 'Romanian Leu',        symbol: 'lei',cc: 'ro',  cat: 'fiat' },
  bgn: { nameRu: 'Болгарский лев',       nameEn: 'Bulgarian Lev',       symbol: 'лв', cc: 'bg',  cat: 'fiat' },
  ils: { nameRu: 'Израильский шекель',   nameEn: 'Israeli Shekel',      symbol: '₪',  cc: 'il',  cat: 'fiat' },
  zar: { nameRu: 'Южноафриканский рэнд', nameEn: 'South African Rand',  symbol: 'R',  cc: 'za',  cat: 'fiat' },
  brl: { nameRu: 'Бразильский реал',     nameEn: 'Brazilian Real',      symbol: 'R$', cc: 'br',  cat: 'fiat' },
  mxn: { nameRu: 'Мексиканское песо',    nameEn: 'Mexican Peso',        symbol: 'Mex$',cc:'mx',  cat: 'fiat' },
  ars: { nameRu: 'Аргентинское песо',    nameEn: 'Argentine Peso',      symbol: 'AR$',cc: 'ar',  cat: 'fiat' },
  thb: { nameRu: 'Тайский бат',          nameEn: 'Thai Baht',          symbol: '฿',  cc: 'th',  cat: 'fiat' },
  idr: { nameRu: 'Индонезийская рупия',  nameEn: 'Indonesian Rupiah',  symbol: 'Rp', cc: 'id',  cat: 'fiat' },
  myr: { nameRu: 'Малайзийский ринггит', nameEn: 'Malaysian Ringgit',  symbol: 'RM', cc: 'my',  cat: 'fiat' },
  php: { nameRu: 'Филиппинское песо',    nameEn: 'Philippine Peso',     symbol: '₱',  cc: 'ph',  cat: 'fiat' },
  vnd: { nameRu: 'Вьетнамский донг',     nameEn: 'Vietnamese Dong',     symbol: '₫',  cc: 'vn',  cat: 'fiat' },
  egp: { nameRu: 'Египетский фунт',      nameEn: 'Egyptian Pound',      symbol: 'E£', cc: 'eg',  cat: 'fiat' },
  aed: { nameRu: 'Дирхам ОАЭ',          nameEn: 'UAE Dirham',          symbol: 'د.إ',cc: 'ae',  cat: 'fiat' },
  sar: { nameRu: 'Саудовский риял',      nameEn: 'Saudi Riyal',         symbol: '﷼',  cc: 'sa',  cat: 'fiat' },
  qar: { nameRu: 'Катарский риял',       nameEn: 'Qatari Riyal',        symbol: 'QR', cc: 'qa',  cat: 'fiat' },
  twd: { nameRu: 'Тайваньский доллар',   nameEn: 'New Taiwan Dollar',   symbol: 'NT$',cc: 'tw',  cat: 'fiat' },
  rsd: { nameRu: 'Сербский динар',       nameEn: 'Serbian Dinar',       symbol: 'din',cc: 'rs',  cat: 'fiat' },

  // — Crypto —
  btc:  { nameRu: 'Bitcoin',    nameEn: 'Bitcoin',    symbol: '₿',   cc: null, cat: 'crypto', color: '#f7931a' },
  eth:  { nameRu: 'Ethereum',   nameEn: 'Ethereum',   symbol: 'Ξ',   cc: null, cat: 'crypto', color: '#627eea' },
  usdt: { nameRu: 'Tether',     nameEn: 'Tether',     symbol: '₮',   cc: null, cat: 'crypto', color: '#26a17b' },
  bnb:  { nameRu: 'BNB',        nameEn: 'BNB',        symbol: 'BNB', cc: null, cat: 'crypto', color: '#f3ba2f' },
  xrp:  { nameRu: 'Ripple',     nameEn: 'Ripple',     symbol: 'XRP', cc: null, cat: 'crypto', color: '#00aae4' },
  sol:  { nameRu: 'Solana',     nameEn: 'Solana',     symbol: 'SOL', cc: null, cat: 'crypto', color: '#9945ff' },
  ada:  { nameRu: 'Cardano',    nameEn: 'Cardano',    symbol: 'ADA', cc: null, cat: 'crypto', color: '#0033ad' },
  doge: { nameRu: 'Dogecoin',   nameEn: 'Dogecoin',   symbol: 'Ð',   cc: null, cat: 'crypto', color: '#c2a633' },
  dot:  { nameRu: 'Polkadot',   nameEn: 'Polkadot',   symbol: 'DOT', cc: null, cat: 'crypto', color: '#e6007a' },
  ltc:  { nameRu: 'Litecoin',   nameEn: 'Litecoin',   symbol: 'Ł',   cc: null, cat: 'crypto', color: '#345d9d' },
  ton:  { nameRu: 'Toncoin',    nameEn: 'Toncoin',    symbol: 'TON', cc: null, cat: 'crypto', color: '#0098ea' },
  trx:  { nameRu: 'TRON',       nameEn: 'TRON',       symbol: 'TRX', cc: null, cat: 'crypto', color: '#ff0013' },
};

// Dynamic name accessor that returns the localized name
for (const [code, cur] of Object.entries(CURRENCIES)) {
  Object.defineProperty(cur, 'name', {
    get() {
      return currentLang === 'en' ? (cur.nameEn || cur.nameRu) : (cur.nameRu || cur.nameEn);
    },
    configurable: true,
    enumerable: true
  });
}

function getCategoryName(cat) {
  if (cat === 'popular') return t('catPopular');
  if (cat === 'cis') return t('catCis');
  if (cat === 'fiat') return t('catFiat');
  if (cat === 'crypto') return t('catCrypto');
  return cat;
}

const CATEGORY_NAMES = new Proxy({}, {
  get: (_, cat) => getCategoryName(cat)
});

function searchMatches(code, cur, filter) {
  if (!filter) return true;
  const f = filter.toLowerCase().trim();
  return (
    code.toLowerCase().includes(f) ||
    (cur.nameRu && cur.nameRu.toLowerCase().includes(f)) ||
    (cur.nameEn && cur.nameEn.toLowerCase().includes(f)) ||
    (cur.symbol && cur.symbol.toLowerCase().includes(f))
  );
}

const CATEGORY_ORDER = ['popular', 'cis', 'fiat', 'crypto'];

// ============ STATE ============
let state = {
  source:  'rub',
  targets: ['usd', 'eur', 'byn'],
  amount:  1000,
  rates:   {},          // { usd: 0.0108, eur: 0.0099, ... }
  rateDate: null,
  cacheTime: 0,
};

// ============ DOM REFS ============
const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);

// ============ HELPERS ============
const flagDataCache = new Map();
let euFlagDataUrlCache = null;

// High-DPI Canvas generator for EUR flag (creates real PNG data URL: instant loading, no CORS, renders in html2canvas)
function getEUFlagDataURL(size = 160) {
  if (euFlagDataUrlCache) return euFlagDataUrlCache;
  try {
    const canvas = document.createElement('canvas');
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext('2d');
    if (!ctx) return '';

    // Circle clipping for clean round flag
    ctx.beginPath();
    ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();

    // EU Blue background
    ctx.fillStyle = '#003399';
    ctx.fillRect(0, 0, size, size);

    // 12 golden five-pointed stars
    const center = size / 2;
    const ringRadius = size * 0.28;
    const outerRadius = size * 0.052;
    const innerRadius = outerRadius * 0.382;

    ctx.fillStyle = '#ffcc00';

    for (let i = 0; i < 12; i++) {
      const angle = (i * 30 - 90) * Math.PI / 180;
      const cx = center + ringRadius * Math.cos(angle);
      const cy = center + ringRadius * Math.sin(angle);

      ctx.beginPath();
      for (let s = 0; s < 10; s++) {
        const starAngle = (s * 36 - 90) * Math.PI / 180;
        const r = s % 2 === 0 ? outerRadius : innerRadius;
        const x = cx + r * Math.cos(starAngle);
        const y = cy + r * Math.sin(starAngle);
        if (s === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.closePath();
      ctx.fill();
    }

    euFlagDataUrlCache = canvas.toDataURL('image/png');
    return euFlagDataUrlCache;
  } catch (e) {
    console.warn('Failed to generate EU flag canvas:', e);
    return '';
  }
}

// ============ THEME MANAGEMENT ============
function getCurrentTheme() {
  const saved = localStorage.getItem(THEME_STORAGE_KEY);
  if (saved === 'dark' || saved === 'light') return saved;
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    ? 'dark'
    : 'light';
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem(THEME_STORAGE_KEY, theme);
  updateThemeIcon(theme);
}

function toggleTheme() {
  const btnEl = $('#theme-btn');
  if (btnEl) {
    btnEl.classList.remove('toggling');
    void btnEl.offsetWidth; // Force reflow to re-trigger animation
    btnEl.classList.add('toggling');
    setTimeout(() => btnEl.classList.remove('toggling'), 400);
  }
  const current = document.documentElement.getAttribute('data-theme') || getCurrentTheme();
  const next = current === 'dark' ? 'light' : 'dark';
  applyTheme(next);
  showToast(next === 'dark' ? t('toastDarkTheme') : t('toastLightTheme'));
}

function updateThemeIcon(theme) {
  const iconEl = $('#theme-icon');
  const btnEl = $('#theme-btn');
  if (!iconEl) return;
  if (theme === 'dark') {
    // Google Material Symbols light_mode (sun)
    iconEl.innerHTML = `<path fill="currentColor" d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36l-1.06 1.06a.996.996 0 0 0 0 1.41c.39.39 1.03.39 1.41 0l1.06-1.06c.39-.39.39-1.03 0-1.41s-1.02-.39-1.41 0z"/>`;
    if (btnEl) btnEl.title = t('switchToLight');
  } else {
    // Official Google Material Design dark_mode (smooth crescent moon)
    iconEl.innerHTML = `<path fill="currentColor" d="M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-3.03 0-5.5-2.47-5.5-5.5 0-1.82.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z"/>`;
    if (btnEl) btnEl.title = t('switchToDark');
  }
}

async function getFlagDataUrl(code) {
  const cur = CURRENCIES[code];
  if (!cur) return null;
  if (cur.cat === 'crypto') return null;

  if (cur.customFlag) return cur.customFlag;
  if (!cur.cc) return null;

  const cc = cur.cc;
  if (flagDataCache.has(cc)) {
    return flagDataCache.get(cc);
  }

  try {
    const img = new Image();
    const loaded = new Promise((resolve, reject) => {
      img.onload = () => resolve(img);
      img.onerror = () => reject(new Error(`Failed to load ${FLAG_URL}/${cc}.svg`));
    });
    img.src = `${FLAG_URL}/${cc}.svg`;
    await loaded;

    const canvas = document.createElement('canvas');
    canvas.width = 80;
    canvas.height = 80;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0, 80, 80);
    const dataUrl = canvas.toDataURL('image/png');
    flagDataCache.set(cc, dataUrl);
    return dataUrl;
  } catch (error) {
    console.warn(`Не удалось подготовить флаг ${cc}:`, error);
    return `${FLAG_URL}/${cc}.svg`;
  }
}

function getExportFlagHTML(code, size = 28) {
  const cur = CURRENCIES[code];
  if (!cur) return '';

  if (cur.cat === 'crypto') {
    return getFlagHTML(code, size);
  }

  let src = null;
  if (cur.customFlag) {
    src = cur.customFlag;
  } else if (cur.cc) {
    src = flagDataCache.get(cur.cc) || `${FLAG_URL}/${cur.cc}.svg`;
  }

  if (!src) {
    return `<span style="
      display:flex;
      align-items:center;
      justify-content:center;
      width:${size}px;
      height:${size}px;
      border-radius:50%;
      background:#555;
      color:white;
      font-size:11px;
      font-weight:bold;
    ">${code.toUpperCase().slice(0, 2)}</span>`;
  }

  return `<img
    src="${src}"
    alt="${code}"
    width="${size}"
    height="${size}"
    style="
      display:block;
      width:${size}px;
      height:${size}px;
      object-fit:cover;
      border-radius:50%;
    "
  >`;
}

function getFlagHTML(code, size = 28) {
  const cur = CURRENCIES[code];
  if (!cur) return `<span style="font-size:${size*0.5}px">${code.toUpperCase().slice(0,2)}</span>`;

  if (cur.customFlag) {
    return `<img src="${cur.customFlag}" alt="${code}" width="${size}" height="${size}" style="display:block;width:${size}px;height:${size}px;object-fit:cover;border-radius:50%">`;
  }
  if (cur.cc) {
    const src = `${FLAG_URL}/${cur.cc}.svg`;
    return `<img src="${src}" alt="${code}" width="${size}" height="${size}" loading="lazy" style="display:block;width:${size}px;height:${size}px;object-fit:cover;border-radius:50%" onerror="this.style.display='none';this.parentElement.textContent='${code.toUpperCase().slice(0,2)}'">`;
  }
  // Crypto — colored circle with symbol
  const bg = cur.color || '#555';
  return `<span style="display:flex;align-items:center;justify-content:center;width:${size}px;height:${size}px;border-radius:50%;background:${bg};color:#fff;font-size:${size*0.42}px;font-weight:700;line-height:1">${cur.symbol || code.toUpperCase().slice(0,1)}</span>`;
}

function formatNumber(num, currency) {
  if (num === null || num === undefined || isNaN(num)) return '—';
  const abs = Math.abs(num);
  let decimals;
  if (abs === 0) decimals = 2;
  else if (abs < 0.0001) decimals = 8;
  else if (abs < 0.01) decimals = 6;
  else if (abs < 1) decimals = 4;
  else if (abs < 100) decimals = 2;
  else decimals = 2;

  // Crypto targets may need more decimals
  const cur = CURRENCIES[currency];
  if (cur && cur.cat === 'crypto' && abs < 1) {
    decimals = Math.max(decimals, 6);
  }

  const locale = currentLang === 'en' ? 'en-US' : 'ru-RU';
  return new Intl.NumberFormat(locale, {
    minimumFractionDigits: 0,
    maximumFractionDigits: decimals,
  }).format(num);
}

function parseAmountInput(val) {
  if (typeof val === 'number') return isNaN(val) ? 0 : Math.max(0, val);
  if (!val) return 0;
  let s = String(val).replace(/[\s\u00A0\u202F]/g, '').trim();
  if (!s) return 0;

  // Handle leading dot or comma (.5 -> 0.5, ,5 -> 0.5)
  if (s.startsWith('.') || s.startsWith(',')) {
    s = '0' + s;
  }

  const lastDot = s.lastIndexOf('.');
  const lastComma = s.lastIndexOf(',');

  if (lastDot !== -1 && lastComma !== -1) {
    if (lastDot > lastComma) {
      // e.g. 1,234.56 -> commas are thousands separators
      s = s.replace(/,/g, '');
    } else {
      // e.g. 1.234,56 -> dots are thousands separators
      s = s.replace(/\./g, '').replace(',', '.');
    }
  } else if (lastComma !== -1) {
    const commaCount = (s.match(/,/g) || []).length;
    if (commaCount > 1) {
      // Multiple commas e.g. 1,000,000
      s = s.replace(/,/g, '');
    } else {
      // Single comma
      const parts = s.split(',');
      if (currentLang === 'en' && parts[1] && parts[1].length === 3 && parts[0].length >= 1 && parts[0].length <= 3) {
        // In English locale, 1,000 is 1000
        s = s.replace(',', '');
      } else {
        // In Russian or decimal comma e.g. 10,5 or 1000,50
        s = s.replace(',', '.');
      }
    }
  } else if (lastDot !== -1) {
    const dotCount = (s.match(/\./g) || []).length;
    if (dotCount > 1) {
      // Multiple dots e.g. 1.000.000
      s = s.replace(/\./g, '');
    }
  }

  s = s.replace(/[^\d.-]/g, '');
  const num = parseFloat(s);
  return isNaN(num) ? 0 : Math.max(0, num);
}

function formatAmountInput(num) {
  if (num === null || num === undefined || isNaN(num)) return '';
  if (num === 0) return '0';
  if (Math.abs(num) >= 1e15) return String(num);
  let s = Number.isInteger(num) ? String(num) : num.toFixed(8).replace(/\.?0+$/, '');
  return s;
}

function showToast(msg, duration = 2500) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.remove('hidden');
  clearTimeout(t._timer);
  t._timer = setTimeout(() => t.classList.add('hidden'), duration);
}

// ============ STORAGE ============
function saveState() {
  const data = {
    source: state.source,
    targets: state.targets,
    amount: state.amount,
    rates: state.rates,
    rateDate: state.rateDate,
    cacheTime: state.cacheTime,
  };
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch (e) { /* quota exceeded, ignore */ }
}

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return false;
    const data = JSON.parse(raw);
    if (data.source) state.source = data.source;
    if (Array.isArray(data.targets) && data.targets.length) state.targets = data.targets;
    if (typeof data.amount === 'number') state.amount = data.amount;
    if (data.rates) state.rates = data.rates;
    if (data.rateDate) state.rateDate = data.rateDate;
    if (data.cacheTime) state.cacheTime = data.cacheTime;
    return true;
  } catch (e) {
    return false;
  }
}

// ============ API ============
const CRYPTO_COINGECKO_MAP = {
  btc: 'bitcoin',
  eth: 'ethereum',
  usdt: 'tether',
  bnb: 'binancecoin',
  xrp: 'ripple',
  sol: 'solana',
  ada: 'cardano',
  doge: 'dogecoin',
  dot: 'polkadot',
  ltc: 'litecoin',
  ton: 'the-open-network',
  trx: 'tron',
};

let cryptoUsdPricesCache = null;
let cryptoCacheTime = 0;
const CRYPTO_CACHE_AGE = 5 * 60 * 1000; // 5 minutes

// Fetch live spot crypto rates in USD (Binance public tickers -> CoinGecko fallback)
async function getLiveCryptoUsdPrices(force = false) {
  if (!force && cryptoUsdPricesCache && (Date.now() - cryptoCacheTime < CRYPTO_CACHE_AGE)) {
    return cryptoUsdPricesCache;
  }

  const prices = { usdt: 1.0 }; // USDT pegged ~1:1 to USD

  // Strategy A: Binance Public Tickers (fast, live spot market prices, no key, no limits)
  try {
    const symbols = [
      'BTCUSDT', 'ETHUSDT', 'BNBUSDT', 'XRPUSDT',
      'SOLUSDT', 'ADAUSDT', 'DOGEUSDT', 'DOTUSDT',
      'LTCUSDT', 'TONUSDT', 'TRXUSDT'
    ];
    const url = `${API_BINANCE}?symbols=${encodeURIComponent(JSON.stringify(symbols))}`;
    const res = await fetch(url);
    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data)) {
        for (const item of data) {
          const coin = item.symbol.replace('USDT', '').toLowerCase();
          const p = parseFloat(item.price);
          if (p > 0) prices[coin] = p;
        }
      }
    }
  } catch (e) {
    console.warn('Binance crypto ticker error, trying fallback:', e);
  }

  // Strategy B: Fallback for missing coins via CoinGecko simple price
  const missing = Object.keys(CRYPTO_COINGECKO_MAP).filter(c => !prices[c]);
  if (missing.length > 0) {
    try {
      const ids = missing.map(c => CRYPTO_COINGECKO_MAP[c]).join(',');
      const res = await fetch(`https://api.coingecko.com/api/v3/simple/price?ids=${ids}&vs_currencies=usd`);
      if (res.ok) {
        const data = await res.json();
        for (const [coin, id] of Object.entries(CRYPTO_COINGECKO_MAP)) {
          if (data[id]?.usd) {
            prices[coin] = data[id].usd;
          }
        }
      }
    } catch (e) {
      console.warn('CoinGecko fallback error:', e);
    }
  }

  if (Object.keys(prices).length > 1) {
    cryptoUsdPricesCache = prices;
    cryptoCacheTime = Date.now();
  }

  return cryptoUsdPricesCache || prices;
}

// Seamlessly enrich rates with real-time crypto prices
async function enrichCryptoRates(rates, sourceCode, force = false) {
  const isSourceCrypto = CURRENCIES[sourceCode]?.cat === 'crypto';
  const cryptoPrices = await getLiveCryptoUsdPrices(force);

  if (isSourceCrypto) {
    const sourceUsdPrice = cryptoPrices[sourceCode] || 1;
    rates['usd'] = sourceUsdPrice;
    rates['usdt'] = sourceUsdPrice;

    // Cross-rates for all cryptos
    for (const [coin, usdPrice] of Object.entries(cryptoPrices)) {
      if (coin !== sourceCode && usdPrice > 0) {
        rates[coin] = sourceUsdPrice / usdPrice;
      }
    }

    // Cross-rates for fiat currencies
    for (const [code, cur] of Object.entries(CURRENCIES)) {
      if (cur.cat !== 'crypto' && rates[code]) {
        rates[code] = sourceUsdPrice * rates[code];
      }
    }
  } else {
    // Source is fiat (RUB, USD, EUR, etc.)
    const usdRate = rates['usd'];
    if (usdRate) {
      for (const [coin, usdPrice] of Object.entries(cryptoPrices)) {
        if (usdPrice > 0) {
          rates[coin] = usdRate / usdPrice;
        }
      }
      rates['usdt'] = usdRate;
    }
  }
}

async function fetchRates(source, force = false) {
  const code = source.toLowerCase();
  const codeUpper = source.toUpperCase();
  let rates = null;
  let dateStr = null;

  // 1. Primary: Open ExchangeRate-API (hourly updated spot rates, 100% free, unlimited, no key)
  try {
    const cacheBuster = force ? `&_=${Date.now()}` : '';
    const res = await fetch(`${API_OPEN_EXCHANGE}/${codeUpper}?source=app${cacheBuster}`);
    if (res.ok) {
      const data = await res.json();
      if (data && data.result === 'success' && data.rates) {
        rates = {};
        for (const [k, v] of Object.entries(data.rates)) {
          rates[k.toLowerCase()] = v;
        }
        rates[code] = 1;
        if (data.time_last_update_utc) {
          dateStr = data.time_last_update_utc;
        }
      }
    }
  } catch (e) {
    console.warn('Open ExchangeRate-API failed, trying fallback:', e);
  }

  // 2. Fallbacks: Cloudflare Pages & jsDelivr (currency-api)
  if (!rates) {
    const cacheBuster = force ? `?_=${Date.now()}` : '';
    const urls = [
      `${API_PAGES_DEV}/${code}.min.json${cacheBuster}`,
      `${API_PAGES_DEV}/${code}.json${cacheBuster}`,
      `${API_JSDELIVR}/${code}.min.json`,
      `${API_JSDELIVR}/${code}.json`,
    ];

    for (const url of urls) {
      try {
        const res = await fetch(url);
        if (!res.ok) continue;
        const data = await res.json();
        if (data && data[code]) {
          rates = data[code];
          dateStr = data.date || new Date().toISOString();
          break;
        }
      } catch (e) {
        continue;
      }
    }
  }

  if (!rates) {
    return false;
  }

  // 3. Live spot crypto enrichment (Binance + CoinGecko)
  try {
    await enrichCryptoRates(rates, code, force);
  } catch (e) {
    console.warn('Crypto enrichment error:', e);
  }

  state.rates = rates;
  state.rateDate = dateStr || new Date().toISOString();
  state.cacheTime = Date.now();
  saveState();
  return true;
}

async function ensureRates(force = false) {
  if (!force && state.rates && Object.keys(state.rates).length > 0) {
    if (Date.now() - state.cacheTime < CACHE_MAX_AGE) return true;
  }
  return await fetchRates(state.source, force);
}

function getConvertedAmount(targetCode) {
  const rate = state.rates[targetCode];
  if (!rate) return null;
  return state.amount * rate;
}

// ============ SETUP SCREEN ============
let setupSource = null;
let setupTargets = new Set();

function initSetupScreen() {
  renderSetupSourceList();
  renderSetupTargetList();

  // Search handlers
  $('#setup-search-source').addEventListener('input', (e) => {
    renderSetupSourceList(e.target.value);
  });
  $('#setup-search-target').addEventListener('input', (e) => {
    renderSetupTargetList(e.target.value);
  });

  // Next button
  $('#setup-next-btn').addEventListener('click', () => {
    if (!setupSource) return;
    $('#setup-step-1').classList.add('hidden');
    $('#setup-step-2').classList.remove('hidden');
    // Don't show the selected source in targets
    renderSetupTargetList();
  });

  // Start button
  $('#setup-start-btn').addEventListener('click', () => {
    if (setupTargets.size === 0) return;
    state.source = setupSource;
    state.targets = [...setupTargets];
    state.amount = 1000;
    saveState();
    showMainScreen();
  });
}

function renderSetupSourceList(filter = '') {
  const container = $('#setup-source-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      return searchMatches(code, c, f);
    });
    if (entries.length === 0) continue;

    const header = document.createElement('div');
    header.className = 'category-header';
    header.textContent = CATEGORY_NAMES[cat];
    container.appendChild(header);

    for (const [code, cur] of entries) {
      const card = document.createElement('div');
      card.className = 'currency-card' + (setupSource === code ? ' selected' : '');
      card.innerHTML = `
        <div class="currency-flag">${getFlagHTML(code, 28)}</div>
        <div class="card-info">
          <div class="currency-code">${code.toUpperCase()}</div>
          <div class="currency-name">${cur.name}</div>
        </div>`;
      card.addEventListener('click', () => {
        setupSource = code;
        container.querySelectorAll('.currency-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        $('#setup-next-btn').disabled = false;
      });
      container.appendChild(card);
    }
  }
}

function renderSetupTargetList(filter = '') {
  const container = $('#setup-target-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      if (code === setupSource) return false;
      return searchMatches(code, c, f);
    });
    if (entries.length === 0) continue;

    const header = document.createElement('div');
    header.className = 'category-header';
    header.textContent = CATEGORY_NAMES[cat];
    container.appendChild(header);

    for (const [code, cur] of entries) {
      const card = document.createElement('div');
      card.className = 'currency-card' + (setupTargets.has(code) ? ' selected' : '');
      card.innerHTML = `
        <div class="currency-flag">${getFlagHTML(code, 28)}</div>
        <div class="card-info">
          <div class="currency-code">${code.toUpperCase()}</div>
          <div class="currency-name">${cur.name}</div>
        </div>`;
      card.addEventListener('click', () => {
        if (setupTargets.has(code)) {
          setupTargets.delete(code);
          card.classList.remove('selected');
        } else {
          setupTargets.add(code);
          card.classList.add('selected');
        }
        $('#setup-start-btn').disabled = setupTargets.size === 0;
        $('#setup-start-btn').textContent = setupTargets.size > 0
          ? t('startBtnCount', setupTargets.size)
          : t('startBtn');
      });
      container.appendChild(card);
    }
  }
}

// ============ MAIN SCREEN ============
async function showMainScreen() {
  $('#setup-screen').classList.add('hidden');
  $('#main-screen').classList.remove('hidden');

  updateSourceDisplay();
  updateAmountInput();
  renderConversionCards(true);

  // Fetch rates
  const ok = await ensureRates();
  if (!ok) {
    showToast(t('toastRatesCheckNet'));
  }
  renderConversionCards(false);
  updateLastUpdated();
}

function updateSourceDisplay() {
  const cur = CURRENCIES[state.source];
  $('#source-flag').innerHTML = getFlagHTML(state.source, 26);
  $('#source-code').textContent = state.source.toUpperCase();
}

function updateAmountInput() {
  $('#amount-input').value = formatAmountInput(state.amount);
}

function updateLastUpdated() {
  const el = $('#last-updated');
  if (!el) return;
  const locale = currentLang === 'en' ? 'en-US' : 'ru-RU';

  if (state.cacheTime) {
    const d = new Date(state.cacheTime);
    const timeStr = d.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
    const dateStr = d.toLocaleDateString(locale, { day: 'numeric', month: 'short' });
    el.textContent = t('ratesUpdated', dateStr, timeStr);
  } else if (state.rateDate) {
    const d = new Date(state.rateDate);
    const dateStr = !isNaN(d.getTime())
      ? d.toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' })
      : state.rateDate;
    el.textContent = t('ratesAsOf', dateStr);
  } else {
    el.textContent = t('ratesNotLoaded');
  }
}

function applyLanguage(lang) {
  currentLang = lang === 'en' ? 'en' : 'ru';
  try {
    localStorage.setItem(LANG_STORAGE_KEY, currentLang);
  } catch (e) {}
  document.documentElement.setAttribute('lang', currentLang);
  document.title = 'Currency converter';

  // Update header and setup titles
  const elAppName = $('#app-name');
  if (elAppName) elAppName.textContent = t('headerTitle');

  const elSetupTitle = $('#setup-title');
  if (elSetupTitle) elSetupTitle.textContent = t('headerTitle');

  // Update static UI elements
  const elSubtitle = $('#setup-subtitle');
  if (elSubtitle) elSubtitle.textContent = t('appSubtitle');

  const elStep1Title = $('#setup-step-1-title');
  if (elStep1Title) elStep1Title.textContent = t('setupStep1Title');

  const elSearchSource = $('#setup-search-source');
  if (elSearchSource) elSearchSource.placeholder = t('searchCurrency');

  const elNextBtn = $('#setup-next-btn');
  if (elNextBtn) elNextBtn.textContent = t('nextBtn');

  const elStep2Title = $('#setup-step-2-title');
  if (elStep2Title) elStep2Title.textContent = t('setupStep2Title');

  const elStep2Hint = $('#setup-step-2-hint');
  if (elStep2Hint) elStep2Hint.textContent = t('setupStep2Hint');

  const elSearchTarget = $('#setup-search-target');
  if (elSearchTarget) elSearchTarget.placeholder = t('searchCurrency');

  const elStartBtn = $('#setup-start-btn');
  if (elStartBtn) {
    elStartBtn.textContent = setupTargets.size > 0
      ? t('startBtnCount', setupTargets.size)
      : t('startBtn');
  }

  // Header tooltips
  const elRefreshBtn = $('#refresh-btn');
  if (elRefreshBtn) elRefreshBtn.title = t('refreshRates');

  const elExportBtn = $('#export-btn');
  if (elExportBtn) elExportBtn.title = t('exportPNG');

  const elSettingsBtn = $('#settings-btn');
  if (elSettingsBtn) elSettingsBtn.title = t('settings');

  updateThemeIcon(document.documentElement.getAttribute('data-theme') || getCurrentTheme());

  // Reorder toolbar
  const elReorderInfo = $('#reorder-info-text');
  if (elReorderInfo) elReorderInfo.textContent = t('reorderInfo');

  const elReorderCancel = $('#reorder-cancel-btn');
  if (elReorderCancel) elReorderCancel.textContent = t('cancel');

  const elReorderSave = $('#reorder-save-btn');
  if (elReorderSave) elReorderSave.textContent = t('save');

  // Tip link
  const elTipText = $('#tip-text');
  if (elTipText) elTipText.textContent = t('buyCoffee');

  // Settings modal
  const elSettingsTitle = $('#settings-title');
  if (elSettingsTitle) elSettingsTitle.textContent = t('settings');

  const elSettingsLangTitle = $('#settings-lang-title');
  if (elSettingsLangTitle) elSettingsLangTitle.textContent = t('language');

  const elSettingsSourceTitle = $('#settings-source-title');
  if (elSettingsSourceTitle) elSettingsSourceTitle.textContent = t('sourceCurrency');

  const elSettingsSearchSource = $('#settings-search-source');
  if (elSettingsSearchSource) elSettingsSearchSource.placeholder = t('searchShort');

  const elSettingsTargetTitle = $('#settings-target-title');
  if (elSettingsTargetTitle) elSettingsTargetTitle.textContent = t('targetCurrencies');

  const elSettingsSearchTarget = $('#settings-search-target');
  if (elSettingsSearchTarget) elSettingsSearchTarget.placeholder = t('searchShort');

  const elSettingsSave = $('#settings-save');
  if (elSettingsSave) elSettingsSave.textContent = t('save');

  // Source picker modal
  const elSourcePickerTitle = $('#source-picker-title');
  if (elSourcePickerTitle) elSourcePickerTitle.textContent = t('sourceCurrency');

  const elSourcePickerSearch = $('#source-picker-search');
  if (elSourcePickerSearch) elSourcePickerSearch.placeholder = t('searchShort');

  // Update language selector active states
  $$('.lang-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.lang === currentLang);
  });

  // Re-render conversion cards and input formatting
  updateAmountInput();
  updateLastUpdated();
  if (!$('#main-screen').classList.contains('hidden')) {
    renderConversionCards(false);
  }
}

// ============ REORDER / DRAG & DROP ============
let isReordering = false;
let reorderOriginalTargets = [];
let touchDragCard = null;
let touchLongPressTimer = null;
let touchMoved = false;

function enterReorderMode() {
  if (isReordering) return;
  isReordering = true;
  reorderOriginalTargets = [...state.targets];
  $('#reorder-toolbar')?.classList.remove('hidden');
  $('#conversion-cards')?.classList.add('is-editing');
  showToast(t('toastReorderMode'));
}

function saveReorder() {
  if (!isReordering) return;
  const container = $('#conversion-cards');
  const cards = [...container.querySelectorAll('.conv-card')];
  const newOrder = cards.map(c => c.dataset.code).filter(Boolean);
  if (newOrder.length > 0) {
    state.targets = newOrder;
    saveState();
  }
  exitReorderMode();
  renderConversionCards(false);
  showToast(t('toastReorderSaved'));
}

function cancelReorder() {
  if (!isReordering) return;
  state.targets = [...reorderOriginalTargets];
  exitReorderMode();
  renderConversionCards(false);
  showToast(t('toastReorderCancelled'));
}

function exitReorderMode() {
  isReordering = false;
  $('#reorder-toolbar')?.classList.add('hidden');
  $('#conversion-cards')?.classList.remove('is-editing');
  cleanupTouchDrag();
}

function cleanupTouchDrag() {
  if (touchLongPressTimer) {
    clearTimeout(touchLongPressTimer);
    touchLongPressTimer = null;
  }
  if (touchDragCard) {
    touchDragCard.classList.remove('is-dragging');
    touchDragCard = null;
  }
  document.querySelectorAll('.conv-card.drag-over').forEach(el => el.classList.remove('drag-over'));
}

function renderConversionCards(loading = false) {
  const container = $('#conversion-cards');
  container.innerHTML = '';

  if (isReordering) {
    container.classList.add('is-editing');
    $('#reorder-toolbar')?.classList.remove('hidden');
  } else {
    container.classList.remove('is-editing');
    $('#reorder-toolbar')?.classList.add('hidden');
  }

  let cardIndex = 0;
  for (const code of state.targets) {
    const cur = CURRENCIES[code] || { name: code.toUpperCase(), symbol: '' };
    const amount = loading ? null : getConvertedAmount(code);
    const rate = state.rates[code];
    const unitRate = rate ? formatNumber(rate, code) : '';

    const card = document.createElement('div');
    card.className = 'conv-card';
    card.dataset.code = code;
    card.style.setProperty('--card-index', cardIndex++);
    card.setAttribute('draggable', 'true');

    card.innerHTML = `
      <div class="conv-drag-handle" title="${t('holdToDrag')}">
        <svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M9 3h2v2H9V3zm4 0h2v2h-2V3zM9 7h2v2H9V7zm4 0h2v2h-2V7zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2z"/></svg>
      </div>
      <div class="conv-flag">${getFlagHTML(code, 40)}</div>
      <div class="conv-info">
        <div class="conv-code">${code.toUpperCase()}</div>
        <div class="conv-name">${cur.name}</div>
      </div>
      <div class="conv-right">
        <div class="conv-amount ${loading ? 'loading' : ''}">${loading ? '...' : formatNumber(amount, code)}</div>
        ${unitRate ? `<div class="conv-rate">${t('unitRate', state.source.toUpperCase(), unitRate)}</div>` : ''}
      </div>
      <button class="conv-delete" title="${t('deleteCurrency')}" data-code="${code}">&times;</button>`;

    // --- HTML5 Desktop Drag & Drop ---
    card.addEventListener('dragstart', (e) => {
      if (!isReordering) {
        enterReorderMode();
      }
      card.classList.add('is-dragging');
      e.dataTransfer.setData('text/plain', code);
      e.dataTransfer.effectAllowed = 'move';
    });

    card.addEventListener('dragend', () => {
      card.classList.remove('is-dragging');
      document.querySelectorAll('.conv-card.drag-over').forEach(el => el.classList.remove('drag-over'));
    });

    card.addEventListener('dragover', (e) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      const dragging = container.querySelector('.is-dragging');
      if (dragging && dragging !== card) {
        const rect = card.getBoundingClientRect();
        const isAfter = (e.clientY - rect.top) / rect.height > 0.5;
        container.insertBefore(dragging, isAfter ? card.nextSibling : card);
      }
    });

    // --- Mobile Touch & Long Press ---
    let touchStartX = 0;
    let touchStartY = 0;

    card.addEventListener('touchstart', (e) => {
      if (e.touches.length !== 1) return;
      const touch = e.touches[0];
      touchStartX = touch.clientX;
      touchStartY = touch.clientY;
      touchMoved = false;

      if (!isReordering) {
        // Long press detection to enter edit mode on phone
        touchLongPressTimer = setTimeout(() => {
          if (!touchMoved) {
            if (navigator.vibrate) {
              try { navigator.vibrate(50); } catch (_) {}
            }
            enterReorderMode();
            touchDragCard = card;
            card.classList.add('is-dragging');
          }
        }, 420);
      } else {
        // Already in reorder mode: start dragging immediately
        touchDragCard = card;
        card.classList.add('is-dragging');
      }
    }, { passive: false });

    card.addEventListener('touchmove', (e) => {
      if (!e.touches.length) return;
      const touch = e.touches[0];
      const deltaX = Math.abs(touch.clientX - touchStartX);
      const deltaY = Math.abs(touch.clientY - touchStartY);

      if (deltaX > 8 || deltaY > 8) {
        touchMoved = true;
      }

      if (!isReordering) {
        if (touchMoved && touchLongPressTimer) {
          clearTimeout(touchLongPressTimer);
          touchLongPressTimer = null;
        }
      } else {
        if (touchDragCard) {
          e.preventDefault();
          const targetEl = document.elementFromPoint(touch.clientX, touch.clientY);
          const targetCard = targetEl?.closest('.conv-card');
          if (targetCard && targetCard !== touchDragCard && container.contains(targetCard)) {
            const rect = targetCard.getBoundingClientRect();
            const isAfter = touch.clientY > (rect.top + rect.height / 2);
            container.insertBefore(touchDragCard, isAfter ? targetCard.nextSibling : targetCard);
          }
        }
      }
    }, { passive: false });

    card.addEventListener('touchend', () => {
      cleanupTouchDrag();
    });

    card.addEventListener('touchcancel', () => {
      cleanupTouchDrag();
    });

    // Delete handler
    card.querySelector('.conv-delete')?.addEventListener('click', (e) => {
      e.stopPropagation();
      if (isReordering) return;
      state.targets = state.targets.filter(t => t !== code);
      saveState();
      renderConversionCards(false);
      showToast(t('toastCurrencyRemoved', code.toUpperCase()));
    });

    container.appendChild(card);
  }
}

// ============ SETTINGS MODAL ============
let settingsSource = null;
let settingsTargets = new Set();

function openSettings() {
  if (isReordering) cancelReorder();
  settingsSource = state.source;
  settingsTargets = new Set(state.targets);

  // Sync active language buttons
  $$('.lang-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.lang === currentLang);
  });

  renderSettingsSourceList();
  renderSettingsTargetList();
  $('#settings-modal').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeSettings() {
  $('#settings-modal').classList.add('hidden');
  document.body.style.overflow = '';
}

function saveSettings() {
  const sourceChanged = settingsSource !== state.source;
  state.source = settingsSource;
  state.targets = [...settingsTargets];
  saveState();
  closeSettings();

  updateSourceDisplay();
  renderConversionCards(sourceChanged);

  if (sourceChanged) {
    // Refetch rates for new source
    (async () => {
      const ok = await fetchRates(state.source);
      if (!ok) showToast(t('toastRatesError'));
      renderConversionCards(false);
      updateLastUpdated();
    })();
  }

  showToast(t('toastSettingsSaved'));
}

function renderSettingsSourceList(filter = '') {
  const container = $('#settings-source-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      return searchMatches(code, c, f);
    });
    if (entries.length === 0) continue;

    const header = document.createElement('div');
    header.className = 'cat-header';
    header.textContent = CATEGORY_NAMES[cat];
    container.appendChild(header);

    for (const [code, cur] of entries) {
      const item = document.createElement('div');
      item.className = 'cur-item' + (settingsSource === code ? ' selected' : '');
      item.innerHTML = `
        <div class="cur-flag">${getFlagHTML(code, 24)}</div>
        <span class="cur-code">${code.toUpperCase()}</span>
        <span class="cur-name">${cur.name}</span>
        <span class="cur-check">✓</span>`;
      item.addEventListener('click', () => {
        settingsSource = code;
        container.querySelectorAll('.cur-item').forEach(i => i.classList.remove('selected'));
        item.classList.add('selected');
      });
      container.appendChild(item);
    }
  }
}

function renderSettingsTargetList(filter = '') {
  const container = $('#settings-target-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      return searchMatches(code, c, f);
    });
    if (entries.length === 0) continue;

    const header = document.createElement('div');
    header.className = 'cat-header';
    header.textContent = CATEGORY_NAMES[cat];
    container.appendChild(header);

    for (const [code, cur] of entries) {
      const item = document.createElement('div');
      item.className = 'cur-item' + (settingsTargets.has(code) ? ' selected' : '');
      item.innerHTML = `
        <div class="cur-flag">${getFlagHTML(code, 24)}</div>
        <span class="cur-code">${code.toUpperCase()}</span>
        <span class="cur-name">${cur.name}</span>
        <span class="cur-check">✓</span>`;
      item.addEventListener('click', () => {
        if (settingsTargets.has(code)) {
          settingsTargets.delete(code);
          item.classList.remove('selected');
        } else {
          settingsTargets.add(code);
          item.classList.add('selected');
        }
      });
      container.appendChild(item);
    }
  }
}

// ============ SOURCE PICKER (quick change) ============
function openSourcePicker() {
  renderSourcePickerList();
  $('#source-picker-modal').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeSourcePicker() {
  $('#source-picker-modal').classList.add('hidden');
  document.body.style.overflow = '';
}

function renderSourcePickerList(filter = '') {
  const container = $('#source-picker-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      return searchMatches(code, c, f);
    });
    if (entries.length === 0) continue;

    const header = document.createElement('div');
    header.className = 'cat-header';
    header.textContent = CATEGORY_NAMES[cat];
    container.appendChild(header);

    for (const [code, cur] of entries) {
      const item = document.createElement('div');
      item.className = 'cur-item' + (state.source === code ? ' selected' : '');
      item.innerHTML = `
        <div class="cur-flag">${getFlagHTML(code, 24)}</div>
        <span class="cur-code">${code.toUpperCase()}</span>
        <span class="cur-name">${cur.name}</span>
        <span class="cur-check">✓</span>`;
      item.addEventListener('click', async () => {
        state.source = code;
        saveState();
        closeSourcePicker();
        updateSourceDisplay();
        renderConversionCards(true);
        const ok = await fetchRates(state.source);
        if (!ok) showToast(t('toastRatesError'));
        renderConversionCards(false);
        updateLastUpdated();
      });
      container.appendChild(item);
    }
  }
}

// ============ PNG EXPORT ============
async function exportToPNG() {
  showToast(t('toastExportStart'));

  try {
    if (typeof html2canvas !== 'function') {
      throw new Error('html2canvas library is not loaded');
    }

    let container = $('#export-container');

    if (!container) {
      container = document.createElement('div');
      container.id = 'export-container';
      container.className = 'export-container';
      document.body.appendChild(container);
    }

    const allExportCodes = [state.source, ...state.targets];

    // Preload flags
    await Promise.all(
      allExportCodes.map(code => getFlagDataUrl(code))
    );

    let itemsHTML = '';

    for (const code of allExportCodes) {
      const isSource = code === state.source;
      const cur = CURRENCIES[code] || {
        name: code.toUpperCase(),
        symbol: ''
      };

      const val = isSource
        ? state.amount
        : getConvertedAmount(code);

      itemsHTML += `
        <div class="export-chip ${isSource ? 'source-chip' : ''}">
          <div class="chip-flag">
            ${getExportFlagHTML(code, 28)}
          </div>

          <div class="chip-info">
            <div class="chip-val">
              ${formatNumber(val, code)}
            </div>

            <div class="chip-label">
              ${code.toUpperCase()} · ${cur.name}
            </div>
          </div>
        </div>`;
    }

    container.innerHTML = `
      <div class="export-card">
        <div class="export-grid">
          ${itemsHTML}
        </div>
      </div>`;

    // Wait for images
    const images = [...container.querySelectorAll('img')];

    await Promise.all(
      images.map(async img => {
        if (img.complete && img.naturalWidth > 0) return;

        await new Promise(resolve => {
          img.onload = resolve;
          img.onerror = resolve;
          setTimeout(resolve, 5000);
        });
      })
    );

    if (document.fonts?.ready) {
      await document.fonts.ready;
    }

    await new Promise(resolve => setTimeout(resolve, 150));

    const exportEl = container.querySelector('.export-card');

    const canvas = await html2canvas(exportEl, {
      backgroundColor: null,
      scale: 2,
      useCORS: true,
      allowTaint: false,
      logging: false
    });

    const blob = await new Promise(resolve => {
      canvas.toBlob(resolve, 'image/png');
    });

    if (!blob) {
      throw new Error('Failed to create image blob');
    }

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    link.download = `prices-${state.source}-${timestamp}.png`;

    link.href = url;
    document.body.appendChild(link);
    link.click();
    link.remove();

    setTimeout(() => URL.revokeObjectURL(url), 1000);

    showToast(t('toastExportDone'));

  } catch (error) {
    console.error('Export error:', error);
    showToast(t('toastExportError', error.message), 5000);
  }
}

// ============ AMOUNT INPUT HANDLING ============
function initAmountInput() {
  const input = $('#amount-input');
  if (!input) return;

  input.addEventListener('input', () => {
    // Sanitize: allow digits, dot, comma, spaces, and minus
    const clean = input.value.replace(/[^\d.,\s\-]/g, '');
    if (clean !== input.value) {
      const pos = Math.max(0, input.selectionStart - (input.value.length - clean.length));
      input.value = clean;
      input.setSelectionRange(pos, pos);
    }

    const trimmed = input.value.trim();
    if (trimmed === '' || trimmed === '.' || trimmed === ',' || trimmed === '-') {
      state.amount = 0;
      saveState();
      renderConversionCards(false);
      return;
    }

    const num = parseAmountInput(input.value);
    state.amount = num;
    saveState();

    // Re-render conversion cards with the new amount in real time.
    // Crucially: We do NOT reformat or overwrite input.value here while typing.
    // This eliminates:
    // 1) The comma bug in ENG mode where 1,000 was converted to 1.000 and blocked typing > 1000.
    // 2) Erasing decimal separators (dots or commas) and trailing zeros when typing fractions (e.g. 10.5).
    // 3) Cursor jumping and backspace trapping.
    renderConversionCards(false);
  });

  input.addEventListener('blur', () => {
    const trimmed = input.value.trim();
    if (trimmed === '' || isNaN(state.amount) || state.amount === 0) {
      state.amount = 0;
      input.value = '0';
      saveState();
      renderConversionCards(false);
    } else {
      // Normalize cleanly on blur (e.g. cleans up pasted "1,000,000" or trailing dot "100." -> "100")
      input.value = formatAmountInput(state.amount);
    }
  });

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      input.blur();
    }
  });

  input.addEventListener('focus', () => {
    // Select all on focus for easy replacement
    setTimeout(() => input.select(), 50);
  });
}

// ============ EVENT LISTENERS ============
function initEventListeners() {
  // Header buttons
  $('#refresh-btn')?.addEventListener('click', async () => {
    const btn = $('#refresh-btn');
    btn?.classList.add('spinning');
    showToast(t('toastRatesUpdating'));
    const ok = await fetchRates(state.source, true);
    btn?.classList.remove('spinning');
    if (ok) {
      renderConversionCards(false);
      updateLastUpdated();
      showToast(t('toastRatesUpdated'));
    } else {
      showToast(t('toastRatesError'));
    }
  });

  $('#theme-btn')?.addEventListener('click', toggleTheme);
  $('#export-btn')?.addEventListener('click', exportToPNG);
  $('#settings-btn')?.addEventListener('click', openSettings);

  // Language selector buttons in settings modal
  $$('.lang-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const lang = btn.dataset.lang;
      applyLanguage(lang);
      renderSettingsSourceList($('#settings-search-source')?.value || '');
      renderSettingsTargetList($('#settings-search-target')?.value || '');
    });
  });

  // Reorder buttons
  $('#reorder-save-btn')?.addEventListener('click', saveReorder);
  $('#reorder-cancel-btn')?.addEventListener('click', cancelReorder);

  // Settings modal
  $('#settings-close')?.addEventListener('click', closeSettings);
  $('#settings-save')?.addEventListener('click', saveSettings);
  $('#settings-modal .modal-overlay')?.addEventListener('click', closeSettings);

  $('#settings-search-source')?.addEventListener('input', (e) => {
    renderSettingsSourceList(e.target.value);
  });
  $('#settings-search-target')?.addEventListener('input', (e) => {
    renderSettingsTargetList(e.target.value);
  });

  // Source picker
  $('#source-btn')?.addEventListener('click', openSourcePicker);
  $('#source-picker-close')?.addEventListener('click', closeSourcePicker);
  $('#source-picker-modal .modal-overlay')?.addEventListener('click', closeSourcePicker);
  $('#source-picker-search')?.addEventListener('input', (e) => {
    renderSourcePickerList(e.target.value);
  });

  // Keyboard: Escape closes modals or exits reorder mode
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if ($('#settings-modal') && !$('#settings-modal').classList.contains('hidden')) closeSettings();
      if ($('#source-picker-modal') && !$('#source-picker-modal').classList.contains('hidden')) closeSourcePicker();
      if (isReordering) cancelReorder();
    }
  });
}

// ============ INIT ============
async function init() {
  // Apply language immediately
  applyLanguage(currentLang);

  // Apply saved or system theme immediately
  applyTheme(getCurrentTheme());

  const hasData = loadState();

  if (hasData && state.source && state.targets.length > 0) {
    // Returning user → main screen
    showMainScreen();
  } else {
    // First visit → setup screen
    $('#setup-screen').classList.remove('hidden');
    initSetupScreen();
  }

  initAmountInput();
  initEventListeners();
}

// Expose on window for convenience
if (typeof window !== 'undefined') {
  window.applyLanguage = applyLanguage;
  window.toggleTheme = toggleTheme;
}

// Start when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
