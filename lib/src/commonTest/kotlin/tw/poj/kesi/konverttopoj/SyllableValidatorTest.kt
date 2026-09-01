package tw.poj.kesi.konverttopoj

import tw.poj.kesi.konverttopoj.LomajiFormat.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyllableValidatorTest {

    // =========================================================================
    // POJ INPUT — Valid: single vowels
    // =========================================================================

    @Test fun pojIn_valid_a() = assertValid("a", POJ_INPUT)
    @Test fun pojIn_valid_i() = assertValid("i", POJ_INPUT)
    @Test fun pojIn_valid_u() = assertValid("u", POJ_INPUT)
    @Test fun pojIn_valid_e() = assertValid("e", POJ_INPUT)
    @Test fun pojIn_valid_o() = assertValid("o", POJ_INPUT)
    @Test fun pojIn_valid_oo() = assertValid("oo", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: all tone numbers
    // =========================================================================

    @Test fun pojIn_valid_a2() = assertValid("a2", POJ_INPUT)
    @Test fun pojIn_valid_a3() = assertValid("a3", POJ_INPUT)
    @Test fun pojIn_valid_a5() = assertValid("a5", POJ_INPUT)
    @Test fun pojIn_valid_a7() = assertValid("a7", POJ_INPUT)
    @Test fun pojIn_valid_a9() = assertValid("a9", POJ_INPUT)
    @Test fun pojIn_valid_ah4() = assertValid("ah4", POJ_INPUT)
    @Test fun pojIn_valid_ah8() = assertValid("ah", POJ_INPUT) // tone 4 implicit
    @Test fun pojIn_valid_kap8() = assertValid("kap8", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: all initials
    // =========================================================================

    @Test fun pojIn_valid_pa() = assertValid("pa", POJ_INPUT)
    @Test fun pojIn_valid_pha() = assertValid("pha", POJ_INPUT)
    @Test fun pojIn_valid_ma() = assertValid("ma", POJ_INPUT)
    @Test fun pojIn_valid_ba() = assertValid("ba", POJ_INPUT)
    @Test fun pojIn_valid_ta() = assertValid("ta", POJ_INPUT)
    @Test fun pojIn_valid_tha() = assertValid("tha", POJ_INPUT)
    @Test fun pojIn_valid_na() = assertValid("na", POJ_INPUT)
    @Test fun pojIn_valid_la() = assertValid("la", POJ_INPUT)
    @Test fun pojIn_valid_ka() = assertValid("ka", POJ_INPUT)
    @Test fun pojIn_valid_kha() = assertValid("kha", POJ_INPUT)
    @Test fun pojIn_valid_ga() = assertValid("ga", POJ_INPUT)
    @Test fun pojIn_valid_nga() = assertValid("nga", POJ_INPUT)
    @Test fun pojIn_valid_ha() = assertValid("ha", POJ_INPUT)
    @Test fun pojIn_valid_sa() = assertValid("sa", POJ_INPUT)
    @Test fun pojIn_valid_ja() = assertValid("ja", POJ_INPUT)
    @Test fun pojIn_valid_cha() = assertValid("cha", POJ_INPUT)
    @Test fun pojIn_valid_chha() = assertValid("chha", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: compound vowels
    // =========================================================================

    @Test fun pojIn_valid_ai() = assertValid("ai", POJ_INPUT)
    @Test fun pojIn_valid_au() = assertValid("au", POJ_INPUT)
    @Test fun pojIn_valid_ia() = assertValid("ia", POJ_INPUT)
    @Test fun pojIn_valid_iu() = assertValid("iu", POJ_INPUT)
    @Test fun pojIn_valid_io() = assertValid("io", POJ_INPUT)
    @Test fun pojIn_valid_ioo() = assertValid("ioo", POJ_INPUT)
    @Test fun pojIn_valid_ui() = assertValid("ui", POJ_INPUT)
    @Test fun pojIn_valid_oa() = assertValid("oa", POJ_INPUT)
    @Test fun pojIn_valid_oe() = assertValid("oe", POJ_INPUT)
    @Test fun pojIn_valid_iau() = assertValid("iau", POJ_INPUT)
    @Test fun pojIn_valid_oai() = assertValid("oai", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: nasal vowels (nn = ⁿ)
    // =========================================================================

    @Test fun pojIn_valid_ann2() = assertValid("ann2", POJ_INPUT)
    @Test fun pojIn_valid_hainn2() = assertValid("hainn2", POJ_INPUT)
    @Test fun pojIn_valid_iann5() = assertValid("iann5", POJ_INPUT)
    @Test fun pojIn_valid_oonn() = assertValid("oonn", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: nasal coda finals
    // =========================================================================

    @Test fun pojIn_valid_ang() = assertValid("ang", POJ_INPUT)
    @Test fun pojIn_valid_iam5() = assertValid("iam5", POJ_INPUT)
    @Test fun pojIn_valid_iong5() = assertValid("iong5", POJ_INPUT)
    @Test fun pojIn_valid_oan5() = assertValid("oan5", POJ_INPUT)
    @Test fun pojIn_valid_eng() = assertValid("eng", POJ_INPUT)
    @Test fun pojIn_valid_seng() = assertValid("seng", POJ_INPUT)
    @Test fun pojIn_valid_ian5() = assertValid("ian5", POJ_INPUT)
    @Test fun pojIn_valid_phang() = assertValid("phang", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: checked syllables (束聲)
    // =========================================================================

    @Test fun pojIn_valid_kap() = assertValid("kap", POJ_INPUT)
    @Test fun pojIn_valid_lat8() = assertValid("lat8", POJ_INPUT)
    @Test fun pojIn_valid_chit8() = assertValid("chit8", POJ_INPUT)
    @Test fun pojIn_valid_oh() = assertValid("oh", POJ_INPUT)
    @Test fun pojIn_valid_oh8() = assertValid("oh8", POJ_INPUT)
    @Test fun pojIn_valid_iap8() = assertValid("iap8", POJ_INPUT)
    @Test fun pojIn_valid_oat8() = assertValid("oat8", POJ_INPUT)
    @Test fun pojIn_valid_oak8() = assertValid("oak8", POJ_INPUT)
    @Test fun pojIn_valid_ooh() = assertValid("ooh", POJ_INPUT)
    @Test fun pojIn_valid_ooh8() = assertValid("ooh8", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: nasal + checked
    // =========================================================================

    @Test fun pojIn_valid_annh() = assertValid("annh", POJ_INPUT)
    @Test fun pojIn_valid_annh8() = assertValid("annh8", POJ_INPUT)
    @Test fun pojIn_valid_iannh8() = assertValid("iannh8", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: standalone nasals (syllabic consonants)
    // =========================================================================

    @Test fun pojIn_valid_m() = assertValid("m", POJ_INPUT)
    @Test fun pojIn_valid_m7() = assertValid("m7", POJ_INPUT)
    @Test fun pojIn_valid_ng5() = assertValid("ng5", POJ_INPUT)
    @Test fun pojIn_valid_mh() = assertValid("mh", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Valid: coastal dialect (海口腔) — requires haikau option
    // =========================================================================

    @Test fun pojIn_valid_ur() = assertValid("ur", POJ_INPUT, ConvertOptions(haikau = true))
    @Test fun pojIn_valid_or() = assertValid("or", POJ_INPUT, ConvertOptions(haikau = true))
    @Test fun pojIn_valid_uri() = assertValid("uri", POJ_INPUT, ConvertOptions(haikau = true))
    @Test fun pojIn_valid_urt() = assertValid("urt", POJ_INPUT, ConvertOptions(haikau = true))

    // =========================================================================
    // POJ INPUT — Valid: tone 9
    // =========================================================================

    @Test fun pojIn_valid_ka9() = assertValid("ka9", POJ_INPUT)
    @Test fun pojIn_valid_ah9() = assertValid("ah", POJ_INPUT) // checked tone implicit

    // =========================================================================
    // POJ INPUT — Valid: case variants
    // =========================================================================

    @Test fun pojIn_valid_Tai5() = assertValid("Tai5", POJ_INPUT)
    @Test fun pojIn_valid_TAI5() = assertValid("TAI5", POJ_INPUT)
    @Test fun pojIn_valid_Ka() = assertValid("Ka", POJ_INPUT)
    @Test fun pojIn_valid_KAP8() = assertValid("KAP8", POJ_INPUT)

    // =========================================================================
    // POJ INPUT — Invalid
    // =========================================================================

    // Invalid vowel sequences
    @Test fun pojIn_invalid_eiou() = assertInvalid("eiou", POJ_INPUT)
    @Test fun pojIn_invalid_aiu() = assertInvalid("aiu", POJ_INPUT)
    @Test fun pojIn_invalid_ooi() = assertInvalid("ooi", POJ_INPUT)
    @Test fun pojIn_invalid_eai() = assertInvalid("eai", POJ_INPUT)
    @Test fun pojIn_invalid_uoa() = assertInvalid("uoa", POJ_INPUT)
    @Test fun pojIn_invalid_aei() = assertInvalid("aei", POJ_INPUT)

    // Invalid tone number
    @Test fun pojIn_invalid_a6() = assertInvalid("a6", POJ_INPUT)
    @Test fun pojIn_invalid_a0() = assertInvalid("a0", POJ_INPUT)

    // Tone-checked consistency: tone 8 on non-checked syllable
    @Test fun pojIn_invalid_ka8() = assertInvalid("ka8", POJ_INPUT)
    @Test fun pojIn_invalid_tai8() = assertInvalid("tai8", POJ_INPUT)

    // Tone-checked consistency: non-checked tone on checked syllable
    @Test fun pojIn_invalid_kap2() = assertInvalid("kap2", POJ_INPUT)
    @Test fun pojIn_invalid_kap5() = assertInvalid("kap5", POJ_INPUT)
    @Test fun pojIn_invalid_ah2() = assertInvalid("ah2", POJ_INPUT)
    @Test fun pojIn_invalid_ok5() = assertInvalid("ok5", POJ_INPUT)
    @Test fun pojIn_invalid_at7() = assertInvalid("at7", POJ_INPUT)

    // Invalid initials
    @Test fun pojIn_invalid_xa() = assertInvalid("xa", POJ_INPUT)
    @Test fun pojIn_invalid_qa() = assertInvalid("qa", POJ_INPUT)

    // KPL-specific initials must NOT validate as POJ
    @Test fun pojIn_invalid_tsit8() = assertInvalid("tsit8", POJ_INPUT)
    @Test fun pojIn_invalid_tshiu2() = assertInvalid("tshiu2", POJ_INPUT)
    @Test fun pojIn_invalid_tsa() = assertInvalid("tsa", POJ_INPUT)
    @Test fun pojIn_invalid_tsha() = assertInvalid("tsha", POJ_INPUT)

    // Blank/empty
    @Test fun pojIn_invalid_empty() = assertInvalid("", POJ_INPUT)
    @Test fun pojIn_invalid_space() = assertInvalid(" ", POJ_INPUT)

    // =========================================================================
    // KPL INPUT — Valid
    // =========================================================================

    @Test fun kplIn_valid_tai5() = assertValid("tai5", KPL_INPUT)
    @Test fun kplIn_valid_tsit8() = assertValid("tsit8", KPL_INPUT)
    @Test fun kplIn_valid_tshiu2() = assertValid("tshiu2", KPL_INPUT)
    @Test fun kplIn_valid_uan5() = assertValid("uan5", KPL_INPUT)
    @Test fun kplIn_valid_ue7() = assertValid("ue7", KPL_INPUT)
    @Test fun kplIn_valid_ik8() = assertValid("ik8", KPL_INPUT)
    @Test fun kplIn_valid_sing() = assertValid("sing", KPL_INPUT)
    @Test fun kplIn_valid_uai2() = assertValid("uai2", KPL_INPUT)
    @Test fun kplIn_valid_oo7() = assertValid("oo7", KPL_INPUT)
    @Test fun kplIn_valid_a9() = assertValid("a9", KPL_INPUT)
    @Test fun kplIn_valid_ir() = assertValid("ir", KPL_INPUT, ConvertOptions(haikau = true))
    @Test fun kplIn_valid_er() = assertValid("er", KPL_INPUT, ConvertOptions(haikau = true))

    // All KPL initials
    @Test fun kplIn_valid_pa() = assertValid("pa", KPL_INPUT)
    @Test fun kplIn_valid_pha() = assertValid("pha", KPL_INPUT)
    @Test fun kplIn_valid_ma() = assertValid("ma", KPL_INPUT)
    @Test fun kplIn_valid_ba() = assertValid("ba", KPL_INPUT)
    @Test fun kplIn_valid_ta() = assertValid("ta", KPL_INPUT)
    @Test fun kplIn_valid_tha() = assertValid("tha", KPL_INPUT)
    @Test fun kplIn_valid_na() = assertValid("na", KPL_INPUT)
    @Test fun kplIn_valid_la() = assertValid("la", KPL_INPUT)
    @Test fun kplIn_valid_ka() = assertValid("ka", KPL_INPUT)
    @Test fun kplIn_valid_kha() = assertValid("kha", KPL_INPUT)
    @Test fun kplIn_valid_ga() = assertValid("ga", KPL_INPUT)
    @Test fun kplIn_valid_nga() = assertValid("nga", KPL_INPUT)
    @Test fun kplIn_valid_ha() = assertValid("ha", KPL_INPUT)
    @Test fun kplIn_valid_tsa() = assertValid("tsa", KPL_INPUT)
    @Test fun kplIn_valid_tsha() = assertValid("tsha", KPL_INPUT)
    @Test fun kplIn_valid_sa() = assertValid("sa", KPL_INPUT)
    @Test fun kplIn_valid_ja() = assertValid("ja", KPL_INPUT)

    // KPL-specific rhymes
    @Test fun kplIn_valid_ua5() = assertValid("ua5", KPL_INPUT)
    @Test fun kplIn_valid_ue5() = assertValid("ue5", KPL_INPUT)
    @Test fun kplIn_valid_ik() = assertValid("ik", KPL_INPUT)
    @Test fun kplIn_valid_ing7() = assertValid("ing7", KPL_INPUT)
    @Test fun kplIn_valid_uainn5() = assertValid("uainn5", KPL_INPUT)
    @Test fun kplIn_valid_uat8() = assertValid("uat8", KPL_INPUT)
    @Test fun kplIn_valid_uak8() = assertValid("uak8", KPL_INPUT)

    // =========================================================================
    // KPL INPUT — Invalid: POJ forms should fail
    // =========================================================================

    @Test fun kplIn_invalid_chit8() = assertInvalid("chit8", KPL_INPUT)
    @Test fun kplIn_invalid_oan5() = assertInvalid("oan5", KPL_INPUT)
    @Test fun kplIn_invalid_oe7() = assertInvalid("oe7", KPL_INPUT)
    @Test fun kplIn_invalid_ek8() = assertInvalid("ek8", KPL_INPUT)
    @Test fun kplIn_invalid_seng() = assertInvalid("seng", KPL_INPUT)

    // =========================================================================
    // POJ UNICODE — Valid
    // =========================================================================

    // Single vowels with tones
    @Test fun pojUni_valid_a2() = assertValid("á", POJ_UNICODE)
    @Test fun pojUni_valid_a3() = assertValid("à", POJ_UNICODE)
    @Test fun pojUni_valid_a5() = assertValid("â", POJ_UNICODE)
    @Test fun pojUni_valid_a7() = assertValid("ā", POJ_UNICODE)
    @Test fun pojUni_valid_a9() = assertValid("ă", POJ_UNICODE)
    @Test fun pojUni_valid_e5() = assertValid("ê", POJ_UNICODE)

    // Tone 1 / tone 4 (no diacritical)
    @Test fun pojUni_valid_ka() = assertValid("ka", POJ_UNICODE)
    @Test fun pojUni_valid_kap() = assertValid("kap", POJ_UNICODE)
    @Test fun pojUni_valid_ah() = assertValid("ah", POJ_UNICODE)

    // With initials
    @Test fun pojUni_valid_tai() = assertValid("tâi", POJ_UNICODE)
    @Test fun pojUni_valid_gi() = assertValid("gí", POJ_UNICODE)
    @Test fun pojUni_valid_si7() = assertValid("sī", POJ_UNICODE)
    @Test fun pojUni_valid_ho2() = assertValid("hó", POJ_UNICODE)
    @Test fun pojUni_valid_phang7() = assertValid("phāng", POJ_UNICODE)
    @Test fun pojUni_valid_chhau3() = assertValid("chhàu", POJ_UNICODE)

    // Compound vowels
    @Test fun pojUni_valid_iau5() = assertValid("iâu", POJ_UNICODE)
    @Test fun pojUni_valid_ia2() = assertValid("iá", POJ_UNICODE)
    @Test fun pojUni_valid_io2() = assertValid("ió", POJ_UNICODE)
    @Test fun pojUni_valid_iu2() = assertValid("iú", POJ_UNICODE)
    @Test fun pojUni_valid_oai2() = assertValid("oái", POJ_UNICODE)

    // o͘ vowel
    @Test fun pojUni_valid_goo7() = assertValid("gō͘", POJ_UNICODE)

    // Nasals
    @Test fun pojUni_valid_hainn2() = assertValid("háiⁿ", POJ_UNICODE)
    @Test fun pojUni_valid_m7() = assertValid("m̄", POJ_UNICODE)
    @Test fun pojUni_valid_ng5() = assertValid("n̂g", POJ_UNICODE)

    // Checked syllables
    @Test fun pojUni_valid_kap8() = assertValid("ka̍p", POJ_UNICODE)
    @Test fun pojUni_valid_lat8() = assertValid("la̍t", POJ_UNICODE)
    @Test fun pojUni_valid_chit8() = assertValid("chi̍t", POJ_UNICODE)

    // Nasal coda finals
    @Test fun pojUni_valid_ong7() = assertValid("ōng", POJ_UNICODE)
    @Test fun pojUni_valid_seng7() = assertValid("sēng", POJ_UNICODE)
    @Test fun pojUni_valid_iong5() = assertValid("iông", POJ_UNICODE)

    // Case
    @Test fun pojUni_valid_Tai() = assertValid("Tâi", POJ_UNICODE)
    @Test fun pojUni_valid_SI() = assertValid("SĪ", POJ_UNICODE)

    // =========================================================================
    // POJ UNICODE — Invalid
    // =========================================================================

    @Test fun pojUni_invalid_xa() = assertInvalid("xá", POJ_UNICODE)
    @Test fun pojUni_invalid_kap5() = assertInvalid("kâp", POJ_UNICODE) // tone 5 on checked
    @Test fun pojUni_invalid_eai() = assertInvalid("êai", POJ_UNICODE)

    // KPL-specific initials must NOT validate as POJ
    @Test fun pojUni_invalid_tsit8() = assertInvalid("tsi̍t", POJ_UNICODE)
    @Test fun pojUni_invalid_tshiu2() = assertInvalid("tshiú", POJ_UNICODE)
    @Test fun pojUni_invalid_tsa() = assertInvalid("tsa", POJ_UNICODE)

    // =========================================================================
    // KPL UNICODE — Valid
    // =========================================================================

    @Test fun kplUni_valid_tai5() = assertValid("tâi", KPL_UNICODE)
    @Test fun kplUni_valid_gi2() = assertValid("gí", KPL_UNICODE)
    @Test fun kplUni_valid_tsit8() = assertValid("tsi̍t", KPL_UNICODE)
    @Test fun kplUni_valid_tiu2() = assertValid("tiú", KPL_UNICODE)
    @Test fun kplUni_valid_ng5() = assertValid("n̂g", KPL_UNICODE)
    @Test fun kplUni_valid_oo7() = assertValid("ōo", KPL_UNICODE)
    @Test fun kplUni_valid_a9() = assertValid("a\u030B", KPL_UNICODE)
    @Test fun kplUni_valid_uan5() = assertValid("uân", KPL_UNICODE)
    @Test fun kplUni_valid_ue7() = assertValid("uē", KPL_UNICODE)
    @Test fun kplUni_valid_ka() = assertValid("ka", KPL_UNICODE)
    @Test fun kplUni_valid_si7() = assertValid("sī", KPL_UNICODE)
    @Test fun kplUni_valid_ing7() = assertValid("īng", KPL_UNICODE)

    // =========================================================================
    // KPL UNICODE — Invalid: POJ forms
    // =========================================================================

    @Test fun kplUni_invalid_chit8() = assertInvalid("chi̍t", KPL_UNICODE)
    @Test fun kplUni_invalid_oe7() = assertInvalid("ōe", KPL_UNICODE)
    @Test fun kplUni_invalid_seng7() = assertInvalid("sēng", KPL_UNICODE)

    // =========================================================================
    // Multi-syllable text validation
    // =========================================================================

    // POJ Input
    @Test fun text_pojIn_valid_tai5gi2() {
        assertTrue(KonvertToPoj.isValidText("tai5-gi2", POJ_INPUT))
    }
    @Test fun text_pojIn_valid_goo2kong7() {
        assertTrue(KonvertToPoj.isValidText("goo2-kong7", POJ_INPUT))
    }
    @Test fun text_pojIn_valid_spaces() {
        assertTrue(KonvertToPoj.isValidText("tai5 gi2", POJ_INPUT))
    }
    @Test fun text_pojIn_invalid_badSyllable() {
        assertFalse(KonvertToPoj.isValidText("tai5-xyz", POJ_INPUT))
    }

    // POJ Unicode
    @Test fun text_pojUni_valid_tai5gi2() {
        assertTrue(KonvertToPoj.isValidText("Tâi-gí", POJ_UNICODE))
    }
    @Test fun text_pojUni_valid_goo2kong7() {
        assertTrue(KonvertToPoj.isValidText("gó͘-kōng", POJ_UNICODE))
    }
    @Test fun text_pojUni_invalid_badSyllable() {
        assertFalse(KonvertToPoj.isValidText("tâi-xyz", POJ_UNICODE))
    }

    // KPL
    @Test fun text_kplUni_valid() {
        assertTrue(KonvertToPoj.isValidText("Tâi-gí", KPL_UNICODE))
    }
    @Test fun text_kplIn_valid() {
        assertTrue(KonvertToPoj.isValidText("tsit8-e5", KPL_INPUT))
    }

    // =========================================================================
    // Extension functions
    // =========================================================================

    @Test fun ext_isValidLomajiSyllable() {
        assertTrue("tai5".isValidLomajiSyllable(POJ_INPUT))
        assertFalse("xyz".isValidLomajiSyllable(POJ_INPUT))
    }

    @Test fun ext_isValidLomaji() {
        assertTrue("tai5-gi2".isValidLomaji(POJ_INPUT))
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun assertValid(syllable: String, format: LomajiFormat, options: ConvertOptions = ConvertOptions()) {
        assertTrue(
            KonvertToPoj.isValidSyllable(syllable, format, options),
            "Expected '$syllable' to be valid in $format"
        )
    }

    private fun assertInvalid(syllable: String, format: LomajiFormat, options: ConvertOptions = ConvertOptions()) {
        assertFalse(
            KonvertToPoj.isValidSyllable(syllable, format, options),
            "Expected '$syllable' to be invalid in $format"
        )
    }
}
