# Currency Converter - Currency & Crypto Converter

A clean, fast, and responsive web converter for world fiat currencies and cryptocurrencies, featuring live exchange rates and PNG export.

**Live Demo:** [https://sashayio.github.io/currency-converter/](https://sashayio.github.io/currency-converter/)

---

## Features

- **Fiat Currencies:** USD, EUR, RUB, GBP, CNY, JPY, CHF, TRY, CIS currencies (BYN, KZT, UAH, UZS, GEL, AZN, AMD, etc.), and other major world currencies.
- **Real-Time Cryptocurrencies:** Bitcoin (BTC), Ethereum (ETH), Tether (USDT), Solana (SOL), TON, BNB, XRP, Dogecoin, and more.
- **Reliable Exchange Rates:**
  - Fiat rates update every hour via Open ExchangeRate-API.
  - Crypto spot prices update in real time via Binance Spot API with CoinGecko fallback.
  - 100% free, no API keys required, no rate limits.
- **Fully Responsive & Clean UI:**
  - Compact view on mobile screens.
  - Multi-column grid on desktop and tablet screens so all selected currencies fit without endless scrolling.
  - Light and Dark theme toggle.
  - English and Russian interface support.
- **Customizable Card Order:**
  - Drag and drop currency cards on desktop or long-press and drag on mobile touch devices.
- **Custom Exchange Rates:**
  - Override live market rates on any currency card with your own exchange kiosk or P2P rate, with instant bidirectional calculation and one-click reset.
- **Export to PNG:**
  - One-click snapshot generation to save or share your selected rates and converted amounts.
- **Persistent State:**
  - Selected currencies, custom order, theme, language, and amounts are saved locally in LocalStorage.

---

## Quick Start

The project is pure vanilla JavaScript, HTML5, and CSS3. No bundlers, Node.js builds, or dependencies are required.

To run locally, simply serve the directory with any static HTTP server or open `index.html` in your browser:

```bash
# Using Python:
python -m http.server 8000

# Using Node.js (npx):
npx serve .
```

Then open `http://localhost:8000` in your browser.

---

## Tech Stack

- HTML5 / CSS3 (CSS Grid, Flexbox, CSS Variables for theming)
- Vanilla JavaScript (ES6+, async/await, Fetch API)
- html2canvas for client-side PNG export
- Open ExchangeRate-API + Binance API + CoinGecko API

---

<details>
<summary><b>Описание на русском (Russian description)</b></summary>

<br>

# Currency Converter - Конвертер валют и криптовалют

Удобный, быстрый и адаптивный веб-конвертер мировых валют и криптовалют с актуальными курсами и экспортом в PNG.

**Демо онлайн:** [https://sashayio.github.io/currency-converter/](https://sashayio.github.io/currency-converter/)

### Возможности

- **Мировые валюты:** RUB, USD, EUR, GBP, CNY, JPY, CHF, TRY, валюты СНГ (BYN, KZT, UAH, UZS, GEL, AZN, AMD и др.) и другие популярные валюты.
- **Криптовалюты в реальном времени:** Bitcoin (BTC), Ethereum (ETH), Tether (USDT), Solana (SOL), TON, BNB, XRP, Dogecoin и др.
- **Актуальные котировки:**
  - Фиатные курсы обновляются каждый час напрямую через Open ExchangeRate-API.
  - Крипто-котировки обновляются в реальном времени через Binance Spot API (с резервным источником CoinGecko).
  - Бесплатно, без API-ключей и лимитов.
- **Интерфейс и адаптивность:**
  - Компактный вид на смартфонах.
  - Сетка карточек на ПК и планшетах без лишней прокрутки.
  - Светлая и темная тема.
  - Поддержка русского и английского языков.
- **Сортировка карточек:**
  - Перетаскивание карточек (drag-and-drop) мышью на ПК или долгим нажатием на сенсорных экранах.
- **Свой курс обмена:**
  - Возможность задать свой курс обменника или P2P для любой валюты с автоматическим пересчетом в обе стороны и быстрым сбросом.
- **Экспорт в PNG:**
  - Быстрое сохранение красивой карточки с выбранными курсами в виде картинки.
- **Сохранение настроек:**
  - Выбранные валюты, порядок, тема и сумма сохраняются в LocalStorage браузера.

### Запуск проекта

Проект не требует сборщиков и сторонних библиотек. Достаточно запустить локальный веб-сервер:

```bash
python -m http.server 8000
```
или открыть `index.html` напрямую в браузере.

</details>
