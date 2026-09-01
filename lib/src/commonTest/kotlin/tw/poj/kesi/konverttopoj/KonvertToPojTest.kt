package tw.poj.kesi.konverttopoj

import tw.poj.kesi.konverttopoj.LomajiFormat.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class KonvertToPojTest {

    // =========================================================================
    // POJ Input → POJ Unicode — tone placement (Section 21)
    // =========================================================================

    // Rule 1: single vowel → mark it
    @Test fun pojIn2Uni_singleVowel_a2() = assertConvert("a2", "á", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_a3() = assertConvert("a3", "à", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_a5() = assertConvert("a5", "â", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_a7() = assertConvert("a7", "ā", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_a9() = assertConvert("a9", "ă", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_i2() = assertConvert("i2", "í", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_u5() = assertConvert("u5", "û", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_e5() = assertConvert("e5", "ê", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_singleVowel_o2() = assertConvert("o2", "ó", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_withInitial_tai5() = assertConvert("tai5", "tâi", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_withInitial_gi2() = assertConvert("gi2", "gí", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_withInitial_ho2() = assertConvert("ho2", "hó", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_withInitial_si7() = assertConvert("si7", "sī", POJ_INPUT, POJ_UNICODE)

    // Rule 2: no vowel → mark nasal
    @Test fun pojIn2Uni_nasal_m7() = assertConvert("m7", "m̄", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_nasal_m2() = assertConvert("m2", "ḿ", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_nasal_ng5() = assertConvert("ng5", "n̂g", POJ_INPUT, POJ_UNICODE)

    // Rule 3: ur/or coastal dialect → mark as 2-char unit
    @Test fun pojIn2Uni_coastal_ur2() = assertConvert("ur2", "ṳ\u0301", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_coastal_or5() {
        // or5 → o̤ with circumflex; NFC normalizes combining mark order
        val result = KonvertToPoj.convert("or5", POJ_INPUT, POJ_UNICODE)
        val backToInput = KonvertToPoj.convert(result, POJ_UNICODE, POJ_INPUT)
        assertEquals("or5", backToInput, "roundtrip or5 → POJ_UNICODE → POJ_INPUT")
    }

    // Rule 4: oo → mark as 2-char unit (becomes o͘)
    @Test fun pojIn2Uni_oo_goo7() = assertConvert("goo7", "gō͘", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_oo_oo2() = assertConvert("oo2", "ó͘", POJ_INPUT, POJ_UNICODE)

    // Rule 5: compound vowels → mark 2nd letter from right
    @Test fun pojIn2Uni_compound_iau5() = assertConvert("iau5", "iâu", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_compound_oai2() = assertConvert("oai2", "oái", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_compound_au5() = assertConvert("au5", "âu", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_compound_ai5() = assertConvert("ai5", "âi", POJ_INPUT, POJ_UNICODE)

    // Exception 1: 2nd from right is "i" → mark rightmost
    @Test fun pojIn2Uni_exc1_ia2() = assertConvert("ia2", "iá", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_exc1_io2() = assertConvert("io2", "ió", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_exc1_iu2() = assertConvert("iu2", "iú", POJ_INPUT, POJ_UNICODE)

    // Exception 2: checked + 2nd from right is i/u → mark 3rd
    @Test fun pojIn2Uni_exc2_aih8() = assertConvert("aih8", "a̍ih", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_exc2_auh8() = assertConvert("auh8", "a̍uh", POJ_INPUT, POJ_UNICODE)

    // Special: iuh → mark 2nd from right
    @Test fun pojIn2Uni_special_iuh8() = assertConvert("iuh8", "iu\u030Dh", POJ_INPUT, POJ_UNICODE)

    // Special: iuh with nasal — ⁿ is transparent, still mark the "u" (hiu\u030Dⁿh, not hi\u030Duⁿh)
    @Test fun pojIn2Uni_special_iunnh8() = assertConvert("iunnh8", "iu\u030D\u207Fh", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_special_hiunnh8() = assertConvert("hiunnh8", "hiu\u030D\u207Fh", POJ_INPUT, POJ_UNICODE)
    @Test fun pojUni2In_special_hiunnh8() = assertConvert("hiu\u030D\u207Fh", "hiunnh8", POJ_UNICODE, POJ_INPUT)

    // Checked syllables (tone 8)
    @Test fun pojIn2Uni_checked_kap8() = assertConvert("kap8", "ka̍p", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_checked_lat8() = assertConvert("lat8", "la̍t", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_checked_chit8() = assertConvert("chit8", "chi̍t", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_checked_oh8() = assertConvert("oh8", "o̍h", POJ_INPUT, POJ_UNICODE)

    // Tone 1 and tone 4 — no diacritical
    @Test fun pojIn2Uni_tone1_ka() = assertConvert("ka", "ka", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_tone4_kap() = assertConvert("kap", "kap", POJ_INPUT, POJ_UNICODE)

    // nn → ⁿ
    @Test fun pojIn2Uni_nasal_hainn2() = assertConvert("hainn2", "háiⁿ", POJ_INPUT, POJ_UNICODE)
    @Test fun pojIn2Uni_nasal_ann2() = assertConvert("ann2", "áⁿ", POJ_INPUT, POJ_UNICODE)

    // nng preserved (not converted to ⁿg)
    @Test fun pojIn2Uni_nng_preserved() = assertConvert("nng", "nng", POJ_INPUT, POJ_UNICODE)

    // Multi-syllable sentence
    @Test fun pojIn2Uni_sentence() = assertConvert(
        "goo2-kong7 e7-hiau2 oh8 tai5-gi2",
        "gó͘-kōng ē-hiáu o̍h tâi-gí",
        POJ_INPUT, POJ_UNICODE
    )

    // =========================================================================
    // POJ Unicode → POJ Input — tone removal
    // =========================================================================

    @Test fun pojUni2In_tai5() = assertConvert("tâi", "tai5", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_gi2() = assertConvert("gí", "gi2", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_compound() = assertConvert("tâi-gí", "tai5-gi2", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_oo() = assertConvert("gō͘", "goo7", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_nasal() = assertConvert("háiⁿ", "hainn2", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_checked() = assertConvert("ka̍p", "kap8", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_m7() = assertConvert("m̄", "m7", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_ng5() = assertConvert("n̂g", "ng5", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_tone1() = assertConvert("ka", "ka", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_tone9() = assertConvert("ă", "a9", POJ_UNICODE, POJ_INPUT)
    @Test fun pojUni2In_sentence() = assertConvert(
        "gó͘-kōng ē-hiáu o̍h tâi-gí",
        "goo2-kong7 e7-hiau2 oh8 tai5-gi2",
        POJ_UNICODE, POJ_INPUT
    )

    // =========================================================================
    // POJ Input ↔ KPL Input — orthographic conversion (Section 44)
    // =========================================================================

    // ch → ts
    @Test fun pojIn2KplIn_ch() = assertConvert("chit8", "tsit8", POJ_INPUT, KPL_INPUT)
    @Test fun pojIn2KplIn_chh() = assertConvert("chhiu2", "tshiu2", POJ_INPUT, KPL_INPUT)

    // oa → ua, oe → ue
    @Test fun pojIn2KplIn_oa() = assertConvert("oan5", "uan5", POJ_INPUT, KPL_INPUT)
    @Test fun pojIn2KplIn_oe() = assertConvert("oe7", "ue7", POJ_INPUT, KPL_INPUT)

    // ek → ik, eng → ing
    @Test fun pojIn2KplIn_ek() = assertConvert("tek8", "tik8", POJ_INPUT, KPL_INPUT)
    @Test fun pojIn2KplIn_eng() = assertConvert("seng", "sing", POJ_INPUT, KPL_INPUT)

    // ur → ir, or → er
    @Test fun pojIn2KplIn_ur() = assertConvert("ur2", "ir2", POJ_INPUT, KPL_INPUT)
    @Test fun pojIn2KplIn_or() = assertConvert("or5", "er5", POJ_INPUT, KPL_INPUT)

    // Reverse: KPL → POJ
    @Test fun kplIn2PojIn_ts() = assertConvert("tsit8", "chit8", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_tsh() = assertConvert("tshiu2", "chhiu2", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_ua() = assertConvert("uan5", "oan5", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_ue() = assertConvert("ue7", "oe7", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_ik() = assertConvert("tik8", "tek8", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_ing() = assertConvert("sing", "seng", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_ir() = assertConvert("ir2", "ur2", KPL_INPUT, POJ_INPUT)
    @Test fun kplIn2PojIn_er() = assertConvert("er5", "or5", KPL_INPUT, POJ_INPUT)

    // =========================================================================
    // KPL Input → KPL Unicode — tone placement (Section 45)
    // =========================================================================

    // Rule 1: if 'a' present → mark the 'a'
    @Test fun kplIn2Uni_a_rule_tai5() = assertConvert("tai5", "tâi", KPL_INPUT, KPL_UNICODE)
    @Test fun kplIn2Uni_a_rule_iau5() = assertConvert("iau5", "iâu", KPL_INPUT, KPL_UNICODE)
    @Test fun kplIn2Uni_a_rule_ua5() = assertConvert("ua5", "uâ", KPL_INPUT, KPL_UNICODE)

    // Rule 2: rightmost vowel (oo as unit, mark left o)
    @Test fun kplIn2Uni_rightmost_oo7() = assertConvert("oo7", "ōo", KPL_INPUT, KPL_UNICODE)
    @Test fun kplIn2Uni_rightmost_tiu2() = assertConvert("tiu2", "tiú", KPL_INPUT, KPL_UNICODE)
    @Test fun kplIn2Uni_rightmost_ue7() = assertConvert("ue7", "uē", KPL_INPUT, KPL_UNICODE)

    // Rule 3: nasal
    @Test fun kplIn2Uni_nasal_ng5() = assertConvert("ng5", "n̂g", KPL_INPUT, KPL_UNICODE)
    @Test fun kplIn2Uni_nasal_m7() = assertConvert("m7", "m̄", KPL_INPUT, KPL_UNICODE)

    // Tone 9 — double acute accent (different from POJ breve)
    @Test fun kplIn2Uni_tone9() = assertConvert("a9", "a\u030B", KPL_INPUT, KPL_UNICODE)

    // =========================================================================
    // KPL Unicode → KPL Input
    // =========================================================================

    @Test fun kplUni2In_tai5() = assertConvert("tâi", "tai5", KPL_UNICODE, KPL_INPUT)
    @Test fun kplUni2In_oo7() = assertConvert("ōo", "oo7", KPL_UNICODE, KPL_INPUT)
    @Test fun kplUni2In_tsit8() = assertConvert("tsi̍t", "tsit8", KPL_UNICODE, KPL_INPUT)
    @Test fun kplUni2In_tone9() = assertConvert("a\u030B", "a9", KPL_UNICODE, KPL_INPUT)
    @Test fun kplUni2In_ng5() = assertConvert("n̂g", "ng5", KPL_UNICODE, KPL_INPUT)

    // =========================================================================
    // Cross-system: POJ Input → KPL Unicode
    // =========================================================================

    @Test fun pojIn2KplUni_chit8() = assertConvert("chit8", "tsi̍t", POJ_INPUT, KPL_UNICODE)
    @Test fun pojIn2KplUni_tai5gi2() = assertConvert("tai5-gi2", "tâi-gí", POJ_INPUT, KPL_UNICODE)
    @Test fun pojIn2KplUni_sentence() = assertConvert(
        "goo2-kong7 e7-hiau2 oh8 tai5-gi2",
        "góo-kōng ē-hiáu o̍h tâi-gí",
        POJ_INPUT, KPL_UNICODE
    )
    @Test fun pojIn2KplUni_oan5() = assertConvert("oan5", "uân", POJ_INPUT, KPL_UNICODE)
    @Test fun pojIn2KplUni_seng() = assertConvert("seng", "sing", POJ_INPUT, KPL_UNICODE)

    // =========================================================================
    // Cross-system: KPL Input → POJ Unicode
    // =========================================================================

    @Test fun kplIn2PojUni_tsit8() = assertConvert("tsit8", "chi̍t", KPL_INPUT, POJ_UNICODE)
    @Test fun kplIn2PojUni_uan5() = assertConvert("uan5", "oân", KPL_INPUT, POJ_UNICODE)
    @Test fun kplIn2PojUni_sing() = assertConvert("sing", "seng", KPL_INPUT, POJ_UNICODE)

    // =========================================================================
    // Cross-system: POJ Unicode → KPL Unicode
    // =========================================================================

    @Test fun pojUni2KplUni_chit8() = assertConvert("chi̍t", "tsi̍t", POJ_UNICODE, KPL_UNICODE)
    @Test fun pojUni2KplUni_oan5() = assertConvert("oân", "uân", POJ_UNICODE, KPL_UNICODE)
    @Test fun pojUni2KplUni_seng7() = assertConvert("sēng", "sīng", POJ_UNICODE, KPL_UNICODE)
    @Test fun pojUni2KplUni_oo() = assertConvert("gō͘", "gōo", POJ_UNICODE, KPL_UNICODE)
    @Test fun pojUni2KplUni_tone9() = assertConvert("ă", "a\u030B", POJ_UNICODE, KPL_UNICODE)

    // =========================================================================
    // Cross-system: KPL Unicode → POJ Unicode
    // =========================================================================

    @Test fun kplUni2PojUni_tsit8() = assertConvert("tsi̍t", "chi̍t", KPL_UNICODE, POJ_UNICODE)
    @Test fun kplUni2PojUni_uan5() = assertConvert("uân", "oân", KPL_UNICODE, POJ_UNICODE)
    @Test fun kplUni2PojUni_oo7() = assertConvert("gōo", "gō͘", KPL_UNICODE, POJ_UNICODE)

    // =========================================================================
    // Cross-system: POJ Unicode ↔ KPL Input
    // =========================================================================

    @Test fun pojUni2KplIn_chit8() = assertConvert("chi̍t", "tsit8", POJ_UNICODE, KPL_INPUT)
    @Test fun pojUni2KplIn_oan5() = assertConvert("oân", "uan5", POJ_UNICODE, KPL_INPUT)

    @Test fun kplIn2PojUni_ue7() = assertConvert("ue7", "ōe", KPL_INPUT, POJ_UNICODE)

    // =========================================================================
    // Cross-system: KPL Unicode → POJ Input
    // =========================================================================

    @Test fun kplUni2PojIn_tsit8() = assertConvert("tsi̍t", "chit8", KPL_UNICODE, POJ_INPUT)
    @Test fun kplUni2PojIn_uan5() = assertConvert("uân", "oan5", KPL_UNICODE, POJ_INPUT)

    // =========================================================================
    // Identity — same format returns unchanged
    // =========================================================================

    @Test fun identity_pojInput() {
        assertEquals("tai5-gi2", KonvertToPoj.convert("tai5-gi2", POJ_INPUT, POJ_INPUT))
    }

    @Test fun identity_pojUnicode() {
        assertEquals("tâi-gí", KonvertToPoj.convert("tâi-gí", POJ_UNICODE, POJ_UNICODE))
    }

    @Test fun identity_kplInput() {
        assertEquals("tai5-gi2", KonvertToPoj.convert("tai5-gi2", KPL_INPUT, KPL_INPUT))
    }

    @Test fun identity_kplUnicode() {
        assertEquals("tâi-gí", KonvertToPoj.convert("tâi-gí", KPL_UNICODE, KPL_UNICODE))
    }

    // =========================================================================
    // Roundtrip — convert A→B→A should return original
    // =========================================================================

    @Test fun roundtrip_pojInput_pojUnicode() {
        val original = "tai5-gi2"
        val unicode = KonvertToPoj.convert(original, POJ_INPUT, POJ_UNICODE)
        assertEquals(original, KonvertToPoj.convert(unicode, POJ_UNICODE, POJ_INPUT))
    }

    @Test fun roundtrip_pojInput_kplInput() {
        val original = "chit8-e5"
        val kpl = KonvertToPoj.convert(original, POJ_INPUT, KPL_INPUT)
        assertEquals(original, KonvertToPoj.convert(kpl, KPL_INPUT, POJ_INPUT))
    }

    @Test fun roundtrip_kplInput_kplUnicode() {
        val original = "tsit8-e5"
        val unicode = KonvertToPoj.convert(original, KPL_INPUT, KPL_UNICODE)
        assertEquals(original, KonvertToPoj.convert(unicode, KPL_UNICODE, KPL_INPUT))
    }

    @Test fun roundtrip_pojInput_kplUnicode() {
        val original = "goo2-kong7"
        val kplUni = KonvertToPoj.convert(original, POJ_INPUT, KPL_UNICODE)
        val back = KonvertToPoj.convert(kplUni, KPL_UNICODE, KPL_INPUT)
        val pojBack = KonvertToPoj.convert(back, KPL_INPUT, POJ_INPUT)
        assertEquals(original, pojBack)
    }

    @Test fun roundtrip_sentence() {
        val original = "goo2-kong7 e7-hiau2 oh8 tai5-gi2"
        val pojUni = KonvertToPoj.convert(original, POJ_INPUT, POJ_UNICODE)
        val kplIn = KonvertToPoj.convert(pojUni, POJ_UNICODE, KPL_INPUT)
        val kplUni = KonvertToPoj.convert(kplIn, KPL_INPUT, KPL_UNICODE)
        val backKplIn = KonvertToPoj.convert(kplUni, KPL_UNICODE, KPL_INPUT)
        val backPojIn = KonvertToPoj.convert(backKplIn, KPL_INPUT, POJ_INPUT)
        assertEquals(original, backPojIn)
    }

    // =========================================================================
    // Case preservation
    // =========================================================================

    @Test fun case_uppercase_TAI5() = assertConvert("TAI5", "TÂI", POJ_INPUT, POJ_UNICODE)
    @Test fun case_titlecase_Tai5() = assertConvert("Tai5", "Tâi", POJ_INPUT, POJ_UNICODE)
    @Test fun case_uppercase_roundtrip() {
        val result = KonvertToPoj.convert("TÂI", POJ_UNICODE, POJ_INPUT)
        assertEquals("TAI5", result)
    }

    @Test fun case_titlecase_oo() = assertConvert("Goo7", "Gō͘", POJ_INPUT, POJ_UNICODE)

    // =========================================================================
    // Hybrid text — convertHybrid
    // =========================================================================

    @Test fun hybrid_cjk_passthrough() {
        assertEquals(
            "歐洲(Europe)。",
            KonvertToPoj.convertHybrid("歐洲(Europe)。", POJ_INPUT, POJ_UNICODE)
        )
    }

    @Test fun hybrid_digits_passthrough() {
        assertEquals(
            "四十九(7X7)工",
            KonvertToPoj.convertHybrid("四十九(7X7)工", POJ_INPUT, POJ_UNICODE)
        )
    }

    @Test fun hybrid_valid_poj_converts() {
        assertEquals(
            "歐羅巴(au-lô-pa)",
            KonvertToPoj.convertHybrid("歐羅巴(au-lo5-pa)", POJ_INPUT, POJ_UNICODE)
        )
    }

    @Test fun hybrid_mixed_cjk_poj() {
        assertEquals(
            "死了四十九(7X7)工ê供養。",
            KonvertToPoj.convertHybrid("死了四十九(7X7)工ê供養。", POJ_INPUT, POJ_UNICODE)
        )
    }

    @Test fun hybrid_poj_to_kpl() {
        assertEquals(
            "台語(tâi-gí)真好聽",
            KonvertToPoj.convertHybrid("台語(tai5-gi2)真好聽", POJ_INPUT, KPL_UNICODE)
        )
    }

    @Test fun hybrid_coastal_dialect() {
        val opts = ConvertOptions(haikau = true)
        assertEquals(
            "漳泉差(kóe(漳)/kó̤(泉))",
            KonvertToPoj.convertHybrid("漳泉差(koe2(漳)/kor2(泉))", POJ_INPUT, POJ_UNICODE, opts)
        )
    }

    @Test fun hybrid_numbered_list() {
        // Tone 1/4 have no diacritical — the tone number is dropped, not kept.
        // The list markers "1." / "2." are non-lomaji and survive untouched.
        assertEquals(
            "1. tâi-oân 2. tiong-kok",
            KonvertToPoj.convertHybrid("1. tai5-oan5 2. tiong1-kok4", POJ_INPUT, POJ_UNICODE)
        )
    }

    // =========================================================================
    // Extension functions
    // =========================================================================

    @Test fun ext_convertLomaji() {
        assertEquals("tâi-gí", "tai5-gi2".convertLomaji(POJ_INPUT, POJ_UNICODE))
    }

    @Test fun ext_convertHybridLomaji() {
        assertEquals(
            "歐(au-lô-pa)",
            "歐(au-lo5-pa)".convertHybridLomaji(POJ_INPUT, POJ_UNICODE)
        )
    }

    @Test fun ext_isValidLomajiSyllable() {
        assertEquals(true, "tai5".isValidLomajiSyllable(POJ_INPUT))
        assertEquals(false, "xyz".isValidLomajiSyllable(POJ_INPUT))
    }

    @Test fun ext_isValidLomaji() {
        assertEquals(true, "tai5-gi2".isValidLomaji(POJ_INPUT))
        assertEquals(false, "tai5-xyz".isValidLomaji(POJ_INPUT))
    }

    // =========================================================================
    // Edge cases
    // =========================================================================

    @Test fun edge_empty_string() {
        assertEquals("", KonvertToPoj.convert("", POJ_INPUT, POJ_UNICODE))
    }

    @Test fun edge_single_char() {
        assertEquals("a", KonvertToPoj.convert("a", POJ_INPUT, POJ_UNICODE))
    }

    @Test fun edge_space_separated() = assertConvert(
        "tai5 gi2", "tâi gí", POJ_INPUT, POJ_UNICODE
    )

    @Test fun edge_punctuation_preserved() = assertConvert(
        "tai5,gi2.", "tâi,gí.", POJ_INPUT, POJ_UNICODE
    )

    // =========================================================================
    // Options — traditional nasal
    // =========================================================================

    // Traditional input "annh" normalized to standard "ahnn"
    @Test fun traditionalNasal_pojInput_nnh_to_unicode() {
        val opts = ConvertOptions(traditionalNasal = true)
        assertConvert("annh8", "a\u030Dh\u207F", POJ_INPUT, POJ_UNICODE, opts)
    }

    // Traditional input "onn" normalized to standard "oonn" (o͘ⁿ)
    @Test fun traditionalNasal_pojInput_onn_to_unicode() {
        val opts = ConvertOptions(traditionalNasal = true)
        val result = KonvertToPoj.convert("onn2", POJ_INPUT, POJ_UNICODE, opts)
        // Should produce ó͘ⁿ (toned o͘ + nasal), not óⁿ (plain toned o + nasal)
        assertTrue(result.contains("\u0358"), "expected o͘ (with U+0358) in result: $result")
    }

    // Traditional input "onnh" normalized to "oohnn" (o͘hⁿ)
    @Test fun traditionalNasal_pojInput_onnh_roundtrip() {
        val opts = ConvertOptions(traditionalNasal = true)
        val unicode = KonvertToPoj.convert("onnh8", POJ_INPUT, POJ_UNICODE, opts)
        val back = KonvertToPoj.convert(unicode, POJ_UNICODE, POJ_INPUT, opts)
        assertEquals("oohnn8", back, "traditional onnh8 should round-trip as standard oohnn8")
    }

    // Without option, "annh8" produces traditional unicode "a̍ⁿh"
    @Test fun traditionalNasal_without_option_unchanged() {
        assertConvert("annh8", "a\u030D\u207Fh", POJ_INPUT, POJ_UNICODE)
    }

    // Traditional POJ_UNICODE → POJ_INPUT normalization
    @Test fun traditionalNasal_pojUnicode_to_input() {
        val opts = ConvertOptions(traditionalNasal = true)
        // "aⁿh" (traditional unicode, no tone) → "ahnn" (standard input)
        assertEquals("ahnn", KonvertToPoj.convert("a\u207Fh", POJ_UNICODE, POJ_INPUT, opts))
    }

    // Same-format normalization: POJ_INPUT → POJ_INPUT
    @Test fun traditionalNasal_sameFormat_normalizes() {
        val opts = ConvertOptions(traditionalNasal = true)
        assertEquals("ahnn8", KonvertToPoj.convert("annh8", POJ_INPUT, POJ_INPUT, opts))
    }

    // Cross-system: traditional POJ → KPL
    @Test fun traditionalNasal_pojInput_to_kplUnicode() {
        val opts = ConvertOptions(traditionalNasal = true)
        val result = KonvertToPoj.convert("annh8", POJ_INPUT, KPL_UNICODE, opts)
        // "annh8" → normalize → "ahnn8" → KPL input "ahnn8" → KPL unicode
        val back = KonvertToPoj.convert(result, KPL_UNICODE, KPL_INPUT)
        assertEquals("ahnn8", back)
    }

    // Validation accepts both forms when option is enabled
    @Test fun traditionalNasal_validation_accepts_both() {
        val opts = ConvertOptions(traditionalNasal = true)
        assertTrue(KonvertToPoj.isValidSyllable("annh8", POJ_INPUT, opts), "traditional form")
        assertTrue(KonvertToPoj.isValidSyllable("ahnn8", POJ_INPUT, opts), "standard form")
    }

    // Validation default: only traditional form accepted
    @Test fun traditionalNasal_validation_default_traditional_only() {
        assertTrue(KonvertToPoj.isValidSyllable("annh8", POJ_INPUT), "traditional form accepted by default")
        assertFalse(KonvertToPoj.isValidSyllable("ahnn8", POJ_INPUT), "standard form not accepted by default")
    }

    // =========================================================================
    // Options — Haikhau dialect
    // =========================================================================

    // Default: Haikhau syllables rejected in validation
    @Test fun haikau_validation_default_rejects() {
        assertFalse(KonvertToPoj.isValidSyllable("ur2", POJ_INPUT))
        assertFalse(KonvertToPoj.isValidSyllable("or5", POJ_INPUT))
        assertFalse(KonvertToPoj.isValidSyllable("ir2", KPL_INPUT))
        assertFalse(KonvertToPoj.isValidSyllable("er5", KPL_INPUT))
    }

    // Enabled: Haikhau syllables accepted
    @Test fun haikau_validation_enabled_accepts() {
        val opts = ConvertOptions(haikau = true)
        assertTrue(KonvertToPoj.isValidSyllable("ur2", POJ_INPUT, opts))
        assertTrue(KonvertToPoj.isValidSyllable("or5", POJ_INPUT, opts))
        assertTrue(KonvertToPoj.isValidSyllable("ir2", KPL_INPUT, opts))
        assertTrue(KonvertToPoj.isValidSyllable("er5", KPL_INPUT, opts))
    }

    // Conversion always works regardless of haikau option
    @Test fun haikau_conversion_works_regardless() {
        assertConvert("ur2", "ir2", POJ_INPUT, KPL_INPUT)
    }

    // Both options combined
    @Test fun options_combined() {
        val opts = ConvertOptions(traditionalNasal = true, haikau = true)
        assertTrue(KonvertToPoj.isValidSyllable("ur2", POJ_INPUT, opts))
        assertTrue(KonvertToPoj.isValidSyllable("ahnn8", POJ_INPUT, opts))
    }

    // =========================================================================
    // POJ Input "ou" as "oo" — default normalization
    // =========================================================================

    // Basic: ou → oo in POJ_INPUT → POJ_UNICODE
    @Test fun pojInputOu_to_pojUnicode() = assertConvert("gou7", "gō͘", POJ_INPUT, POJ_UNICODE)
    @Test fun pojInputOu_to_pojUnicode_tone2() = assertConvert("ou2", "ó͘", POJ_INPUT, POJ_UNICODE)

    // POJ_INPUT → KPL_INPUT: ou normalized to oo first
    @Test fun pojInputOu_to_kplInput() = assertConvert("gou7", "goo7", POJ_INPUT, KPL_INPUT)

    // POJ_INPUT → KPL_UNICODE
    @Test fun pojInputOu_to_kplUnicode() = assertConvert("gou7", "gōo", POJ_INPUT, KPL_UNICODE)

    // POJ_INPUT → POJ_INPUT: normalizes ou to oo
    @Test fun pojInputOu_to_pojInput() = assertConvert("gou7", "goo7", POJ_INPUT, POJ_INPUT)

    // Case preservation
    @Test fun pojInputOu_titlecase() = assertConvert("Gou7", "Gō͘", POJ_INPUT, POJ_UNICODE)
    @Test fun pojInputOu_uppercase_to_kplInput() = assertConvert("GOU7", "GOO7", POJ_INPUT, KPL_INPUT)

    // Multi-syllable sentence with ou
    @Test fun pojInputOu_sentence() = assertConvert(
        "gou2-kong7 e7-hiau2 oh8 tai5-gi2",
        "gó͘-kōng ē-hiáu o̍h tâi-gí",
        POJ_INPUT, POJ_UNICODE
    )

    // convertHybrid with ou
    @Test fun pojInputOu_hybrid() {
        assertEquals(
            "台語(gó͘-kōng)",
            KonvertToPoj.convertHybrid("台語(gou2-kong7)", POJ_INPUT, POJ_UNICODE)
        )
    }

    // Does NOT affect KPL_INPUT (ou should stay as-is for KPL)
    @Test fun kplInput_ou_unchanged() {
        assertEquals("ou7", KonvertToPoj.convert("ou7", KPL_INPUT, KPL_INPUT))
    }

    // =========================================================================
    // normalizePoj — round-trip POJ Unicode through input form
    // =========================================================================

    @Test fun normalizePoj_canonicalizesToneMarkPlacement() {
        // oē → ōe (rule 5: mark 2nd-from-right of compound vowel "oe")
        // sìan → siàn (rule 5 exception 1: when 2nd-from-right is "i", mark rightmost vowel)
        // phoé → phóe (rule 5: mark 2nd-from-right of compound vowel "oe")
        // tô͘, chhùi already canonical — pass through unchanged.
        assertEquals(
            "ōe tô͘, siàn chhùi-phóe",
            KonvertToPoj.normalizePoj("oē tô͘, sìan chhùi-phoé")
        )
    }

    @Test fun normalizePoj_appliesNfc() {
        // Decomposed "ô͘" (o + U+0302 + U+0358) should still round-trip cleanly to NFC form.
        val decomposed = "t" + "ô͘"
        val expected = "t" + "ô͘" // ô (precomposed) + U+0358
        assertEquals(expected, KonvertToPoj.normalizePoj(decomposed))
    }

    // =========================================================================
    // normalizePojHybrid — validation-aware normalization
    // =========================================================================

    @Test fun normalizePojHybrid_canonicalizesValidSyllables() {
        assertEquals(
            "ōe tô͘, siàn chhùi-phóe",
            KonvertToPoj.normalizePojHybrid("oē tô͘, sìan chhùi-phoé")
        )
    }

    @Test fun normalizePojHybrid_preservesForeignWords() {
        assertEquals("Tennessee", KonvertToPoj.normalizePojHybrid("Tennessee"))
        assertEquals("Timor", KonvertToPoj.normalizePojHybrid("Timor"))
        assertEquals("Saxhorn", KonvertToPoj.normalizePojHybrid("Saxhorn"))
    }

    @Test fun normalizePojHybrid_hanLoPreservesForeignWords() {
        val input = "歌名是「山人氣物者(yama no ninkhi mono)」"
        val result = KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(input)
        assertTrue(result.contains("ninkhi mono"), "Space lost: $result")
        assertTrue(result.contains("yama"), "yama corrupted: $result")
    }

    @Test fun normalizePojHybrid_hanLoNormalizesValidPoj() {
        val input = "我oē講台語"
        val result = KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(input)
        assertTrue(result.contains("ōe"), "oē should be normalized to ōe: $result")
    }

    // =========================================================================
    // normalizePoj — KPL contamination is converted to POJ
    // =========================================================================

    @Test fun normalizePoj_convertsKplTsToPojCh() {
        // KPL "tsi̍t" → POJ "chi̍t"; "jī" is valid in both, passes through unchanged.
        assertEquals("ū chi̍t jī", KonvertToPoj.normalizePoj("ū tsi̍t jī"))
    }

    @Test fun normalizePoj_convertsKplTshToPojChh() {
        assertEquals("chhiú", KonvertToPoj.normalizePoj("tshiú"))
    }

    @Test fun normalizePoj_convertsKplUaToPojOa() {
        // "kua" (KPL) → "koa" (POJ); "ua" is not a POJ rhyme.
        assertEquals("koa", KonvertToPoj.normalizePoj("kua"))
    }

    @Test fun normalizePoj_convertsKplIngToPojEng() {
        // "ing" (KPL) → "eng" (POJ).
        assertEquals("kéng", KonvertToPoj.normalizePoj("kíng"))
    }

    @Test fun normalizePoj_convertsKplIngToPojEng_tone1() {
        // Tone 1 (no diacritical): still detected as strictly KPL and converted.
        assertEquals("keng", KonvertToPoj.normalizePoj("king"))
    }

    @Test fun normalizePoj_foreignWordWithKplInitialIsConverted() {
        // Documented behavior: a token whose spelling is strictly-valid KPL but not POJ
        // (e.g. the surname "Tsai") will be converted by the strict-KPL pre-pass.
        // Callers needing to preserve such tokens should use normalizePojHybrid with
        // surrounding non-lomaji context, or pre-tag the tokens before normalization.
        assertEquals("Chai", KonvertToPoj.normalizePoj("Tsai"))
    }

    @Test fun normalizePoj_handlesMixedKplAndPoj() {
        // Mixed input: ts (KPL-only) → ch, ōe (POJ) stays, kua (KPL) → koa.
        assertEquals("chi̍t ōe koa", KonvertToPoj.normalizePoj("tsi̍t ōe kua"))
    }

    @Test fun normalizePojHybrid_convertsKplTsToPojCh() {
        assertEquals("ū chi̍t jī", KonvertToPoj.normalizePojHybrid("ū tsi̍t jī"))
    }

    @Test fun normalizePojHybrid_preservesForeignTsWords() {
        // "Tsai" looks KPL-ish but the rhyme "ai" is shared. Strict KPL validation
        // accepts it, strict POJ rejects it → it gets converted. We accept that
        // behavior: callers wanting to preserve foreign names should avoid
        // KPL-looking initials in their source text.
        // This test documents the boundary: pure non-lomaji passes through.
        assertEquals("Tennessee", KonvertToPoj.normalizePojHybrid("Tennessee"))
    }

    // =========================================================================
    // Confusable character normalization (dotless i, etc.)
    // =========================================================================

    @Test fun confusable_dotlessI_convertHybrid() {
        // ı (U+0131 dotless i) should be treated as regular i
        assertEquals(
            "chi̍t",
            KonvertToPoj.convertHybrid("tsı̍t", KPL_UNICODE, POJ_UNICODE),
            "dotless ı in KPL tsi̍t should convert to POJ chi̍t"
        )
    }

    @Test fun confusable_dotlessI_kplToPoj_hybrid() {
        assertEquals(
            "ū-chi̍t-ji̍t",
            KonvertToPoj.convertHybrid("ū-tsı̍t-jı̍t", KPL_UNICODE, POJ_UNICODE),
            "dotless ı in ū-tsı̍t-jı̍t should produce ū-chi̍t-ji̍t"
        )
    }

    @Test fun confusable_dotlessI_isValidSyllable() {
        assertTrue(
            KonvertToPoj.isValidText("tsı̍t", KPL_UNICODE),
            "tsı̍t with dotless i should be valid KPL"
        )
    }

    @Test fun confusable_dotlessI_convert() {
        assertEquals(
            "chi̍t",
            KonvertToPoj.convert("tsı̍t", KPL_UNICODE, POJ_UNICODE),
            "convert() should also handle dotless ı"
        )
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private fun assertConvert(input: String, expected: String, from: LomajiFormat, to: LomajiFormat, options: ConvertOptions = ConvertOptions()) {
        assertEquals(expected, KonvertToPoj.convert(input, from, to, options), "convert(\"$input\", $from, $to)")
    }
}
