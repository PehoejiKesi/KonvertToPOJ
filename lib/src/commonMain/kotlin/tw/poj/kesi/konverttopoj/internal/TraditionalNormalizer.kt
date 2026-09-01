package tw.poj.kesi.konverttopoj.internal

/**
 * Normalizes traditional POJ nasalization conventions to standard form.
 *
 * Traditional POJ writes nasal marker (ⁿ/nn) before checked coda (h):
 *   - "aⁿh" / "annh" (traditional) → "ahⁿ" / "ahnn" (standard)
 * And uses plain "o" before nasal for the o͘ vowel:
 *   - "oⁿ" / "onn" (traditional) → "o͘ⁿ" / "oonn" (standard)
 *
 * Normalization operates on the input-number form (after unicode→input conversion).
 */
internal object TraditionalNormalizer {

    /**
     * Normalize a POJ input-mode syllable from traditional to standard form.
     */
    fun normalizeSyllable(inputSyllable: String): String {
        var base = inputSyllable
        var toneSuffix = ""
        if (base.isNotEmpty() && base.last().isDigit()) {
            toneSuffix = base.last().toString()
            base = base.dropLast(1)
        }

        // Order matters: onn→oonn first, then nnh→hnn
        base = normalizeOnn(base)
        base = normalizeNnh(base)

        return base + toneSuffix
    }

    /**
     * "onn" → "oonn": traditional o-nasal → standard o͘-nasal.
     * Only when 'o' is a standalone vowel (not part of a diphthong like "io").
     */
    private fun normalizeOnn(base: String): String {
        val lower = base.lowercase()
        val onnIdx = lower.indexOf("onn")
        if (onnIdx < 0) return base

        // Skip if 'o' is preceded by a vowel (part of diphthong like "io", "ao")
        if (onnIdx > 0 && lower[onnIdx - 1] in "aiueo") return base

        // Insert a second 'o' after the existing one
        val oChar = base[onnIdx]
        val insertChar = if (base.isAllUpper()) oChar else oChar.lowercaseChar()
        return base.substring(0, onnIdx + 1) + insertChar + base.substring(onnIdx + 1)
    }

    /**
     * "nnh" → "hnn": swap nasal marker and checked coda at end of syllable base.
     */
    private fun normalizeNnh(base: String): String {
        val lower = base.lowercase()
        if (!lower.endsWith("nnh")) return base

        val prefix = base.substring(0, base.length - 3)
        val nn = base.substring(base.length - 3, base.length - 1)
        val h = base[base.length - 1]
        return prefix + h + nn
    }
}
