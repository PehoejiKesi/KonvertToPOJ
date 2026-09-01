package tw.poj.kesi.konverttopoj

import tw.poj.kesi.konverttopoj.internal.*

object KonvertToPoj {

    /**
     * Convert pure roman orthography text between any two formats.
     * Syllables should be separated by hyphens, spaces, or punctuation.
     */
    fun convert(text: String, from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String {
        if (from == to && !options.traditionalNasal && from != LomajiFormat.POJ_INPUT) return text
        val normalized = normalizeConfusables(normalizeNfc(text))
        val tokens = Tokenizer.splitSyllables(normalized)
        return tokens.joinToString("") { token ->
            if (token.isLomaji) convertSyllable(token.text, from, to, options) else token.text
        }
    }

    /**
     * Convert hybrid text (roman orthography mixed with CJK, numbers, etc.) between any two formats.
     */
    fun convertHybrid(text: String, from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String {
        if (from == to && !options.traditionalNasal && from != LomajiFormat.POJ_INPUT) return text
        val normalized = normalizeConfusables(normalizeNfc(text))
        val segments = Tokenizer.splitHybrid(normalized)
        return segments.joinToString("") { segment ->
            if (segment.isLomaji) {
                // Further split lomaji segment into syllables
                val syllableTokens = Tokenizer.splitSyllables(segment.text)
                syllableTokens.joinToString("") { token ->
                    val tokenText = if (from == LomajiFormat.POJ_INPUT) normalizePojInputOu(token.text) else token.text
                    if (token.isLomaji && SyllableValidator.isValid(tokenText, from, strictTones = false, permissive = true, options = options)) {
                        convertSyllable(tokenText, from, to, options)
                    } else {
                        token.text
                    }
                }
            } else {
                segment.text
            }
        }
    }

    /**
     * Validate a single syllable against Taiwanese roman orthography rules.
     * Returns true if the syllable is a linguistically valid Taiwanese syllable.
     */
    fun isValidSyllable(syllable: String, format: LomajiFormat, options: ConvertOptions = ConvertOptions()): Boolean =
        SyllableValidator.isValid(normalizeConfusables(normalizeNfc(syllable)), format, options = options)

    /**
     * Validate multi-syllable text (separated by hyphens, spaces, punctuation).
     * Returns true if every syllable token is valid.
     */
    fun isValidText(text: String, format: LomajiFormat, options: ConvertOptions = ConvertOptions()): Boolean {
        val normalized = normalizeConfusables(normalizeNfc(text))
        val tokens = Tokenizer.splitSyllables(normalized)
        return tokens.all { token ->
            !token.isLomaji || SyllableValidator.isValid(token.text, format, options = options)
        }
    }

    /**
     * Normalize POJ Unicode text by round-tripping through input form: unicode → input → unicode.
     * Canonicalizes tone-mark placement (e.g. moves a misplaced tone mark to the position
     * dictated by POJ tone placement rules) and applies Unicode NFC normalization to the result.
     *
     * **KPL pre-pass:** before the round-trip, any syllables that strictly validate as KPL
     * (e.g. `tsi̍t`, `tshiu`, `kua`, `kíng`) — and do *not* strictly validate as POJ — are
     * converted to POJ (`chi̍t`, `chhiu`, `koa`, `kéng`). This guarantees POJ output never
     * contains KPL-specific orthography. The pre-pass is validation-gated (strict, not
     * permissive) and runs even on the non-hybrid `normalizePoj` — meaning a token like
     * `Tsai` will be converted to `Chai` since `tsai` is strictly-valid KPL but not POJ.
     *
     * Note: input-form syllables (e.g. `goa2`) embedded in the text are also converted to
     * Unicode form, since the tokenizer cannot distinguish them from unmarked tone-1 lomaji.
     */
    fun normalizePoj(text: String, options: ConvertOptions = ConvertOptions()): String {
        val pojified = convertKplSyllablesToPoj(text, LomajiFormat.POJ_UNICODE, options)
        val asInput = convert(pojified, LomajiFormat.POJ_UNICODE, LomajiFormat.POJ_INPUT, options)
        val asUnicode = convert(asInput, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE, options)
        return normalizeNfc(asUnicode)
    }

    /**
     * Normalize POJ Unicode text using validation-aware conversion: only valid Taiwanese
     * syllables are round-tripped through input form. Non-POJ words (foreign names,
     * abbreviations, etc.) pass through untouched.
     *
     * Any syllables that strictly validate as KPL (e.g. `tsi̍t`, `tshiu`) are first converted to
     * POJ (`chi̍t`, `chhiu`), ensuring POJ output never contains KPL-specific orthography.
     */
    fun normalizePojHybrid(text: String, options: ConvertOptions = ConvertOptions()): String {
        val pojified = convertKplSyllablesToPoj(text, LomajiFormat.POJ_UNICODE, options)
        val asInput = convertHybrid(pojified, LomajiFormat.POJ_UNICODE, LomajiFormat.POJ_INPUT, options)
        val asUnicode = convertHybrid(asInput, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE, options)
        return normalizeNfc(asUnicode)
    }

    /**
     * Normalize Han-Lo (漢羅) mixed text, forcing full-width punctuation throughout
     * (the standard Han-Lo convention):
     * 1. Round-trip POJ syllables through input form to canonicalize tone-mark placement.
     * 2. Remove spaces between Hanji, between Hanji and roman orthography, and around punctuation.
     * 3. Whitespace between roman orthography words: see [ConvertOptions.aggressiveWhitespace] —
     *    aggressive (default) collapses to exactly one space; conservative preserves the
     *    user's whitespace as-is.
     * 4. Convert common half-width punctuation to full-width equivalents.
     */
    fun normalizePojHanLoForceUsingFullwidthPunctuation(
        text: String,
        options: ConvertOptions = ConvertOptions()
    ): String {
        if (text.isBlank()) return text
        val normalized = normalizePoj(text, options)
        return HanLoSpacing.fix(normalized, HanLoPunctuationMode.FORCE_FULLWIDTH, options.aggressiveWhitespace)
    }

    /**
     * Normalize Han-Lo mixed text, choosing punctuation width per sentence:
     * 1. Round-trip POJ syllables through input form to canonicalize tone-mark placement.
     * 2. Remove spaces between Hanji, between Hanji and roman orthography, and around punctuation.
     * 3. Whitespace between roman orthography words: see [ConvertOptions.aggressiveWhitespace] —
     *    aggressive (default) collapses to exactly one space; conservative preserves the
     *    user's whitespace as-is.
     * 4. Per sentence: full-width punctuation if the sentence contains any Hanji
     *    (Han-Lo style); half-width if the sentence is pure roman orthography
     *    (Choân-lô / 全羅 style).
     *
     * Sentences are split at sentence-terminating punctuation (`. ! ? 。 ！ ？`).
     * Other punctuation (`, ; :` etc.) does not split sentences.
     */
    fun normalizePojHanLoAutoChoanLoOrHanLoPunctuation(
        text: String,
        options: ConvertOptions = ConvertOptions()
    ): String {
        if (text.isBlank()) return text
        val normalized = normalizePoj(text, options)
        return HanLoSpacing.fix(normalized, HanLoPunctuationMode.AUTO_PER_SENTENCE, options.aggressiveWhitespace)
    }

    /**
     * Validation-aware variant of [normalizePojHanLoForceUsingFullwidthPunctuation].
     * Only valid Taiwanese syllables are normalized; foreign words pass through untouched.
     */
    fun normalizePojHybridHanLoForceUsingFullwidthPunctuation(
        text: String,
        options: ConvertOptions = ConvertOptions()
    ): String {
        if (text.isBlank()) return text
        val normalized = normalizePojHybrid(text, options)
        return HanLoSpacing.fix(normalized, HanLoPunctuationMode.FORCE_FULLWIDTH, options.aggressiveWhitespace)
    }

    /**
     * Validation-aware variant of [normalizePojHanLoAutoChoanLoOrHanLoPunctuation].
     * Only valid Taiwanese syllables are normalized; foreign words pass through untouched.
     */
    fun normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
        text: String,
        options: ConvertOptions = ConvertOptions()
    ): String {
        if (text.isBlank()) return text
        val normalized = normalizePojHybrid(text, options)
        return HanLoSpacing.fix(normalized, HanLoPunctuationMode.AUTO_PER_SENTENCE, options.aggressiveWhitespace)
    }

    /**
     * Pre-pass for POJ normalization: convert any syllables that strictly validate as KPL
     * (but not as POJ) into POJ. This catches KPL contamination like `tsi̍t` → `chi̍t`,
     * `tshiu` → `chhiu`, `kua` → `koa`, `ing` → `eng` in text intended to be POJ.
     *
     * Uses strict (non-permissive) KPL validation so only unambiguously KPL syllables are
     * touched; POJ syllables, foreign words, and ambiguous tokens pass through untouched.
     */
    private fun convertKplSyllablesToPoj(text: String, pojFormat: LomajiFormat, options: ConvertOptions): String {
        require(pojFormat == LomajiFormat.POJ_UNICODE || pojFormat == LomajiFormat.POJ_INPUT) {
            "convertKplSyllablesToPoj only supports POJ target formats"
        }
        val kplFormat = if (pojFormat == LomajiFormat.POJ_UNICODE) LomajiFormat.KPL_UNICODE else LomajiFormat.KPL_INPUT
        val normalized = normalizeConfusables(normalizeNfc(text))
        val segments = Tokenizer.splitHybrid(normalized)
        return segments.joinToString("") { segment ->
            if (segment.isLomaji) {
                val syllableTokens = Tokenizer.splitSyllables(segment.text)
                syllableTokens.joinToString("") { token ->
                    if (token.isLomaji &&
                        SyllableValidator.isValid(token.text, kplFormat, strictTones = false, permissive = false, options = options) &&
                        !SyllableValidator.isValid(token.text, pojFormat, strictTones = false, permissive = false, options = options)
                    ) {
                        convertSyllable(token.text, kplFormat, pojFormat, options)
                    } else {
                        token.text
                    }
                }
            } else {
                segment.text
            }
        }
    }

    /**
     * Normalize POJ input "ou" to "oo" (o͘ representation).
     * Many users type "ou" for the o͘ vowel in POJ input mode.
     */
    private fun normalizePojInputOu(syllable: String): String {
        val lower = syllable.lowercase()
        val ouIdx = lower.indexOf("ou")
        if (ouIdx < 0) return syllable
        val oChar = syllable[ouIdx]
        val replaceChar = if (oChar.isUpperCase()) 'O' else 'o'
        return syllable.substring(0, ouIdx + 1) + replaceChar + syllable.substring(ouIdx + 2)
    }

    private fun convertSyllable(syllable: String, from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String {
        // Pipeline: source format → input numbers → (normalize) → (system convert if needed) → target format
        val isAllUpper = syllable.isAllUpper() && syllable.count { it.isLetter() } > 1
        val isPojSource = from == LomajiFormat.POJ_INPUT || from == LomajiFormat.POJ_UNICODE

        // Step 1: Convert source to input-number form
        var inputForm = when (from) {
            LomajiFormat.POJ_INPUT -> normalizePojInputOu(syllable)
            LomajiFormat.POJ_UNICODE -> ToneMarker.pojUnicodeToInput(syllable, isAllUpper)
            LomajiFormat.KPL_INPUT -> syllable
            LomajiFormat.KPL_UNICODE -> ToneMarker.kplUnicodeToInput(syllable)
        }

        // Step 1.5: Traditional nasal normalization (POJ source only)
        if (options.traditionalNasal && isPojSource) {
            inputForm = TraditionalNormalizer.normalizeSyllable(inputForm)
        }

        // Step 2: System conversion if crossing between POJ and KPL
        val isPojTarget = to == LomajiFormat.POJ_INPUT || to == LomajiFormat.POJ_UNICODE
        val convertedInput = when {
            isPojSource && !isPojTarget -> SystemConverter.pojInputToKplInput(inputForm)
            !isPojSource && isPojTarget -> SystemConverter.kplInputToPojInput(inputForm)
            else -> inputForm
        }

        // Step 3: Convert to target format
        return when (to) {
            LomajiFormat.POJ_INPUT -> convertedInput
            LomajiFormat.POJ_UNICODE -> ToneMarker.pojInputToUnicode(convertedInput)
            LomajiFormat.KPL_INPUT -> convertedInput
            LomajiFormat.KPL_UNICODE -> ToneMarker.kplInputToUnicode(convertedInput)
        }
    }
}

/** Extension: convert pure roman orthography text. */
fun String.convertLomaji(from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String =
    KonvertToPoj.convert(this, from, to, options)

/** Extension: convert hybrid text (roman orthography mixed with other content). */
fun String.convertHybridLomaji(from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()): String =
    KonvertToPoj.convertHybrid(this, from, to, options)

/** Extension: validate a single syllable. */
fun String.isValidLomajiSyllable(format: LomajiFormat, options: ConvertOptions = ConvertOptions()): Boolean =
    KonvertToPoj.isValidSyllable(this, format, options)

/** Extension: validate multi-syllable text. */
fun String.isValidLomaji(format: LomajiFormat, options: ConvertOptions = ConvertOptions()): Boolean =
    KonvertToPoj.isValidText(this, format, options)

/** Extension: normalize POJ Unicode by round-tripping through input form. */
fun String.normalizePoj(options: ConvertOptions = ConvertOptions()): String =
    KonvertToPoj.normalizePoj(this, options)

/** Extension: normalize Han-Lo mixed text, forcing full-width punctuation throughout. */
fun String.normalizePojHanLoForceUsingFullwidthPunctuation(
    options: ConvertOptions = ConvertOptions()
): String =
    KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(this, options)

/** Extension: normalize Han-Lo mixed text, choosing punctuation width per sentence. */
fun String.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(
    options: ConvertOptions = ConvertOptions()
): String =
    KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(this, options)

/** Extension: validation-aware POJ normalization (foreign words pass through). */
fun String.normalizePojHybrid(options: ConvertOptions = ConvertOptions()): String =
    KonvertToPoj.normalizePojHybrid(this, options)

/** Extension: validation-aware Han-Lo normalization, forcing full-width punctuation. */
fun String.normalizePojHybridHanLoForceUsingFullwidthPunctuation(
    options: ConvertOptions = ConvertOptions()
): String =
    KonvertToPoj.normalizePojHybridHanLoForceUsingFullwidthPunctuation(this, options)

/** Extension: validation-aware Han-Lo normalization, auto punctuation width per sentence. */
fun String.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
    options: ConvertOptions = ConvertOptions()
): String =
    KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(this, options)
