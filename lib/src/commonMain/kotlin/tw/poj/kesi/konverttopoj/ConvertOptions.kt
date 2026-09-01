package tw.poj.kesi.konverttopoj

/**
 * Options for roman orthography conversion and validation.
 */
data class ConvertOptions(
    /**
     * Accept traditional POJ nasalization conventions:
     * - "ⁿh" as "hⁿ" (nasal marker before checked coda)
     * - "oⁿ" as "o͘ⁿ" (plain o for o͘ before nasal marker)
     * - "oⁿh" as "o͘hⁿ" (both combined)
     *
     * When enabled, traditional input is normalized to standard form during conversion,
     * and both traditional and standard forms are accepted during validation.
     */
    val traditionalNasal: Boolean = false,

    /**
     * Include 海口腔 (Hái-kháu-khiuⁿ) coastal dialect vowels:
     * POJ: ur, or | KPL: ir, er
     */
    val haikau: Boolean = false,

    /**
     * Whitespace handling in `normalizePojHanLo*` methods.
     *
     * - `true` (default, **aggressive**): strip all horizontal whitespace and
     *   rebuild canonical spacing. Multiple spaces collapse to one, tabs and
     *   ideographic spaces (U+3000) disappear, leading/trailing whitespace on
     *   a line is removed. Newlines are always preserved.
     * - `false` (**conservative**): preserve the user's whitespace as-is
     *   *except* where Han-Lo rules forbid it. Drops whitespace adjacent to
     *   Hanji and around punctuation (with the half-width clause-punctuation
     *   trailing-space exception); everywhere else — between lomaji words,
     *   between lomaji and digits/symbols, and at line edges — multi-space,
     *   tabs, and ideographic spaces (U+3000) are left untouched.
     *
     * Has no effect outside `normalizePojHanLoForceUsingFullwidthPunctuation`
     * and `normalizePojHanLoAutoChoanLoOrHanLoPunctuation`.
     */
    val aggressiveWhitespace: Boolean = true
)
