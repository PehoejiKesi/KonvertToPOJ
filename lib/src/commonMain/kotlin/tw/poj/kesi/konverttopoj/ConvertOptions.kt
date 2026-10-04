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
    val aggressiveWhitespace: Boolean = true,

    /**
     * How `POJ_UNICODE ↔ KPL_UNICODE` conversion is carried out.
     *
     * - `false` (default, **direct**): the tone is read off the diacritic and
     *   carried as a value, the bare letters are converted between systems, and
     *   the tone mark is re-placed by the target system's rules. Digits in the
     *   text are never read as tone numbers — a token containing a digit (e.g.
     *   `goa2`, `COVID19`) passes through unchanged — and a syllable carrying two
     *   conflicting tone marks is left as-is.
     *   (Other directions from a Unicode format still go through input form, where a
     *   trailing digit is a tone number, e.g. `goa2` POJ_UNICODE → KPL_INPUT gives `gua2`.)
     * - `true` (**via input form**): the original pipeline — Unicode → input
     *   (tone-number) form → system convert → Unicode. A trailing digit in a
     *   Unicode token is treated as a tone number (`goa2` → `guá`).
     *
     * Has no effect on any other conversion direction.
     */
    val viaInputForm: Boolean = false
)
