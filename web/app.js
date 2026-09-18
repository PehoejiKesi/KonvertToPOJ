// KonvertToPOJ web converter — all logic runs in the browser.
import { KonvertToPoj } from './lib/KonvertToPOJ-lib.mjs';

const K = KonvertToPoj.getInstance();

const FORMATS = [
  { id: 'POJ_INPUT',   label: 'POJ · input numbers',     sample: 'chit8-e5' },
  { id: 'POJ_UNICODE', label: 'POJ · Unicode diacritics', sample: 'chi̍t-ê' },
  { id: 'KPL_INPUT',   label: 'KPL · input numbers',     sample: 'tsit8-e5' },
  { id: 'KPL_UNICODE', label: 'KPL · Unicode diacritics', sample: 'tsi̍t-ê' },
];

const BLURBS = {
  convert: 'Convert between the four roman orthography formats. Twelve directions in all — POJ ↔ KPL and tone numbers ↔ Unicode diacriticals, in any combination.',
  normalize: 'Canonicalise POJ: tone marks are moved to the position the POJ placement rules require, and any unambiguously KPL syllables (tsi̍t, tshiu, kua, kíng) are rewritten as POJ (chi̍t, chhiu, koa, kéng). That KPL pre-pass runs in both text modes — Hybrid only stops non-syllables from having their tone marks rearranged.',
  hanlo: 'Normalise mixed Hanji + lô-má-jī text: canonicalise POJ tone marks, fix spacing between Hanji and lô-má-jī, and set punctuation width.',
  validate: 'Check every syllable against the Tâi-gí syllable whitelist — initials, rhymes and tone/coda agreement. Each token is reported separately.',
};

const HYBRID_HINTS = {
  pure:   'Every lô-má-jī token is converted. Use for text that is entirely roman orthography.',
  hybrid: 'Only tokens that validate as real Tâi-gí syllables are converted; Hanji, foreign words and abbreviations pass through untouched.',
};

const PUNCT_HINTS = {
  fullwidth: 'Every sentence gets full-width punctuation — the standard Han-Lo (漢羅) convention.',
  auto:      'Per sentence: full-width if it contains Hanji, half-width if it is pure lô-má-jī (Choân-lô / 全羅 style).',
};

const EXAMPLES = {
  convert: [
    { label: 'goo2-kong7 e7-hiau2 oh8 tai5-gi2', text: 'goo2-kong7 e7-hiau2 oh8 tai5-gi2', from: 'POJ_INPUT', to: 'KPL_UNICODE' },
    { label: 'Tâi-oân-ōe', text: 'Tâi-oân-ōe', from: 'POJ_UNICODE', to: 'KPL_UNICODE' },
    { label: 'tsi̍t-ê lâng', text: 'tsi̍t-ê lâng', from: 'KPL_UNICODE', to: 'POJ_UNICODE' },
    { label: 'gou2 (ou → oo)', text: 'gou2-lang5', from: 'POJ_INPUT', to: 'POJ_UNICODE' },
    { label: 'chhut-hoat', text: 'chhut-hoat', from: 'POJ_UNICODE', to: 'KPL_INPUT' },
    { label: 'foreign words — try Hybrid', text: 'Góa khì Pokémon Center', from: 'POJ_UNICODE', to: 'KPL_UNICODE' },
  ],
  normalize: [
    { label: 'misplaced tone mark', text: 'Tái-gi̍' },
    { label: 'KPL leaking into POJ', text: 'tsi̍t-ê tshiú kíng-tshat' },
    { label: 'input form mixed in', text: 'goa2 beh khi3 Tâi-pak' },
    { label: 'foreign names — compare modes', text: 'Pokémon kap Ōsaka chin sui' },
  ],
  hanlo: [
    { label: '漢羅 with loose spacing', text: '我 beh 去 Tâi-pak , 你 kám 欲 做伙 ?' },
    { label: 'mixed sentences', text: 'Góa chin hoaⁿ-hí. 今仔日 天氣 真 好!' },
    { label: 'double spaces', text: '阿母  講 :  「 你  tio̍h  食  飯 」' },
  ],
  validate: [
    { label: 'valid POJ', text: 'chhut-hoat kong-hoe' },
    { label: 'KPL spellings', text: 'tshut-huat kong-hue' },
    { label: 'bad tone/coda', text: 'tai4 goa8 bok9' },
    { label: 'mixed good & bad', text: 'chi̍t ê zzz lâng' },
  ],
};

const state = {
  mode: 'convert',
  from: 'POJ_INPUT',
  to: 'KPL_UNICODE',
  validateFormat: 'POJ_UNICODE',
  hybrid: false,
  punct: 'fullwidth',
  traditionalNasal: false,
  haikau: false,
  aggressiveWhitespace: true,
  text: '',
};

const $ = (sel) => document.querySelector(sel);
const el = {
  blurb: $('#mode-blurb'),
  from: $('#from-format'),
  to: $('#to-format'),
  validateFormat: $('#validate-format'),
  segHybrid: $('#seg-hybrid'),
  segPunct: $('#seg-punct'),
  hybridHint: $('#hybrid-hint'),
  punctHint: $('#punct-hint'),
  traditional: $('#opt-traditional'),
  haikau: $('#opt-haikau'),
  aggressive: $('#opt-aggressive'),
  aggressiveWrap: $('#opt-ws-wrap'),
  input: $('#input'),
  output: $('#output'),
  report: $('#validate-report'),
  inCount: $('#in-count'),
  outCount: $('#out-count'),
  chips: $('#chips'),
  api: $('#api-snippet'),
  toast: $('#toast'),
};

/* ---------------- setup ---------------- */

for (const sel of [el.from, el.to, el.validateFormat]) {
  for (const f of FORMATS) {
    const opt = document.createElement('option');
    opt.value = f.id;
    opt.textContent = `${f.label}  —  ${f.sample}`;
    sel.append(opt);
  }
}

el.output.dataset.placeholder = 'Output appears here as you type.';

/* ---------------- theme ---------------- */

const savedTheme = safeGet('konverttopoj:theme');
if (savedTheme) document.documentElement.dataset.theme = savedTheme;

$('#theme-toggle').addEventListener('click', () => {
  const isDark = matchMedia('(prefers-color-scheme: dark)').matches;
  const current = document.documentElement.dataset.theme || (isDark ? 'dark' : 'light');
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.dataset.theme = next;
  safeSet('konverttopoj:theme', next);
});

/* ---------------- conversion ---------------- */

function run() {
  const text = state.text;
  if (!text) {
    el.output.textContent = '';
    el.report.hidden = true;
    updateCounts('');
    renderApi();
    return;
  }

  try {
    if (state.mode === 'validate') {
      renderValidation(text);
      renderApi();
      return;
    }
    el.report.hidden = true;
    const result = compute(text);
    el.output.textContent = result;
    el.output.classList.remove('is-error');
    updateCounts(result);
  } catch (err) {
    el.output.textContent = `⚠ ${err && err.message ? err.message : err}`;
    updateCounts('');
  }
  renderApi();
}

function compute(text) {
  const { traditionalNasal: tn, haikau: hk, aggressiveWhitespace: ws, hybrid } = state;
  switch (state.mode) {
    case 'convert':
      return hybrid
        ? K.convertHybrid(text, state.from, state.to, tn, hk)
        : K.convert(text, state.from, state.to, tn, hk);
    case 'normalize':
      return hybrid ? K.normalizePojHybrid(text, tn, hk) : K.normalizePoj(text, tn, hk);
    case 'hanlo': {
      const full = state.punct === 'fullwidth';
      if (hybrid) {
        return full
          ? K.normalizePojHybridHanLoForceUsingFullwidthPunctuation(text, tn, hk, ws)
          : K.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(text, tn, hk, ws);
      }
      return full
        ? K.normalizePojHanLoForceUsingFullwidthPunctuation(text, tn, hk, ws)
        : K.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(text, tn, hk, ws);
    }
    default:
      return text;
  }
}

/* ---------------- validation view ---------------- */

// A lo-ma-ji token: any Latin-script letter (precomposed letters included), any
// non-spacing combining mark (tone marks, the o-dot and o-diaeresis), and tone digits.
const TOKEN_RE = /[\p{Script=Latin}\p{Mn}0-9]+/gu;
const HAS_LETTER = /\p{Script=Latin}/u;

function renderValidation(text) {
  const fmt = state.validateFormat;
  const { traditionalNasal: tn, haikau: hk } = state;

  const overall = K.isValidText(text, fmt, tn, hk);

  const tokens = [];
  for (const m of text.normalize('NFC').matchAll(TOKEN_RE)) {
    // Hyphens and spaces are not part of the class, so each match is one syllable.
    if (HAS_LETTER.test(m[0])) tokens.push(m[0]);
  }

  el.output.textContent = overall
    ? 'Every lô-má-jī token is a valid ' + fmt.replace('_', ' ') + ' syllable.'
    : 'At least one token is not a valid ' + fmt.replace('_', ' ') + ' syllable — see below.';
  updateCounts(el.output.textContent);

  const verdict = document.createElement('p');
  verdict.className = 'verdict ' + (overall ? 'ok' : 'bad');
  verdict.textContent = overall ? '✓ isValidText → true' : '✕ isValidText → false';

  const list = document.createElement('div');
  list.className = 'syls';
  for (const syl of tokens) {
    const ok = K.isValidSyllable(syl, fmt, tn, hk);
    const span = document.createElement('span');
    span.className = 'syl ' + (ok ? 'ok' : 'bad');
    span.textContent = syl;
    const mark = document.createElement('span');
    mark.className = 'mark';
    mark.textContent = ok ? '✓' : '✕';
    span.append(mark);
    list.append(span);
  }

  el.report.replaceChildren(verdict, list);
  if (!tokens.length) {
    const none = document.createElement('p');
    none.className = 'hint';
    none.textContent = 'No lô-má-jī tokens found in the input.';
    el.report.append(none);
  }
  el.report.hidden = false;
}

/* ---------------- Kotlin snippet ---------------- */

function renderApi() {
  const optParts = [];
  if (state.traditionalNasal) optParts.push('traditionalNasal = true');
  if (state.haikau) optParts.push('haikau = true');
  if (state.mode === 'hanlo' && !state.aggressiveWhitespace) optParts.push('aggressiveWhitespace = false');
  const opts = optParts.length ? `, ConvertOptions(${optParts.join(', ')})` : '';
  const arg = 'text';
  let call;

  switch (state.mode) {
    case 'convert':
      call = `KonvertToPoj.${state.hybrid ? 'convertHybrid' : 'convert'}(${arg}, ${state.from}, ${state.to}${opts})`;
      break;
    case 'normalize':
      call = `KonvertToPoj.${state.hybrid ? 'normalizePojHybrid' : 'normalizePoj'}(${arg}${opts})`;
      break;
    case 'hanlo': {
      const name = 'normalizePoj' + (state.hybrid ? 'Hybrid' : '') +
        (state.punct === 'fullwidth'
          ? 'HanLoForceUsingFullwidthPunctuation'
          : 'HanLoAutoChoanLoOrHanLoPunctuation');
      call = `KonvertToPoj.${name}(${arg}${opts})`;
      break;
    }
    case 'validate':
      call = `KonvertToPoj.isValidText(${arg}, ${state.validateFormat}${opts})\n` +
             `KonvertToPoj.isValidSyllable(syllable, ${state.validateFormat}${opts})`;
      break;
  }
  el.api.textContent = call;
}

/* ---------------- UI wiring ---------------- */

function setMode(mode) {
  state.mode = mode;
  for (const tab of document.querySelectorAll('.tab')) {
    const on = tab.dataset.mode === mode;
    tab.classList.toggle('is-active', on);
    tab.setAttribute('aria-selected', String(on));
  }
  for (const row of document.querySelectorAll('.ctl-row[data-for]')) {
    row.hidden = !row.dataset.for.split(' ').includes(mode);
  }
  el.aggressiveWrap.hidden = mode !== 'hanlo';
  el.blurb.textContent = BLURBS[mode];
  el.output.dataset.placeholder = mode === 'validate'
    ? 'Validation verdict appears here.'
    : 'Output appears here as you type.';
  renderChips();
  run();
  syncHash();
}

function renderChips() {
  el.chips.replaceChildren();
  for (const ex of EXAMPLES[state.mode]) {
    const b = document.createElement('button');
    b.type = 'button';
    b.className = 'chip';
    b.textContent = ex.label;
    b.addEventListener('click', () => {
      if (ex.from) { state.from = ex.from; el.from.value = ex.from; }
      if (ex.to)   { state.to = ex.to;     el.to.value = ex.to; }
      el.input.value = ex.text;
      state.text = ex.text;
      updateInCount();
      run();
      syncHash();
    });
    el.chips.append(b);
  }
}

function setSegment(group, value) {
  for (const b of group.querySelectorAll('button')) {
    const on = b.dataset.value === value;
    b.classList.toggle('is-active', on);
    b.setAttribute('aria-checked', String(on));
  }
}

document.querySelectorAll('.tab').forEach((tab) => {
  tab.addEventListener('click', () => setMode(tab.dataset.mode));
});

el.segHybrid.addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;
  state.hybrid = btn.dataset.value === 'hybrid';
  setSegment(el.segHybrid, btn.dataset.value);
  el.hybridHint.textContent = HYBRID_HINTS[btn.dataset.value];
  run();
  syncHash();
});

el.segPunct.addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;
  state.punct = btn.dataset.value;
  setSegment(el.segPunct, state.punct);
  el.punctHint.textContent = PUNCT_HINTS[state.punct];
  run();
  syncHash();
});

el.from.addEventListener('change', () => { state.from = el.from.value; run(); syncHash(); });
el.to.addEventListener('change',   () => { state.to = el.to.value;     run(); syncHash(); });
el.validateFormat.addEventListener('change', () => { state.validateFormat = el.validateFormat.value; run(); syncHash(); });

el.traditional.addEventListener('change', () => { state.traditionalNasal = el.traditional.checked; run(); syncHash(); });
el.haikau.addEventListener('change',      () => { state.haikau = el.haikau.checked; run(); syncHash(); });
el.aggressive.addEventListener('change',  () => { state.aggressiveWhitespace = el.aggressive.checked; run(); syncHash(); });

$('#swap').addEventListener('click', () => {
  [state.from, state.to] = [state.to, state.from];
  el.from.value = state.from;
  el.to.value = state.to;
  run();
  syncHash();
});

el.input.addEventListener('input', () => {
  state.text = el.input.value;
  updateInCount();
  run();
  syncHash();
});

$('#clear').addEventListener('click', () => {
  el.input.value = '';
  state.text = '';
  updateInCount();
  run();
  syncHash();
  el.input.focus();
});

$('#reuse').addEventListener('click', () => {
  const out = el.output.textContent;
  if (!out) return;
  el.input.value = out;
  state.text = out;
  // After converting, the output is now in the target format.
  if (state.mode === 'convert') {
    state.from = state.to;
    el.from.value = state.from;
  }
  updateInCount();
  run();
  syncHash();
  toast('Output moved to input');
});

$('#copy').addEventListener('click', () => copy(el.output.textContent, 'Output copied'));
$('#copy-api').addEventListener('click', () => copy(el.api.textContent, 'Snippet copied'));

/* ---------------- helpers ---------------- */

function updateInCount() {
  el.inCount.textContent = `${[...state.text].length} chars`;
}

function updateCounts(out) {
  updateInCount();
  el.outCount.textContent = `${[...out].length} chars`;
}

async function copy(text, msg) {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    toast(msg);
  } catch {
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.append(ta);
    ta.select();
    try { document.execCommand('copy'); toast(msg); } catch { toast('Copy failed'); }
    ta.remove();
  }
}

let toastTimer;
function toast(msg) {
  el.toast.textContent = msg;
  el.toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.toast.classList.remove('show'), 1600);
}

function safeGet(key) { try { return localStorage.getItem(key); } catch { return null; } }
function safeSet(key, val) { try { localStorage.setItem(key, val); } catch { /* ignore */ } }

/* ---------------- shareable URL ---------------- */

let hashTimer;
function syncHash() {
  clearTimeout(hashTimer);
  hashTimer = setTimeout(() => {
    const p = new URLSearchParams();
    p.set('m', state.mode);
    if (state.mode === 'convert') { p.set('f', state.from); p.set('t', state.to); }
    if (state.mode === 'validate') p.set('v', state.validateFormat);
    if (state.hybrid) p.set('h', '1');
    if (state.mode === 'hanlo' && state.punct !== 'fullwidth') p.set('p', state.punct);
    if (state.traditionalNasal) p.set('tn', '1');
    if (state.haikau) p.set('hk', '1');
    if (!state.aggressiveWhitespace) p.set('aw', '0');
    if (state.text) p.set('q', state.text);
    history.replaceState(null, '', '#' + p.toString());
  }, 250);
}

function readHash() {
  const raw = location.hash.replace(/^#/, '');
  if (!raw) return;
  const p = new URLSearchParams(raw);
  const mode = p.get('m');
  if (mode && BLURBS[mode]) state.mode = mode;
  const ids = FORMATS.map((f) => f.id);
  if (ids.includes(p.get('f'))) state.from = p.get('f');
  if (ids.includes(p.get('t'))) state.to = p.get('t');
  if (ids.includes(p.get('v'))) state.validateFormat = p.get('v');
  state.hybrid = p.get('h') === '1';
  if (p.get('p') === 'auto') state.punct = 'auto';
  state.traditionalNasal = p.get('tn') === '1';
  state.haikau = p.get('hk') === '1';
  state.aggressiveWhitespace = p.get('aw') !== '0';
  state.text = p.get('q') || '';
}

/* ---------------- boot ---------------- */

readHash();

el.from.value = state.from;
el.to.value = state.to;
el.validateFormat.value = state.validateFormat;
el.traditional.checked = state.traditionalNasal;
el.haikau.checked = state.haikau;
el.aggressive.checked = state.aggressiveWhitespace;
setSegment(el.segHybrid, state.hybrid ? 'hybrid' : 'pure');
setSegment(el.segPunct, state.punct);
el.hybridHint.textContent = HYBRID_HINTS[state.hybrid ? 'hybrid' : 'pure'];
el.punctHint.textContent = PUNCT_HINTS[state.punct];

if (!state.text) state.text = 'goo2-kong7 e7-hiau2 oh8 tai5-gi2';
el.input.value = state.text;

setMode(state.mode);
