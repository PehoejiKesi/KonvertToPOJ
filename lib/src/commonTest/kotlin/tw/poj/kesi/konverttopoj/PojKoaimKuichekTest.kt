package tw.poj.kesi.konverttopoj

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Golden test generated from the syllable table (Im-chat Pio\u0301) in
 * POJ_KOAIM_KUICHEK.md \u2014 the canonical statement of the POJ tone-placement
 * rules. Every rhyme in that table is converted POJ_INPUT \u2192 POJ_UNICODE and
 * compared against the table's own tone-marked form.
 */
class PojKoaimKuichekTest {

    private val table = listOf(
        "a2" to "\u00E1",
        "i2" to "\u00ED",
        "u2" to "\u00FA",
        "oo2" to "\u00F3\u0358",
        "e2" to "\u00E9",
        "o2" to "\u00F3",
        "am2" to "\u00E1m",
        "an2" to "\u00E1n",
        "ang2" to "\u00E1ng",
        "im2" to "\u00EDm",
        "in2" to "\u00EDn",
        "eng2" to "\u00E9ng",
        "om2" to "\u00F3m",
        "ong2" to "\u00F3ng",
        "ai2" to "\u00E1i",
        "au2" to "\u00E1u",
        "ia2" to "i\u00E1",
        "io2" to "i\u00F3",
        "iu2" to "i\u00FA",
        "ui2" to "\u00FAi",
        "oa2" to "\u00F3a",
        "oe2" to "\u00F3e",
        "iau2" to "i\u00E1u",
        "oai2" to "o\u00E1i",
        "iam2" to "i\u00E1m",
        "ian2" to "i\u00E1n",
        "iang2" to "i\u00E1ng",
        "iong2" to "i\u00F3ng",
        "oan2" to "o\u00E1n",
        "oang2" to "o\u00E1ng",
        "ap8" to "a\u030Dp",
        "at8" to "a\u030Dt",
        "ak8" to "a\u030Dk",
        "ah8" to "a\u030Dh",
        "ip8" to "i\u030Dp",
        "it8" to "i\u030Dt",
        "ih8" to "i\u030Dh",
        "ut8" to "u\u030Dt",
        "uh8" to "u\u030Dh",
        "op8" to "o\u030Dp",
        "ok8" to "o\u030Dk",
        "ooh8" to "o\u030D\u0358h",
        "ek8" to "e\u030Dk",
        "eh8" to "e\u030Dh",
        "oh8" to "o\u030Dh",
        "aih8" to "a\u030Dih",
        "auh8" to "a\u030Duh",
        "iah8" to "ia\u030Dh",
        "ioh8" to "io\u030Dh",
        "iuh8" to "iu\u030Dh",
        "uih8" to "u\u030Dih",
        "oah8" to "oa\u030Dh",
        "oeh8" to "oe\u030Dh",
        "iauh8" to "ia\u030Duh",
        "oaih8" to "oa\u030Dih",
        "iap8" to "ia\u030Dp",
        "iat8" to "ia\u030Dt",
        "iak8" to "ia\u030Dk",
        "iok8" to "io\u030Dk",
        "oat8" to "oa\u030Dt",
        "m2" to "\u1E3F",
        "ng2" to "\u0144g",
        "mh8" to "m\u030Dh",
        "ngh8" to "n\u030Dgh",
    )

    @Test
    fun everySyllableInTheTableMarksTheRightLetter() {
        val failures = mutableListOf<String>()
        for ((input, expected) in table) {
            val actual = KonvertToPoj.convert(input, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE)
            if (actual != expected) failures += "$input: expected $expected, got $actual"
        }
        assertEquals(emptyList(), failures.toList(), "tone placement disagrees with POJ_KOAIM_KUICHEK.md")
    }

    /**
     * Tones 1 and 4 carry no diacritical, so the tone number is simply dropped.
     */
    @Test
    fun tone1And4DropTheToneNumber() {
        val failures = mutableListOf<String>()
        for ((input, _) in table) {
            val plain = input.dropLast(1)
            val tone = if (input.last() == '8') "4" else "1"
            val expected = plain.replace("oo", "o\u0358")
            val actual = KonvertToPoj.convert(plain + tone, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE)
            if (actual != expected) failures += "$plain$tone: expected $expected, got $actual"
        }
        assertEquals(emptyList(), failures.toList(), "tone 1/4 must not leave the tone number behind")
    }

    /**
     * A bare numeral has nothing that can carry a tone mark, so its trailing digit
     * is not a tone number and must survive untouched \u2014 "2024" is not "202" + tone 4.
     */
    @Test
    fun bareNumeralsAreNotTreatedAsTonedSyllables() {
        val numerals = listOf("1", "4", "24", "2024", "1894", "1895", "0", "1911", "404")
        val failures = mutableListOf<String>()
        for (n in numerals) {
            for (format in listOf(LomajiFormat.POJ_UNICODE, LomajiFormat.KPL_UNICODE)) {
                val actual = KonvertToPoj.convert(n, LomajiFormat.POJ_INPUT, format)
                if (actual != n) failures += "$n \u2192 $format: got $actual"
            }
        }
        assertEquals(emptyList(), failures.toList(), "bare numerals must pass through unchanged")
    }

    /** The same, reached through the Han-Lo normalization entry points. */
    @Test
    fun numeralsSurviveNormalization() {
        assertEquals("T\u00e2i-o\u00e2n 2024 n\u00ee", KonvertToPoj.normalizePoj("T\u00e2i-o\u00e2n 2024 n\u00ee"))
        assertEquals("T\u00e2i-o\u00e2n 1894 n\u00ee", KonvertToPoj.normalizePoj("T\u00e2i-o\u00e2n 1894 n\u00ee"))
        assertEquals("1. t\u00e2i-o\u00e2n 2. tiong-kok",
            KonvertToPoj.convertHybrid("1. tai5-oan5 2. tiong1-kok4", LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE))
    }

    /**
     * "Phi\u0304\u207F-im ki\u0300-ho\u0304 \u201C\u207F\u201D bo\u0302 s\u01F9g" \u2014 the nasal marker counts for nothing, so
     * adding it must never move the tone mark.
     */
    @Test
    fun nasalMarkerIsTransparentToPlacement() {
        val nasalRhymes = listOf(
            "ann", "inn", "unn", "oonn", "enn", "onn",
            "ainn", "aunn", "iann", "iunn", "ionn", "uinn", "oann", "oenn", "iaunn", "oainn",
            "annh", "innh", "unnh", "oonnh", "ennh", "onnh",
            "ainnh", "aunnh", "iannh", "iunnh", "uinnh", "oannh", "oainnh", "iaunnh", "oennh"
        )
        val failures = mutableListOf<String>()
        for (rhyme in nasalRhymes) {
            val bare = rhyme.replace("nn", "")
            val tone = if (rhyme.endsWith("h")) "8" else "5"
            val nasal = KonvertToPoj.convert(rhyme + tone, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE)
            val plain = KonvertToPoj.convert(bare + tone, LomajiFormat.POJ_INPUT, LomajiFormat.POJ_UNICODE)
            val stripped = nasal.replace("\u207F", "")
            if (stripped != plain) failures += "$rhyme$tone: $nasal strips to $stripped, but $bare$tone is $plain"
        }
        assertEquals(emptyList(), failures.toList(), "\u207F must be transparent to tone placement")
    }

    @Test
    fun everySyllableRoundTripsBackToInput() {
        val failures = mutableListOf<String>()
        for ((input, expected) in table) {
            val back = KonvertToPoj.convert(expected, LomajiFormat.POJ_UNICODE, LomajiFormat.POJ_INPUT)
            if (back != input) failures += "$expected: expected $input, got $back"
        }
        assertEquals(emptyList(), failures.toList(), "unicode \u2192 input round-trip disagrees")
    }
}
