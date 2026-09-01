# KonvertToPOJ — Taiwanese Roman Orthography Converter

Kotlin Multiplatform library converting between **POJ** (Pe̍h-ōe-jī) and **KPL** (Kàu-io̍k Pō͘ Lô-má-jī / 教育部台灣閩南語羅馬字拼音方案), each in input-number and Unicode-diacritical modes.

## Quick Reference

```
./gradlew build                          # Build all targets
./gradlew :lib:jvmTest                   # Run JVM tests (unit + dictionary)
./gradlew :lib:allTests                  # Run all platform tests
```

Requires **JDK 17+**. iOS targets require macOS + Xcode.

## Architecture

### Conversion Pipeline

All 12 conversion directions follow a pipeline with optional normalization:

```
Source Format → Input-Number Form → [POJ Input "ou"→"oo" if from POJ_INPUT] → [Traditional Normalize if options.traditionalNasal] → [System Convert if crossing POJ↔KPL] → Target Format
```

Example: `POJ_UNICODE "Tâi-gí"` → `POJ_INPUT "tai5-gi2"` → `KPL_INPUT "tai5-gi2"` → `KPL_UNICODE "Tâi-gí"`

### Four Formats (`LomajiFormat` enum)

| Format | Example | Description |
|--------|---------|-------------|
| `POJ_INPUT` | `chit8-e5` | POJ with tone numbers (ASCII-safe) |
| `POJ_UNICODE` | `chi̍t-ê` | POJ with tone diacriticals |
| `KPL_INPUT` | `tsit8-e5` | KPL with tone numbers |
| `KPL_UNICODE` | `tsi̍t-ê` | KPL with tone diacriticals |

### Module Structure

```
lib/src/
├── commonMain/kotlin/tw/poj/kesi/konverttopoj/
│   ├── KonvertToPoj.kt              # Public API: convert(), convertHybrid(), isValidSyllable(), isValidText()
│   ├── LomajiFormat.kt          # Enum: POJ_INPUT, POJ_UNICODE, KPL_INPUT, KPL_UNICODE
│   ├── ConvertOptions.kt        # Options: traditionalNasal, haikau, aggressiveWhitespace
│   └── internal/
│       ├── ToneMap.kt           # Bidirectional maps: number↔unicode for each tone/vowel combo
│       ├── ToneMarker.kt        # Tone placement/removal algorithms (POJ rules ≠ KPL rules)
│       ├── SystemConverter.kt   # POJ↔KPL orthographic conversion (ch↔ts, oa↔ua, etc.)
│       ├── Tokenizer.kt         # Split text into lomaji/non-lomaji tokens
│       ├── SyllableValidator.kt # Whitelist-based syllable validation (haikau optional)
│       ├── TraditionalNormalizer.kt # Normalize traditional POJ nasal conventions
│       ├── HanLoSpacing.kt      # Han-Lo mixed text spacing/punctuation normalization
│       ├── NfcNormalize.kt      # expect/actual Unicode NFC normalization
│       └── StringUtils.kt       # Minor string helpers
├── commonTest/                  # 300+ unit tests (all passing)
├── jvmMain/                     # JVM NFC normalization
├── jvmTest/                     # Dictionary tests (82K + 868K entries)
├── jsMain/                      # JS export wrapper + NFC
├── appleMain/                   # iOS NFC normalization
└── wasmJsMain/                  # Wasm NFC normalization
```

### Default POJ Input Normalization

When converting FROM `POJ_INPUT`, "ou" is automatically normalized to "oo" (the standard POJ input representation of o͘). This is default behavior requiring no option:
- `gou2` → treated as `goo2` → `gó͘` (POJ Unicode)
- `gou2` → treated as `goo2` → `goo2` (KPL Input)
- Case-aware: `Gou` → `Goo`, `GOU` → `GOO`
- Applied in both `convert()` and `convertHybrid()` (before validation in hybrid)
- Does NOT affect `isValidSyllable()` or `isValidText()` — "ou" syllables are not valid, only "oo"

### KPL → POJ Pre-pass in `normalizePoj*`

All `normalizePoj*` methods (`normalizePoj`, `normalizePojHybrid`, and the HanLo
variants that delegate to them) run a strict KPL → POJ pre-pass before the POJ
unicode↔input round-trip. Implementation lives in private helper
`KonvertToPoj.convertKplSyllablesToPoj()`:

1. Tokenize the input via `Tokenizer.splitHybrid` + `Tokenizer.splitSyllables`.
2. For each lomaji syllable token, check `SyllableValidator.isValid(..., permissive = false)`:
   - If it strictly validates as **KPL** AND does NOT strictly validate as **POJ**, convert KPL → POJ.
   - Otherwise, leave the token unchanged.
3. Pass the cleaned text to the existing POJ unicode↔input round-trip.

The strict check (not permissive) is intentional: it only converts unambiguously
KPL syllables. Syllables valid in both systems (e.g. `kau`, `tai`) pass through;
syllables valid in neither (foreign words, abbreviations) pass through.

**Known behavior:** a "foreign" word whose spelling happens to be a strictly-valid
KPL syllable will be converted. For example, `normalizePoj("Tsai")` returns
`"Chai"` because `tsai` validates as KPL (initial `ts` + rhyme `ai`) but not as
POJ. Callers needing to preserve such tokens should use `normalizePojHybrid` and
ensure the foreign tokens appear in non-lomaji context, or pre-tag them.

### Strict POJ ↔ KPL Validator Separation

`SyllableValidator.isValid(..., permissive = false)` (the default for the public
`isValidSyllable` / `isValidText` API) enforces strict format separation:

- POJ initials (`ch`, `chh`) are rejected as KPL.
- KPL initials (`ts`, `tsh`) are rejected as POJ.
- POJ rhymes (`oa`, `oe`, `ek`, `eng`) are rejected as KPL.
- KPL rhymes (`ua`, `ue`, `ik`, `ing`) are rejected as POJ.

The permissive mode (used internally by `convertHybrid` for foreign-word
leniency) combines both initial/rhyme sets and is NOT exposed via the public
validation API.

### ConvertOptions

All public API methods accept an optional `ConvertOptions` parameter:

- **`traditionalNasal`** (default: `false`) — Accept traditional POJ nasal conventions:
  - `ⁿh` as `hⁿ` (nasal before checked coda → after)
  - `oⁿ` as `o͘ⁿ` (plain o → o͘ before nasal)
  - `oⁿh` as `o͘hⁿ` (both combined)
  - Normalization operates at the input-number level via `TraditionalNormalizer`:
    - `nnh` → `hnn` (swap nasal marker and checked coda)
    - `onn` → `oonn` (standalone o before nasal → o͘)
  - Validation adds standard-form rhymes (`ahnn`, `ihnn`, etc.) alongside traditional (`annh`, `innh`)

- **`haikau`** (default: `false`) — Include 海口腔 coastal dialect vowels:
  - POJ: `ur`, `or` and compounds | KPL: `ir`, `er` and compounds
  - Affects validation and `convertHybrid` (which uses validation to decide what to convert)
  - `convert()` handles these vowels regardless of this option

- **`aggressiveWhitespace`** (default: `true`) — Whitespace handling in `normalizePojHanLo*` methods:
  - `true` (aggressive): strip all horizontal whitespace and rebuild canonical spacing
  - `false` (conservative): preserve user whitespace except where Han-Lo rules forbid it
  - Has no effect outside the HanLo normalization methods

## Linguistic Domain Knowledge

### Reference Material

Linguistic rules are derived from **狗公會曉學台語** by Ngô͘ Hê-bí (https://oh.taigi.info/kauchai/), specifically:
- **Section 21**: POJ tone placement rules
- **Section 44**: POJ↔KPL orthographic differences
- **Section 45**: KPL tone placement rules
- **Pages 78, 82-83**: IPA table and valid syllable components (rhyme whitelist)

The POJ tone-placement rules and the syllable table are transcribed in full in
[POJ_KOAIM_KUICHEK.md](POJ_KOAIM_KUICHEK.md).

### Taiwanese Syllable Structure

Every syllable: `[initial consonant] + rhyme + [tone]`

**Tones**: 1, 2, 3, 4, 5, 7, 8, 9
- Tone 6 does not exist in standard Taiwanese
- Tones 4/8 are **checked tones** (束聲) — syllable must end in p, t, k, or h
- Non-checked syllables cannot have tones 4/8

### POJ Tone Placement Rules (Section 21)

Full canonical statement of the rules, with the syllable table, lives in
[POJ_KOAIM_KUICHEK.md](POJ_KOAIM_KUICHEK.md) (在 Tâi-gí). Summary below.

**Design principle (設-kè goân-chek):** put the tone mark as close to the middle of
the syllable as possible, and avoid marking "i" where possible. This keeps syllables
visually even in height, steadier and prettier.

**Letter counting.** "Letter" means a *Taigi* letter, not a Latin one:
- `ng` counts as **1** letter (but the mark is drawn on its leading Latin `n`).
- The nasal marker `ⁿ` (`nn` in input form) does **not** count at all — it is
  transparent to every rule below.
- `o͘` (`oo`), `ṳ` (`ur`) and `o̤` (`or`) each count as 1 letter / 1 marking unit.

1. **Single vowel** → mark it (`a, i, u, o͘, e, o`).
2. **Compound vowel** → mark the **2nd letter counting from the right** of the syllable.
   - **Exception 1**: if that 2nd-from-right letter is `i`, skip to the **1st**
     (rightmost) letter instead. — Lē: `iá, ió, iú`.
   - **Exception 2**: if the syllable is a **checked** compound vowel (coda `-p, -t, -k, -h`)
     and the 2nd-from-right letter is `i` or `u`, skip to the **3rd** letter.
     — Lē: `a̍ih, a̍uh, u̍ih, ia̍uh, oa̍ih`.
     - Only in `iuh` would the 3rd letter land on an `i`, so `iuh` does **not** skip:
       it keeps the 2nd-from-right letter. — Lē: `iu̍h` (and therefore `iu̍ⁿh`,
       since `ⁿ` does not count).
3. **No vowel** → mark the rightmost nasal coda (`m, n, ng`).
   - `ng` is 1 Taigi letter; draw the mark on its leading `n` → `ńg`, `n̍gh`.
4. `oo` / `ur` / `or` → mark as a 2-char unit (→ `o͘`, `ṳ`, `o̤`).

**Implementation note:** `ToneMarker.findPojTonePosition()` strips `ⁿ` before matching
rhyme patterns (`stripNasal`), so `iuⁿh` follows the same branch as `iuh`.

### KPL Tone Placement Rules (Section 45)

1. If `a` is present → mark the `a`
2. Else mark the rightmost vowel (treating "oo" as a unit, mark the left `o`)
3. Else mark the nasal (ng as unit, then m, then n)

### POJ ↔ KPL Orthographic Differences (Section 44)

| POJ | KPL | Category |
|-----|-----|----------|
| ch / chh | ts / tsh | Initials |
| oa / oe | ua / ue | Vowel combinations |
| ek / eng | ik / ing | Finals |
| o͘ (o + U+0358) | oo | Vowel representation |
| ⁿ (U+207F) | nn | Nasalization marker |
| ur / or | ir / er | 海口腔 (Hái-kháu-khiuⁿ) coastal vowels |
| ă (breve, U+0306) | a̋ (double acute, U+030B) | Tone 9 diacritical |

### Valid Initials

POJ: p, ph, m, b, t, th, n, l, k, kh, g, ng, h, s, j, ch, chh
KPL: p, ph, m, b, t, th, n, l, k, kh, g, ng, h, s, j, ts, tsh

### Special Unicode Characters

- **O͘** (o + combining dot above right U+0358) — POJ representation of /ɔ/, KPL uses `oo`
- **ⁿ** (U+207F superscript n) — POJ nasalization, KPL uses `nn`
- **Ṳ** (u + combining double macron below) — POJ coastal dialect vowel `ur`
- **O̤** (o + combining diaeresis below U+0324) — POJ coastal dialect vowel `or`
- **Tone 8 mark**: U+030D (combining vertical line above) — used in both POJ and KPL
- **Tone 9 mark**: U+0306 breve (POJ) vs U+030B double acute accent (KPL)

### NFC Normalization

All input text is NFC-normalized before processing. This is critical because combining diacriticals can be represented in multiple equivalent Unicode forms. Platform implementations:
- JVM: `java.text.Normalizer.normalize(str, Form.NFC)`
- JS: `str.normalize("NFC")`
- Apple: `NSString.precomposedStringWithCanonicalMapping`
- WasmJs: JS interop `str.normalize('NFC')`

## Conventions

- Zero runtime dependencies — pure Kotlin only
- `expect/actual` pattern for platform-specific code (only NFC normalization)
- Validation is whitelist-based (explicit lists of valid initials + rhymes), not regex
- Case handling: conversions preserve original case; all-uppercase syllables get special treatment
- Maven artifact: `tw.poj.kesi:konvert-to-poj:0.6.0` (Kotlin package remains `tw.poj.kesi.konverttopoj`)

## Test Data

- `test_data/TaijitToaSutian.csv` — 82K entries from 台日大辭典 for dictionary conversion testing
- `test_data/JOINED/` — 868K+ CSV entries from ChhoeTaigi dictionaries for real-world conversion testing
- `test_data/failures_*.txt` — generated failure reports from dictionary tests
- Dictionary tests are JVM-only (file I/O needed for CSV parsing)

### Valid Rhymes — Extended Coastal Dialect Forms

Beyond the standard rhymes from 狗公會曉學台語, these additional coastal dialect (海口腔) rhymes are validated:

| POJ | KPL | Source | Example |
|-----|-----|--------|---------|
| `uri` | `iri` | Coastal `ur` + `i` compound | nguri (眼) |
| `urt` | `irt` | Coastal `ur` + `t` checked | gurt (兀) |
| `um` | `um` | Nasal coda (same in both) | num (撼) |
