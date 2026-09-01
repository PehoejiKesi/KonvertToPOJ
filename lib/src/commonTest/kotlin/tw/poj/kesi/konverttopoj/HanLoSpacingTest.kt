package tw.poj.kesi.konverttopoj

import kotlin.test.Test
import kotlin.test.assertEquals

class HanLoSpacingTest {

    private fun assertSpacing(input: String, expected: String) {
        assertEquals(
            expected,
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(input),
            "normalizePojHanLoForceUsingFullwidthPunctuation(\"$input\")"
        )
    }

    private fun assertAuto(input: String, expected: String) {
        assertEquals(
            expected,
            KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(input),
            "normalizePojHanLoAutoChoanLoOrHanLoPunctuation(\"$input\")"
        )
    }

    // --- Rule 1: No space between Hanji ---

    @Test fun hanji_hanji_removes_space() = assertSpacing("我 是", "我是")
    @Test fun hanji_hanji_multiple_chars() = assertSpacing("台 灣 人", "台灣人")
    @Test fun hanji_hanji_already_no_space() = assertSpacing("台灣人", "台灣人")

    // --- Rule 2: No space between Hanji and Lomaji ---

    @Test fun lomaji_hanji_removes_space() = assertSpacing("góa 是", "góa是")
    @Test fun hanji_lomaji_removes_space() = assertSpacing("是 góa", "是góa")
    @Test fun hanji_lomaji_hanji() = assertSpacing("我 ê 冊", "我ê冊")
    @Test fun lomaji_hanji_lomaji() = assertSpacing("góa 是 lâng", "góa是lâng")

    // --- Rule 3: Single space between Lomaji words ---

    @Test fun lomaji_lomaji_single_space() = assertSpacing("góa sī", "góa sī")
    @Test fun lomaji_lomaji_multiple_spaces() = assertSpacing("góa  sī", "góa sī")
    @Test fun lomaji_lomaji_many_spaces() = assertSpacing("góa   sī   lâng", "góa sī lâng")
    @Test fun lomaji_hyphenated_word() = assertSpacing("Tâi-gí chin hó", "Tâi-gí chin hó")
    @Test fun lomaji_preserves_hyphens() = assertSpacing("tâi-oân-lâng", "tâi-oân-lâng")

    // --- Rule 4: No space around punctuation ---

    @Test fun fullwidth_comma_no_space() = assertSpacing("góa ， 是", "góa，是")
    @Test fun fullwidth_period_no_space() = assertSpacing("好 。", "好。")
    @Test fun fullwidth_brackets_no_space() = assertSpacing("「 góa 」", "「góa」")
    @Test fun fullwidth_parens() = assertSpacing("（ 台語 ）", "（台語）")
    @Test fun punctuation_after_lomaji() = assertSpacing("hó ，", "hó，")
    @Test fun punctuation_before_hanji() = assertSpacing("， 好", "，好")

    // --- Punctuation conversion: half-width → full-width ---

    @Test fun convert_comma() = assertSpacing("góa , 你", "góa，你")
    @Test fun convert_period() = assertSpacing("好 .", "好。")
    @Test fun convert_exclamation() = assertSpacing("hó !", "hó！")
    @Test fun convert_question() = assertSpacing("sī ?", "sī？")
    @Test fun convert_semicolon() = assertSpacing("góa ;", "góa；")
    @Test fun convert_colon() = assertSpacing("注意 :", "注意：")
    @Test fun convert_parens() = assertSpacing("( góa )", "（góa）")

    // --- Rule 5: Digits adjacent to Hanji drop their space ---

    @Test fun digit_near_hanji() = assertSpacing("第 3 名", "第3名")
    @Test fun digit_after_hanji() = assertSpacing("第 1", "第1")
    // POJ-input syllables get canonicalized to Unicode form by the pre-normalization step.
    @Test fun input_syllable_canonicalized() = assertSpacing("goa2 是", "góa是")

    // --- Rule 6: Newlines preserved ---

    @Test fun preserves_newlines() = assertSpacing("我 是\n你 是", "我是\n你是")
    @Test fun preserves_multiple_newlines() = assertSpacing("好\n\n好", "好\n\n好")

    // --- Mixed sentences ---

    @Test fun mixed_sentence() = assertSpacing(
        "góa 是 台灣 ê lâng ，真 好 。",
        "góa是台灣ê lâng，真好。"
    )

    @Test fun mixed_sentence_with_conversion() = assertSpacing(
        "Lí 好 , 我 是 台灣 lâng .",
        "Lí好，我是台灣lâng。"
    )

    @Test fun mixed_with_hyphenated_words() = assertSpacing(
        "台灣 ê gín-á 真 古錐 。",
        "台灣ê gín-á真古錐。"
    )

    // --- Edge cases ---

    @Test fun empty_string() = assertSpacing("", "")
    @Test fun blank_string() = assertSpacing("   ", "   ")
    @Test fun only_hanji() = assertSpacing("台灣人", "台灣人")
    @Test fun only_lomaji() = assertSpacing("tâi-oân-lâng", "tâi-oân-lâng")
    @Test fun single_hanji() = assertSpacing("我", "我")
    @Test fun single_lomaji() = assertSpacing("góa", "góa")

    // --- Extension function ---

    @Test fun extension_function_works() {
        assertEquals("我是", "我 是".normalizePojHanLoForceUsingFullwidthPunctuation())
    }

    // --- Combined POJ tone canonicalization + spacing ---

    @Test fun normalizes_misplaced_tone_marks_then_fixes_spacing() {
        // sìan → siàn (canonical) and the space between Hanji and roman orthography is dropped.
        assertEquals(
            "我siàn-tio̍h",
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation("我 sìan-tio̍h")
        )
    }

    // ---------------------------------------------------------------------------
    // AUTO mode: choose punctuation width per sentence based on Hanji presence
    // ---------------------------------------------------------------------------

    // --- Pure-lomaji sentence → half-width punctuation ---

    @Test fun auto_pure_lomaji_keeps_halfwidth_comma() =
        assertAuto("góa , lí", "góa, lí")

    @Test fun auto_pure_lomaji_converts_full_to_half_comma() =
        assertAuto("góa ， lí", "góa, lí")

    @Test fun auto_pure_lomaji_period_halfwidth() =
        assertAuto("Tâi-gí chin hó .", "Tâi-gí chin hó.")

    @Test fun auto_pure_lomaji_question_halfwidth() =
        assertAuto("Lí hó ？", "Lí hó?")

    @Test fun auto_pure_lomaji_exclamation_halfwidth() =
        assertAuto("Án-ne ！", "Án-ne!")

    @Test fun auto_pure_lomaji_parens_halfwidth() =
        assertAuto("góa （ lí ）", "góa(lí)")

    @Test fun auto_pure_lomaji_ideographic_comma_to_halfwidth() =
        assertAuto("góa 、 lí", "góa, lí")

    // --- Han-Lo sentence → full-width punctuation (same as FORCE mode) ---

    @Test fun auto_hanlo_uses_fullwidth_comma() =
        assertAuto("góa , 是 lâng", "góa，是lâng")

    @Test fun auto_hanlo_uses_fullwidth_period() =
        assertAuto("我 是 lâng .", "我是lâng。")

    @Test fun auto_hanlo_keeps_existing_fullwidth() =
        assertAuto("góa ， 是", "góa，是")

    // --- Mixed: per-sentence decision ---

    @Test fun auto_mixed_per_sentence() = assertAuto(
        "góa 是 lâng . Lí hó ?",
        "góa是lâng。Lí hó?"
    )

    @Test fun auto_lomaji_then_hanlo() = assertAuto(
        "Lí hó . 我 是 lâng .",
        "Lí hó.我是lâng。"
    )

    // --- Trailing-no-terminator sentence ---

    @Test fun auto_trailing_lomaji_no_terminator_halfwidth() =
        assertAuto("Tâi-gí chin hó ， án-ne", "Tâi-gí chin hó, án-ne")

    @Test fun auto_trailing_hanlo_no_terminator_fullwidth() =
        assertAuto("góa , 是", "góa，是")

    // --- Non-terminator punctuation does not split sentences ---

    @Test fun auto_semicolon_does_not_split_sentence() {
        // ; is not a sentence terminator — the whole line is one sentence,
        // and because it contains Hanji the ; is rendered full-width too.
        assertAuto("góa 是 ; lí hó .", "góa是；lí hó。")
    }

    @Test fun auto_colon_does_not_split_sentence() {
        // : in a pure-lomaji sentence stays half-width and gets a trailing space.
        assertAuto("Tâi-gí : chin hó .", "Tâi-gí: chin hó.")
    }

    // --- Spacing rules still apply ---

    @Test fun auto_pure_lomaji_collapses_spaces() =
        assertAuto("góa   sī   lâng", "góa sī lâng")

    @Test fun auto_hanlo_drops_inner_spaces() =
        assertAuto("我 是 台灣 lâng", "我是台灣lâng")

    // --- Extension function ---

    @Test fun auto_extension_function_works() {
        assertEquals("Tâi-gí.", "Tâi-gí .".normalizePojHanLoAutoChoanLoOrHanLoPunctuation())
        assertEquals("我是lâng。", "我 是 lâng .".normalizePojHanLoAutoChoanLoOrHanLoPunctuation())
    }

    // --- POJ canonicalization still applies in auto mode ---

    @Test fun auto_canonicalizes_tone_marks_pure_lomaji() {
        assertEquals(
            "siàn-tio̍h.",
            KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation("sìan-tio̍h .")
        )
    }

    // ---------------------------------------------------------------------------
    // Conservative whitespace (aggressiveWhitespace = false): preserve user
    // whitespace except where Han-Lo rules forbid it.
    // ---------------------------------------------------------------------------

    private val conservative = ConvertOptions(aggressiveWhitespace = false)

    private fun assertSpacingConservative(input: String, expected: String) {
        assertEquals(
            expected,
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(input, conservative),
            "conservative force(\"$input\")"
        )
    }

    private fun assertAutoConservative(input: String, expected: String) {
        assertEquals(
            expected,
            KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(input, conservative),
            "conservative auto(\"$input\")"
        )
    }

    // --- Han-Lo rule violations are still corrected ---

    @Test fun conservative_drops_space_between_hanji() =
        assertSpacingConservative("我 是", "我是")

    @Test fun conservative_drops_space_hanji_lomaji() =
        assertSpacingConservative("我 góa", "我góa")

    @Test fun conservative_drops_space_around_punctuation() =
        assertSpacingConservative("góa ， 是", "góa，是")

    // --- User whitespace between lomaji is preserved ---

    @Test fun conservative_preserves_multiple_spaces_between_lomaji() =
        assertSpacingConservative("góa   sī   lâng", "góa   sī   lâng")

    @Test fun conservative_preserves_tab_between_lomaji() =
        assertSpacingConservative("góa\tsī", "góa\tsī")

    @Test fun conservative_preserves_leading_whitespace() =
        assertSpacingConservative("  góa hó", "  góa hó")

    @Test fun conservative_preserves_ideographic_space_between_lomaji() =
        assertSpacingConservative("góa　sī", "góa　sī")

    // --- Auto mode in conservative ---

    @Test fun conservative_auto_preserves_multi_space_in_lomaji_sentence() =
        assertAutoConservative("Tâi-gí   chin   hó .", "Tâi-gí   chin   hó.")

    @Test fun conservative_auto_keeps_halfwidth_trailing_space_after_punct() =
        assertAutoConservative("góa , lí", "góa, lí")

    @Test fun conservative_auto_hanlo_drops_inner_spaces() =
        assertAutoConservative("我  是  lâng .", "我是lâng。")

    // --- README example ---

    @Test fun readme_aggressive_example() {
        assertEquals(
            "góa sī lâng，真好。",
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation("  góa   sī\tlâng ， 真 好 。")
        )
    }

    @Test fun readme_conservative_example() {
        assertEquals(
            "  góa   sī\tlâng，真好。",
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(
                "  góa   sī\tlâng ， 真 好 。",
                ConvertOptions(aggressiveWhitespace = false)
            )
        )
    }

    // --- Multi-line conservative: leading whitespace on inner lines preserved ---

    @Test fun conservative_preserves_indent_on_inner_line() =
        assertSpacingConservative("góa\n  hó", "góa\n  hó")

    @Test fun conservative_preserves_indent_across_multiple_lines() =
        assertSpacingConservative("我 是\n\t  góa  hó", "我是\n\t  góa  hó")

    // --- Conservative composes with traditionalNasal (orthogonal options) ---

    @Test fun conservative_with_traditional_nasal_preserves_whitespace_and_canonicalizes() {
        // traditionalNasal canonicalizes traditional "oⁿ" → standard "o͘ⁿ" inside normalizePoj;
        // aggressiveWhitespace=false then preserves user spacing in the surrounding text.
        // The two options must compose.
        val opts = ConvertOptions(aggressiveWhitespace = false, traditionalNasal = true)
        assertEquals(
            "góa   ho͘ⁿ",
            KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation("góa   hoⁿ", opts)
        )
    }

    // --- CRLF line endings are preserved symmetrically across both modes ---

    @Test fun aggressive_preserves_crlf() =
        assertSpacing("我 是\r\n你 是", "我是\r\n你是")

    @Test fun conservative_preserves_crlf() =
        assertSpacingConservative("我 是\r\n你 是", "我是\r\n你是")

    @Test fun aggressive_strips_trailing_spaces_but_keeps_crlf() =
        assertSpacing("góa  \r\nhó", "góa\r\nhó")

    @Test fun conservative_keeps_trailing_spaces_and_crlf() =
        assertSpacingConservative("góa  \r\nhó", "góa  \r\nhó")

    // --- Whitespace-separated OTHER tokens (digits, symbols) keep a space ---
    // Two OTHER tokens can only end up separate if whitespace stood between them
    // in the input, so the boundary itself signals they're meant to stay apart.

    @Test fun aggressive_keeps_space_between_two_digits() =
        assertSpacing("1 2", "1 2")

    @Test fun aggressive_collapses_multispace_between_digits() =
        assertSpacing("100   200", "100 200")

    @Test fun conservative_keeps_space_between_two_digits() =
        assertSpacingConservative("1 2", "1 2")

    @Test fun conservative_preserves_multi_space_between_digits() =
        assertSpacingConservative("100   200", "100   200")

    // --- Half-width clause punctuation gets trailing space before digits too ---

    @Test fun aggressive_halfwidth_comma_trailing_space_before_digit() =
        assertAuto("góa, 3 lí", "góa, 3 lí")

    @Test fun aggressive_halfwidth_colon_trailing_space_before_digit() =
        assertAuto("Tâi-gí: 1 hāng", "Tâi-gí: 1 hāng")

    @Test fun conservative_halfwidth_clause_keeps_trailing_space_before_digit() =
        assertAutoConservative("góa, 3 lí", "góa, 3 lí")

    @Test fun conservative_halfwidth_clause_drops_leading_space_before_punct_with_digit() =
        assertAutoConservative("góa , 3", "góa, 3")

    // --- isBlank() early return: blank-only inputs are returned verbatim across modes ---

    @Test fun aggressive_blank_only_input_preserved() = assertSpacing("\t  ", "\t  ")
    @Test fun aggressive_crlf_only_input_preserved() = assertSpacing("\r\n", "\r\n")
    @Test fun conservative_blank_only_input_preserved() =
        assertSpacingConservative("\t  ", "\t  ")
    @Test fun conservative_crlf_only_input_preserved() =
        assertSpacingConservative("\r\n", "\r\n")

    // --- Mixed CRLF and LF line endings preserved per-line ---

    @Test fun aggressive_mixed_crlf_and_lf() =
        assertSpacing("我 是\r\n你 是\n好", "我是\r\n你是\n好")

    @Test fun conservative_mixed_crlf_and_lf() =
        assertSpacingConservative("我 是\r\n你 是\n好", "我是\r\n你是\n好")
}
