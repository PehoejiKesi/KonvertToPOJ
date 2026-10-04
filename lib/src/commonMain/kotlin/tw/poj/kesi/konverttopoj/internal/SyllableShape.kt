package tw.poj.kesi.konverttopoj.internal

import tw.poj.kesi.konverttopoj.LomajiFormat

/**
 * Structural syllable check, run before converting a syllable in any direction:
 *
 *     [onset] + nucleus (any combination of 1–3 vowels, e.g. a, oa, iai) + [nasal] + [coda] + [tone]
 *     [onset] + syllabic m / ng + [h] + [tone]
 *
 * Tone — input formats: an optional trailing digit 2, 3, 5, 7, 8 or 9. Tones 1 and 4 are
 * normally written without a digit; a typed 1 or 4 is still accepted (and dropped by the
 * converter). Unicode formats: at most one tone diacritic and no digits — unless
 * `allowToneDigit` (any route through input form), where a trailing tone digit is accepted too.
 * Tones 4 and 8 require a checked syllable (final p, t, k or h).
 * After a nasal onset (m, n, ng) the nasal marker never appears.
 *
 * This is what keeps foreign words such as "connecting" or "Tennessee" from being
 * treated as POJ / KPL.
 *
 * POJ — onset: ph p m b th t l kh k ng n g h chh ch s j
 *       vowels: a i u e o o͘, coastal ṳ o̤ (also typed ur / or)
 *       nasal: ⁿ (also typed nn), before or after a final h
 * KPL — onset: ph p m b th t l kh k ng n g h tsh ts s j
 *       vowels: a i u e o oo, coastal ir er
 *       nasal: nn, before or after a final h
 * Coda (both): ng m n p t k h
 *
 * Unlike [SyllableValidator] this does not whitelist rhymes; it only checks the shape.
 */
internal object SyllableShape {

    private const val CODA = "ng|m|n|p|t|k|h"
    private const val SYLLABIC = "(?:m|ng)h?"

    private val POJ = shape(
        onset = "chh|ch|ph|th|kh|ng|p|m|b|t|l|k|n|g|h|s|j",
        vowel = "o͘|o̤|ṳ|ur|or|[aiueo]",
        nasal = "ⁿ|nn",
    )

    private val KPL = shape(
        onset = "tsh|ts|ph|th|kh|ng|p|m|b|t|l|k|n|g|h|s|j",
        vowel = "ir|er|[aiueo]",
        nasal = "nn",
    )

    private fun shape(onset: String, vowel: String, nasal: String): Regex {
        val rhyme = "(?:$vowel){1,3}(?:(?:$nasal)h?|h(?:$nasal)|$CODA)?"
        return "^(?:$onset)?(?:$rhyme|$SYLLABIC)$".toRegex()
    }

    private const val INPUT_TONES = "12345789"

    fun matches(syllable: String, format: LomajiFormat, allowToneDigit: Boolean = false): Boolean {
        val isInput = format == LomajiFormat.POJ_INPUT || format == LomajiFormat.KPL_INPUT
        val last = syllable.lastOrNull() ?: return false
        val digitTone = if (last in INPUT_TONES && (isInput || allowToneDigit)) last.digitToInt() else null
        val body = if (digitTone != null) syllable.dropLast(1) else syllable

        val (bare, markTone) = if (isInput) body to null else ToneMarker.stripToneMark(body) ?: return false
        if (digitTone != null && markTone != null) return false
        val tone = digitTone ?: markTone

        return when (format) {
            LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE -> isPoj(bare, tone)
            LomajiFormat.KPL_INPUT, LomajiFormat.KPL_UNICODE -> isKpl(bare, tone)
        }
    }

    /** Tones 4 and 8 are checked tones: the syllable must end in p, t, k or h (a trailing nasal marker is ignored). */
    private fun toneFits(lower: String, tone: Int?, nasal: String): Boolean {
        if (tone != 4 && tone != 8) return true
        return lower.removeSuffix(nasal).lastOrNull()?.let { it in "ptkh" } == true
    }

    /**
     * A nasal onset (m, n, ng) already nasalizes the vowel, so the syllable never also
     * carries the nasal marker: `niû`, not `niûⁿ`.
     */
    private fun nasalFits(lower: String, nasals: List<String>): Boolean {
        val rest = when {
            lower.startsWith("ng") -> lower.drop(2)
            lower.startsWith("m") || lower.startsWith("n") -> lower.drop(1)
            else -> return true
        }
        return nasals.none { it in rest }
    }

    fun isPoj(bare: String, tone: Int?): Boolean {
        val lower = bare.lowercase()
        return POJ.matches(lower) &&
            toneFits(lower.removeSuffix("nn"), tone, "\u207F") &&
            nasalFits(lower, listOf("\u207F", "nn"))
    }

    fun isKpl(bare: String, tone: Int?): Boolean {
        val lower = bare.lowercase()
        return KPL.matches(lower) && toneFits(lower, tone, "nn") && nasalFits(lower, listOf("nn"))
    }
}
