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
const CACHE_MAX_AGE     = 15 * 60 * 1000; // 15 minutes fresh cache

// ============ CURRENCY DATABASE ============
const CURRENCIES = {
  // — Popular fiat —
  rub: { name: 'Российский рубль',     symbol: '₽',  cc: 'ru',  cat: 'popular' },
  usd: { name: 'Доллар США',           symbol: '$',  cc: 'us',  cat: 'popular' },
  eur: { name: 'Евро',                 symbol: '€',  cc: 'eu',  cat: 'popular' },
  gbp: { name: 'Фунт стерлингов',      symbol: '£',  cc: 'gb',  cat: 'popular' },
  cny: { name: 'Китайский юань',       symbol: '¥',  cc: 'cn',  cat: 'popular' },
  jpy: { name: 'Японская иена',        symbol: '¥',  cc: 'jp',  cat: 'popular' },
  chf: { name: 'Швейцарский франк',    symbol: 'Fr', cc: 'ch',  cat: 'popular' },
  try: { name: 'Турецкая лира',        symbol: '₺',  cc: 'tr',  cat: 'popular' },
  inr: { name: 'Индийская рупия',      symbol: '₹',  cc: 'in',  cat: 'popular' },
  krw: { name: 'Южнокорейская вона',   symbol: '₩',  cc: 'kr',  cat: 'popular' },

  // — CIS & neighbours —
  byn: { name: 'Белорусский рубль',    symbol: 'Br', cc: 'by',  cat: 'cis' },
  uah: { name: 'Украинская гривна',    symbol: '₴',  cc: 'ua',  cat: 'cis' },
  kzt: { name: 'Казахский тенге',      symbol: '₸',  cc: 'kz',  cat: 'cis' },
  uzs: { name: 'Узбекский сум',        symbol: 'сўм',cc: 'uz',  cat: 'cis' },
  gel: { name: 'Грузинский лари',      symbol: '₾',  cc: 'ge',  cat: 'cis' },
  azn: { name: 'Азербайджанский манат', symbol: '₼', cc: 'az',  cat: 'cis' },
  amd: { name: 'Армянский драм',       symbol: '֏',  cc: 'am',  cat: 'cis' },
  mdl: { name: 'Молдавский лей',       symbol: 'L',  cc: 'md',  cat: 'cis' },
  kgs: { name: 'Киргизский сом',       symbol: 'С',  cc: 'kg',  cat: 'cis' },
  tjs: { name: 'Таджикский сомони',    symbol: 'SM', cc: 'tj',  cat: 'cis' },

  // — Other fiat —
  cad: { name: 'Канадский доллар',     symbol: 'C$', cc: 'ca',  cat: 'fiat' },
  aud: { name: 'Австралийский доллар',  symbol: 'A$', cc: 'au',  cat: 'fiat' },
  nzd: { name: 'Новозеландский доллар', symbol: 'NZ$',cc: 'nz', cat: 'fiat' },
  sgd: { name: 'Сингапурский доллар',  symbol: 'S$', cc: 'sg',  cat: 'fiat' },
  hkd: { name: 'Гонконгский доллар',   symbol: 'HK$',cc: 'hk', cat: 'fiat' },
  sek: { name: 'Шведская крона',       symbol: 'kr', cc: 'se',  cat: 'fiat' },
  nok: { name: 'Норвежская крона',     symbol: 'kr', cc: 'no',  cat: 'fiat' },
  dkk: { name: 'Датская крона',        symbol: 'kr', cc: 'dk',  cat: 'fiat' },
  pln: { name: 'Польский злотый',      symbol: 'zł', cc: 'pl',  cat: 'fiat' },
  czk: { name: 'Чешская крона',        symbol: 'Kč', cc: 'cz',  cat: 'fiat' },
  huf: { name: 'Венгерский форинт',    symbol: 'Ft', cc: 'hu',  cat: 'fiat' },
  ron: { name: 'Румынский лей',        symbol: 'lei',cc: 'ro',  cat: 'fiat' },
  bgn: { name: 'Болгарский лев',       symbol: 'лв', cc: 'bg',  cat: 'fiat' },
  ils: { name: 'Израильский шекель',   symbol: '₪',  cc: 'il',  cat: 'fiat' },
  zar: { name: 'Южноафриканский рэнд', symbol: 'R',  cc: 'za',  cat: 'fiat' },
  brl: { name: 'Бразильский реал',     symbol: 'R$', cc: 'br',  cat: 'fiat' },
  mxn: { name: 'Мексиканское песо',    symbol: 'Mex$',cc:'mx',  cat: 'fiat' },
  ars: { name: 'Аргентинское песо',    symbol: 'AR$',cc: 'ar',  cat: 'fiat' },
  thb: { name: 'Тайский бат',          symbol: '฿',  cc: 'th',  cat: 'fiat' },
  idr: { name: 'Индонезийская рупия',  symbol: 'Rp', cc: 'id',  cat: 'fiat' },
  myr: { name: 'Малайзийский ринггит', symbol: 'RM', cc: 'my',  cat: 'fiat' },
  php: { name: 'Филиппинское песо',    symbol: '₱',  cc: 'ph',  cat: 'fiat' },
  vnd: { name: 'Вьетнамский донг',     symbol: '₫',  cc: 'vn',  cat: 'fiat' },
  egp: { name: 'Египетский фунт',      symbol: 'E£', cc: 'eg',  cat: 'fiat' },
  aed: { name: 'Дирхам ОАЭ',          symbol: 'د.إ',cc: 'ae',  cat: 'fiat' },
  sar: { name: 'Саудовский риял',      symbol: '﷼',  cc: 'sa',  cat: 'fiat' },
  qar: { name: 'Катарский риял',       symbol: 'QR', cc: 'qa',  cat: 'fiat' },
  twd: { name: 'Тайваньский доллар',   symbol: 'NT$',cc: 'tw',  cat: 'fiat' },
  rsd: { name: 'Сербский динар',       symbol: 'din',cc: 'rs',  cat: 'fiat' },

  // — Crypto —
  btc:  { name: 'Bitcoin',    symbol: '₿',   cc: null, cat: 'crypto', color: '#f7931a' },
  eth:  { name: 'Ethereum',   symbol: 'Ξ',   cc: null, cat: 'crypto', color: '#627eea' },
  usdt: { name: 'Tether',     symbol: '₮',   cc: null, cat: 'crypto', color: '#26a17b' },
  bnb:  { name: 'BNB',        symbol: 'BNB', cc: null, cat: 'crypto', color: '#f3ba2f' },
  xrp:  { name: 'Ripple',     symbol: 'XRP', cc: null, cat: 'crypto', color: '#00aae4' },
  sol:  { name: 'Solana',     symbol: 'SOL', cc: null, cat: 'crypto', color: '#9945ff' },
  ada:  { name: 'Cardano',    symbol: 'ADA', cc: null, cat: 'crypto', color: '#0033ad' },
  doge: { name: 'Dogecoin',   symbol: 'Ð',   cc: null, cat: 'crypto', color: '#c2a633' },
  dot:  { name: 'Polkadot',   symbol: 'DOT', cc: null, cat: 'crypto', color: '#e6007a' },
  ltc:  { name: 'Litecoin',   symbol: 'Ł',   cc: null, cat: 'crypto', color: '#345d9d' },
  ton:  { name: 'Toncoin',    symbol: 'TON', cc: null, cat: 'crypto', color: '#0098ea' },
  trx:  { name: 'TRON',       symbol: 'TRX', cc: null, cat: 'crypto', color: '#ff0013' },
};

const CATEGORY_NAMES = {
  popular: '⭐ Популярные',
  cis:     '🌍 СНГ и соседи',
  fiat:    '💵 Другие валюты',
  crypto:  '🪙 Криптовалюты',
};

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
  const current = document.documentElement.getAttribute('data-theme') || getCurrentTheme();
  const next = current === 'dark' ? 'light' : 'dark';
  applyTheme(next);
  showToast(next === 'dark' ? '🌙 Тёмная тема' : '☀️ Светлая тема');
}

function updateThemeIcon(theme) {
  const iconEl = $('#theme-icon');
  const btnEl = $('#theme-btn');
  if (!iconEl) return;
  if (theme === 'dark') {
    // Sun icon to switch to light
    iconEl.innerHTML = `<path fill="currentColor" d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 00-1.41 0 .996.996 0 000 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 0 00-1.41 0 .996.996 0 000 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 000-1.41.996.996 0 00-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36l-1.06 1.06a.996.996 0 000 1.41c.39.39 1.03.39 1.41 0l1.06-1.06c.39-.39.39-1.03 0-1.41s-1.02-.39-1.41 0z"/>`;
    if (btnEl) btnEl.title = 'Переключить на светлую тему';
  } else {
    // Moon icon to switch to dark
    iconEl.innerHTML = `<path fill="currentColor" d="M12.3 2a10 10 0 00-1.9 19.8 10 10 0 0010.5-12.8A8 8 0 0112.3 2z"/>`;
    if (btnEl) btnEl.title = 'Переключить на тёмную тему';
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

  return new Intl.NumberFormat('ru-RU', {
    minimumFractionDigits: 0,
    maximumFractionDigits: decimals,
  }).format(num);
}

function parseAmountInput(val) {
  // Allow both . and , as decimal separators; strip spaces
  const cleaned = val.replace(/\s/g, '').replace(',', '.');
  const num = parseFloat(cleaned);
  return isNaN(num) ? 0 : num;
}

function formatAmountInput(num) {
  if (!num && num !== 0) return '';
  return new Intl.NumberFormat('ru-RU', {
    maximumFractionDigits: 10,
    useGrouping: true,
  }).format(num);
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
      if (!f) return true;
      return code.includes(f) || c.name.toLowerCase().includes(f) || (c.symbol && c.symbol.toLowerCase().includes(f));
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
      if (!f) return true;
      return code.includes(f) || c.name.toLowerCase().includes(f) || (c.symbol && c.symbol.toLowerCase().includes(f));
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
          ? `Начать (${setupTargets.size}) 🚀`
          : 'Начать 🚀';
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
    showToast('⚠️ Не удалось загрузить курсы. Проверьте интернет.');
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
  if (state.cacheTime) {
    const d = new Date(state.cacheTime);
    const timeStr = d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    const dateStr = d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    el.textContent = `Курсы обновлены: ${dateStr}, ${timeStr}`;
  } else if (state.rateDate) {
    const d = new Date(state.rateDate);
    const dateStr = !isNaN(d.getTime())
      ? d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
      : state.rateDate;
    el.textContent = `Курсы на ${dateStr}`;
  } else {
    el.textContent = 'Курсы не загружены';
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
  showToast('Режим изменения порядка: перетащите карточки');
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
  showToast('✓ Порядок сохранён');
}

function cancelReorder() {
  if (!isReordering) return;
  state.targets = [...reorderOriginalTargets];
  exitReorderMode();
  renderConversionCards(false);
  showToast('Изменения отменены');
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

  for (const code of state.targets) {
    const cur = CURRENCIES[code] || { name: code.toUpperCase(), symbol: '' };
    const amount = loading ? null : getConvertedAmount(code);
    const rate = state.rates[code];
    const unitRate = rate ? formatNumber(rate, code) : '';

    const card = document.createElement('div');
    card.className = 'conv-card';
    card.dataset.code = code;
    card.setAttribute('draggable', 'true');

    card.innerHTML = `
      <div class="conv-drag-handle" title="Зажмите для перемещения">
        <svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M9 3h2v2H9V3zm4 0h2v2h-2V3zM9 7h2v2H9V7zm4 0h2v2h-2V7zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2zm-4 4h2v2H9v-2zm4 0h2v2h-2v-2z"/></svg>
      </div>
      <div class="conv-flag">${getFlagHTML(code, 40)}</div>
      <div class="conv-info">
        <div class="conv-code">${code.toUpperCase()}</div>
        <div class="conv-name">${cur.name}</div>
      </div>
      <div class="conv-right">
        <div class="conv-amount ${loading ? 'loading' : ''}">${loading ? '...' : formatNumber(amount, code)}</div>
        ${unitRate ? `<div class="conv-rate">1 ${state.source.toUpperCase()} = ${unitRate}</div>` : ''}
      </div>
      <button class="conv-delete" title="Удалить" data-code="${code}">&times;</button>`;

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
      showToast(`${code.toUpperCase()} удалён`);
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
      if (!ok) showToast('⚠️ Не удалось загрузить курсы');
      renderConversionCards(false);
      updateLastUpdated();
    })();
  }

  showToast('✓ Настройки сохранены');
}

function renderSettingsSourceList(filter = '') {
  const container = $('#settings-source-list');
  container.innerHTML = '';
  const f = filter.toLowerCase().trim();

  for (const cat of CATEGORY_ORDER) {
    const entries = Object.entries(CURRENCIES).filter(([code, c]) => {
      if (c.cat !== cat) return false;
      if (!f) return true;
      return code.includes(f) || c.name.toLowerCase().includes(f);
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
      if (!f) return true;
      return code.includes(f) || c.name.toLowerCase().includes(f);
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
      if (!f) return true;
      return code.includes(f) || c.name.toLowerCase().includes(f);
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
        if (!ok) showToast('⚠️ Не удалось загрузить курсы');
        renderConversionCards(false);
        updateLastUpdated();
      });
      container.appendChild(item);
    }
  }
}

// ============ PNG EXPORT ============
async function exportToPNG() {
  showToast('📸 Генерация изображения...');

  try {
    if (typeof html2canvas !== 'function') {
      throw new Error('Библиотека html2canvas не загружена');
    }

    let container = $('#export-container');

    if (!container) {
      container = document.createElement('div');
      container.id = 'export-container';
      container.className = 'export-container';
      document.body.appendChild(container);
    }

    const allExportCodes = [state.source, ...state.targets];

    // Загружаем флаги
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

    // Дожидаемся загрузки изображений
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
      logging: true
    });

    // Формируем PNG
    const blob = await new Promise(resolve => {
      canvas.toBlob(resolve, 'image/png');
    });

    if (!blob) {
      throw new Error('Не удалось сформировать PNG');
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

    showToast('✓ Изображение сохранено!');

  } catch (error) {
    console.error('Export error:', error);
    showToast('⚠️ Ошибка экспорта: ' + error.message, 5000);
  }
}

// ============ AMOUNT INPUT HANDLING ============
function initAmountInput() {
  const input = $('#amount-input');
  let rawValue = String(state.amount);

  input.addEventListener('input', () => {
    const pos = input.selectionStart;
    const oldLen = input.value.length;

    // Parse the raw number
    rawValue = input.value.replace(/[^\d.,\-]/g, '');
    const num = parseAmountInput(rawValue);
    state.amount = num;
    saveState();

    // Format display
    const formatted = formatAmountInput(num);
    input.value = formatted;

    // Try to preserve cursor position
    const newLen = formatted.length;
    const diff = newLen - oldLen;
    input.setSelectionRange(pos + diff, pos + diff);

    // Re-render cards
    renderConversionCards(false);
  });

  input.addEventListener('focus', () => {
    // Select all on focus for easy replacement
    setTimeout(() => input.select(), 50);
  });
}

// ============ EVENT LISTENERS ============
function initEventListeners() {
  // Header buttons
  $('#refresh-btn').addEventListener('click', async () => {
    const btn = $('#refresh-btn');
    btn.classList.add('spinning');
    showToast('🔄 Обновление курсов...');
    const ok = await fetchRates(state.source, true);
    btn.classList.remove('spinning');
    if (ok) {
      renderConversionCards(false);
      updateLastUpdated();
      showToast('✓ Курсы обновлены');
    } else {
      showToast('⚠️ Не удалось обновить');
    }
  });

  $('#theme-btn')?.addEventListener('click', toggleTheme);
  $('#export-btn').addEventListener('click', exportToPNG);
  $('#settings-btn').addEventListener('click', openSettings);

  // Reorder buttons
  $('#reorder-save-btn')?.addEventListener('click', saveReorder);
  $('#reorder-cancel-btn')?.addEventListener('click', cancelReorder);

  // Settings modal
  $('#settings-close').addEventListener('click', closeSettings);
  $('#settings-save').addEventListener('click', saveSettings);
  $('#settings-modal .modal-overlay').addEventListener('click', closeSettings);

  $('#settings-search-source').addEventListener('input', (e) => {
    renderSettingsSourceList(e.target.value);
  });
  $('#settings-search-target').addEventListener('input', (e) => {
    renderSettingsTargetList(e.target.value);
  });

  // Source picker
  $('#source-btn').addEventListener('click', openSourcePicker);
  $('#source-picker-close').addEventListener('click', closeSourcePicker);
  $('#source-picker-modal .modal-overlay').addEventListener('click', closeSourcePicker);
  $('#source-picker-search').addEventListener('input', (e) => {
    renderSourcePickerList(e.target.value);
  });

  // Keyboard: Escape closes modals or exits reorder mode
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (!$('#settings-modal').classList.contains('hidden')) closeSettings();
      if (!$('#source-picker-modal').classList.contains('hidden')) closeSourcePicker();
      if (isReordering) cancelReorder();
    }
  });
}

// ============ INIT ============
async function init() {
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

// Start when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
