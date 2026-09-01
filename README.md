# KonvertToPOJ

Kotlin Multiplatform library for converting Taiwanese roman orthography between **POJ** (Pe̍h-ōe-jī) and **KPL** (Kàu-io̍k Pō͘ Lô-má-jī), in both input and Unicode modes.

```kotlin
KonvertToPoj.convert("goo2-kong7 e7-hiau2 oh8 tai5-gi2", POJ_INPUT, KPL_UNICODE)
// → "góo-kōng ē-hiáu o̍h tâi-gí"
```

## Platform Support

| Platform | Target |
| :------- | :----- |
| JVM / Android | `jvm` |
| iOS | `iosArm64`, `iosSimulatorArm64`, `iosX64` |
| JS (Browser / Node.js) | `js` |
| Wasm (Browser / Node.js) | `wasmJs` |

Zero runtime dependencies — pure Kotlin with platform-native Unicode normalization only.

## Installation

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("tw.poj.kesi:konvert-to-poj:0.6.0")
            }
        }
    }
}
```

### JVM / Android

```kotlin
dependencies {
    implementation("tw.poj.kesi:konvert-to-poj-jvm:0.6.0")
}
```

## Formats

Each roman orthography system has two modes:

- **Input** — tone numbers appended to syllables, ASCII-safe (e.g. `tai5-gi2`)
- **Unicode** — tone diacriticals on vowels (e.g. `Tâi-gí`)

| Format | Example | Description |
| :----- | :------ | :---------- |
| `POJ_INPUT` | `chit8-e5` | POJ with tone numbers |
| `POJ_UNICODE` | `chi̍t-ê` | POJ with tone diacriticals |
| `KPL_INPUT` | `tsit8-e5` | KPL with tone numbers |
| `KPL_UNICODE` | `tsi̍t-ê` | KPL with tone diacriticals |

## Usage

### Conversion

```kotlin
import tw.poj.kesi.konverttopoj.KonvertToPoj
import tw.poj.kesi.konverttopoj.LomajiFormat.*

// Input → Unicode
KonvertToPoj.convert("tai5-gi2", POJ_INPUT, POJ_UNICODE)   // → "Tâi-gí"

// Unicode → Input
KonvertToPoj.convert("Tâi-gí", POJ_UNICODE, POJ_INPUT)     // → "tai5-gi2"

// POJ → KPL
KonvertToPoj.convert("chit8-e5", POJ_INPUT, KPL_INPUT)     // → "tsit8-e5"

// Cross-system (POJ Input → KPL Unicode)
KonvertToPoj.convert("goo2-kong7 e7-hiau2 oh8 tai5-gi2", POJ_INPUT, KPL_UNICODE)
// → "góo-kōng ē-hiáu o̍h tâi-gí"
```

All 12 conversion directions (4 formats × 3 other formats) are supported.

### Options

`ConvertOptions` controls optional behaviors for conversion and validation:

```kotlin
import tw.poj.kesi.konverttopoj.ConvertOptions

val opts = ConvertOptions(
    traditionalNasal = true,        // Accept traditional POJ nasal conventions
    haikau = true,                  // Include 海口腔 coastal dialect vowels
    aggressiveWhitespace = true     // Han-Lo whitespace mode (only affects normalizePojHanLo*)
)

KonvertToPoj.convert("annh8", POJ_INPUT, POJ_UNICODE, opts)  // → "a̍hⁿ"
KonvertToPoj.isValidSyllable("ur2", POJ_INPUT, opts)          // → true
```

#### `traditionalNasal`

Accepts traditional POJ nasalization conventions where:
- `ⁿh` is equivalent to `hⁿ` (nasal marker before checked coda)
- `oⁿ` is equivalent to `o͘ⁿ` (plain o for o͘ before nasal)
- `oⁿh` is equivalent to `o͘hⁿ` (both combined)

When enabled, traditional input is normalized to standard form during conversion,
and both traditional and standard forms are accepted during validation.

#### `haikau`

Includes 海口腔 (Hái-kháu-khiuⁿ) coastal dialect vowels in validation:
- POJ: `ur`, `or` | KPL: `ir`, `er`

Conversion always handles these vowels regardless of this option; it only affects validation and `convertHybrid`.

#### `aggressiveWhitespace`

Controls whitespace handling in `normalizePojHanLoForceUsingFullwidthPunctuation`
and `normalizePojHanLoAutoChoanLoOrHanLoPunctuation`. Defaults to `true`.

- **`true` (aggressive)** — strip all horizontal whitespace and rebuild canonical
  spacing. Multiple spaces collapse to one, tabs and ideographic spaces (U+3000)
  disappear, leading/trailing whitespace on a line is removed. Newlines are
  always preserved.
- **`false` (conservative)** — preserve the user's whitespace as-is *except*
  where Han-Lo rules forbid it. Drops whitespace adjacent to Hanji and around
  punctuation (with the half-width clause-punctuation trailing-space exception);
  everywhere else — between lomaji words, between lomaji and digits/symbols,
  and at line edges — multi-space, tabs, and ideographic spaces (U+3000) are
  left untouched.

```kotlin
val text = "  góa   sī\tlâng ， 真 好 。"

KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(text)
// → "góa sī lâng，真好。"   // aggressive: canonical spacing

KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(
    text, ConvertOptions(aggressiveWhitespace = false)
)
// → "  góa   sī\tlâng，真好。"   // conservative: rule-required drops only
```

Has no effect on other methods.

### Hybrid text

Convert roman orthography embedded in CJK text, annotations, or other non-roman orthography content:

```kotlin
// Coastal-dialect "kor" requires the haikau option to be recognized as a valid syllable:
KonvertToPoj.convertHybrid(
    "koe2(漳)/kor2(泉)", POJ_INPUT, POJ_UNICODE,
    ConvertOptions(haikau = true)
)
// → "kóe(漳)/kó̤(泉)"

KonvertToPoj.convertHybrid("1. tai5-oan5 2. tiong1-kok4", POJ_INPUT, POJ_UNICODE)
// → "1. tâi-oân 2. tiong-kok"
```

### Validation

Validates syllables against linguistically correct Taiwanese phonotactics (whitelist-based, not regex):

```kotlin
KonvertToPoj.isValidSyllable("tai5", POJ_INPUT)    // true
KonvertToPoj.isValidSyllable("xyz", POJ_INPUT)      // false — not a valid syllable
KonvertToPoj.isValidSyllable("ka8", POJ_INPUT)      // false — tone 8 requires checked final
KonvertToPoj.isValidSyllable("kap8", POJ_INPUT)     // true

KonvertToPoj.isValidText("tai5-gi2", POJ_INPUT)     // true
KonvertToPoj.isValidText("Tâi-gí", POJ_UNICODE)     // true

// Strict separation: KPL-specific orthography is rejected as POJ (and vice versa).
KonvertToPoj.isValidSyllable("tsit8", POJ_INPUT)    // false — `ts` is a KPL initial
KonvertToPoj.isValidSyllable("tshiú", POJ_UNICODE)  // false — `tsh` is a KPL initial
KonvertToPoj.isValidSyllable("chit8", KPL_INPUT)    // false — `ch` is a POJ initial
```

### Normalization

Round-trip POJ Unicode text through input form to canonicalize tone-mark placement
and apply Unicode NFC normalization:

```kotlin
KonvertToPoj.normalizePoj("Ta̍i-gi̍")    // tone marks moved to the correct vowel per POJ rules
```

Note: input-form syllables embedded in the text (e.g. `goa2`) are also converted to
Unicode form, since the tokenizer cannot distinguish them from unmarked tone-1 lomaji.

#### KPL contamination is cleaned up

All `normalizePoj*` methods run a strict-KPL pre-pass before the POJ round-trip:
any syllable that **strictly** validates as KPL but **not** as POJ is converted to
POJ first. This guarantees POJ output never contains KPL-specific orthography
(`ts`, `tsh`, `ua`, `ue`, `ik`, `ing`):

```kotlin
KonvertToPoj.normalizePoj("ū tsi̍t jī")     // → "ū chi̍t jī"    (ts → ch)
KonvertToPoj.normalizePoj("tshiú")          // → "chhiú"         (tsh → chh)
KonvertToPoj.normalizePoj("kua")            // → "koa"           (ua → oa)
KonvertToPoj.normalizePoj("kíng")           // → "kéng"          (ing → eng)
```

The check is strict — syllables valid in both POJ and KPL pass through unchanged,
and syllables valid in neither (foreign words, abbreviations) pass through too.

**Caveat:** a "foreign" word whose spelling happens to be a strictly-valid KPL
syllable will be converted. For example, the surname `Tsai` looks like KPL
(initial `ts` + rhyme `ai`) and not POJ, so `normalizePoj("Tsai")` returns
`"Chai"`. If you need to preserve such tokens, use `normalizePojHybrid` and wrap
them in non-lomaji context, or pre-tag them before normalization.

For Han-Lo (漢羅) mixed text, two variants additionally fix spacing and punctuation
around CJK and roman orthography:

```kotlin
// Always emit full-width punctuation (Han-Lo standard):
KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation("我 是 lâng , Lí hó .")
// → "我是lâng，Lí hó。"

// Per-sentence auto:
//   sentence with Hanji   → full-width punctuation (Han-Lo style)
//   sentence pure lomaji  → half-width punctuation (Choân-lô / 全羅 style)
KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation("góa 是 lâng . Lí hó ?")
// → "góa是lâng。Lí hó?"

KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation("Tâi-gí chin hó ， án-ne")
// → "Tâi-gí chin hó, án-ne"   // pure lomaji → half-width comma + trailing space

KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation("góa 是 ; lí hó .")
// → "góa是；lí hó。"            // ; inherits the Han-Lo sentence's full-width
```

Both variants:
1. Round-trip POJ syllables through input form to canonicalize tone-mark placement.
2. Remove spaces between Hanji, between Hanji and roman orthography, and around punctuation.
3. Whitespace between roman orthography words is governed by `aggressiveWhitespace` (see above) —
   aggressive (default) collapses to exactly one space; conservative preserves the user's spacing.

The two variants differ only in punctuation width:

- **`...ForceUsingFullwidthPunctuation`** — converts half-width punctuation to full-width
  throughout (the standard Han-Lo convention).
- **`...AutoChoanLoOrHanLoPunctuation`** — splits text into sentences at `. ! ? 。 ！ ？`
  (other punctuation such as `, ; :` does *not* split sentences, so it inherits the
  surrounding sentence's mode) and chooses per-sentence: full-width when the sentence
  contains any Hanji, half-width (Choân-lô / 全羅 style) when the sentence is pure
  roman orthography. In half-width mode, clause punctuation (`, . ! ? ; :`) gets a trailing
  space when followed by a word-like token (lomaji or digit/symbol).

#### Validation-aware variants (`normalizePojHybrid*`)

Three additional variants run the round-trip through `convertHybrid` instead of
`convert`, so only tokens that match Taiwanese phonotactics are normalized;
foreign words (e.g. `Tennessee`, `Saxhorn`, `yama`) and abbreviations pass
through untouched. Useful when text mixes POJ with non-Taiwanese content.

```kotlin
// Plain validation-aware normalization:
KonvertToPoj.normalizePojHybrid("oē tô͘, sìan chhùi-phoé")
// → "ōe tô͘, siàn chhùi-phóe"

KonvertToPoj.normalizePojHybrid("Tennessee")  // → "Tennessee" (untouched)

// Han-Lo + validation-aware:
KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
    "歌名是「山人氣物者(yama no ninkhi mono)」"
)
// foreign tokens "yama", "no", "ninkhi", "mono" pass through; Han-Lo spacing applies
```

### Extension functions

```kotlin
import tw.poj.kesi.konverttopoj.convertLomaji
import tw.poj.kesi.konverttopoj.convertHybridLomaji
import tw.poj.kesi.konverttopoj.isValidLomajiSyllable
import tw.poj.kesi.konverttopoj.isValidLomaji
import tw.poj.kesi.konverttopoj.normalizePoj
import tw.poj.kesi.konverttopoj.normalizePojHybrid
import tw.poj.kesi.konverttopoj.normalizePojHanLoForceUsingFullwidthPunctuation
import tw.poj.kesi.konverttopoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation
import tw.poj.kesi.konverttopoj.normalizePojHybridHanLoForceUsingFullwidthPunctuation
import tw.poj.kesi.konverttopoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation

"tai5-gi2".convertLomaji(POJ_INPUT, POJ_UNICODE)     // "Tâi-gí"
"tai5-gi2".isValidLomaji(POJ_INPUT)                   // true
"Ta̍i-gi̍".normalizePoj()                              // canonicalized tone marks
"Tennessee".normalizePojHybrid()                      // foreign word passes through
```

### Swift (iOS)

Build the framework with `./gradlew linkReleaseFrameworkIosSimulatorArm64`, then add `KonvertToPOJ.framework` to your Xcode project.

```swift
import KonvertToPOJ

let result = KonvertToPoj.shared.convert(
    text: "tai5-gi2",
    from: .pojInput,
    to: .pojUnicode
)
// result: "Tâi-gí"

let valid = KonvertToPoj.shared.isValidSyllable(syllable: "tai5", format: .pojInput)
// valid: true
```

### JavaScript

The JS target exports a `KonvertToPoj` object with string-based format parameters:

```javascript
import { KonvertToPoj } from 'konverttopoj';

const result = KonvertToPoj.convert("tai5-gi2", "POJ_INPUT", "POJ_UNICODE");
// → "Tâi-gí"

const valid = KonvertToPoj.isValidSyllable("tai5", "POJ_INPUT");
// → true

// With options (traditionalNasal, haikau):
const trad = KonvertToPoj.convert("annh8", "POJ_INPUT", "POJ_UNICODE", true, false);
const coastal = KonvertToPoj.isValidSyllable("ur2", "POJ_INPUT", false, true);

// Normalization:
KonvertToPoj.normalizePoj("Ta̍i-gi̍");
KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation("我  beh  去 chhōe   i .");
KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation("góa 是 lâng . Lí hó ?");

// Validation-aware variants (foreign words pass through):
KonvertToPoj.normalizePojHybrid("Tennessee");                                            // "Tennessee"
KonvertToPoj.normalizePojHybridHanLoForceUsingFullwidthPunctuation("我oē講台語");
KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
    "歌名是「山人氣物者(yama no ninkhi mono)」"
);

// 4th positional arg controls aggressive vs. conservative whitespace:
KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(
    "  góa   sī\tlâng ， 真 好 。", false, false, /* aggressiveWhitespace= */ false
);

// Format constants are available on the object:
// KonvertToPoj.POJ_INPUT, KonvertToPoj.POJ_UNICODE, KonvertToPoj.KPL_INPUT, KonvertToPoj.KPL_UNICODE
```

## API Reference

### `KonvertToPoj` object

| Method | Description |
| :----- | :---------- |
| `convert(text, from, to, options?)` | Convert pure roman orthography text between any two formats |
| `convertHybrid(text, from, to, options?)` | Convert roman orthography mixed with CJK or other content |
| `isValidSyllable(syllable, format, options?)` | Validate a single syllable against Taiwanese phonotactics |
| `isValidText(text, format, options?)` | Validate multi-syllable text (hyphen/space/punctuation separated) |
| `normalizePoj(text, options?)` | Canonicalize POJ tone-mark placement via input-form round-trip + NFC |
| `normalizePojHybrid(text, options?)` | Validation-aware variant of `normalizePoj`: only valid Taiwanese syllables are normalized; foreign words pass through |
| `normalizePojHanLoForceUsingFullwidthPunctuation(text, options?)` | Normalize Han-Lo: canonicalize POJ + fix spacing, force full-width punctuation |
| `normalizePojHanLoAutoChoanLoOrHanLoPunctuation(text, options?)` | Normalize Han-Lo: per-sentence punctuation width (Han-Lo → full-width, pure lomaji → half-width) |
| `normalizePojHybridHanLoForceUsingFullwidthPunctuation(text, options?)` | Validation-aware variant: foreign words pass through, then Han-Lo + full-width punctuation |
| `normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(text, options?)` | Validation-aware variant: foreign words pass through, then Han-Lo + per-sentence punctuation width |

### `String` extensions

| Function | Equivalent |
| :------- | :--------- |
| `String.convertLomaji(from, to, options?)` | `KonvertToPoj.convert(this, from, to, options)` |
| `String.convertHybridLomaji(from, to, options?)` | `KonvertToPoj.convertHybrid(this, from, to, options)` |
| `String.isValidLomajiSyllable(format, options?)` | `KonvertToPoj.isValidSyllable(this, format, options)` |
| `String.isValidLomaji(format, options?)` | `KonvertToPoj.isValidText(this, format, options)` |
| `String.normalizePoj(options?)` | `KonvertToPoj.normalizePoj(this, options)` |
| `String.normalizePojHybrid(options?)` | `KonvertToPoj.normalizePojHybrid(this, options)` |
| `String.normalizePojHanLoForceUsingFullwidthPunctuation(options?)` | `KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(this, options)` |
| `String.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(options?)` | `KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(this, options)` |
| `String.normalizePojHybridHanLoForceUsingFullwidthPunctuation(options?)` | `KonvertToPoj.normalizePojHybridHanLoForceUsingFullwidthPunctuation(this, options)` |
| `String.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(options?)` | `KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(this, options)` |

## POJ ↔ KPL Differences Handled

| POJ | KPL | Category |
| :-- | :-- | :------- |
| ch / chh | ts / tsh | Initials |
| oa / oe | ua / ue | Vowel combinations |
| ek / eng | ik / ing | Finals |
| o͘ (o + U+0358) | oo | Vowel |
| ⁿ (U+207F) | nn | Nasalization |
| ur / or | ir / er | Haikhau dialect vowels (optional) |
| ă (breve) | a̋ (double acute) | Tone 9 diacritical |

## Tone Placement Rules

The POJ tone-placement rules this library implements — the design principle, the
letter-counting conventions (`ng` = 1 letter, `ⁿ` counts for nothing), the two
exceptions for compound vowels, and the full syllable table — are written out in
**[POJ_KOAIM_KUICHEK.md](POJ_KOAIM_KUICHEK.md)** (in Taigi).

Source: [狗公會曉學台語](https://oh.taigi.info/kauchai/) by Ngô͘ Hê-bí, Section 21.

## Build

Requires JDK 17+. iOS targets require macOS with Xcode.

```bash
./gradlew build
```

## License

GPLv3
