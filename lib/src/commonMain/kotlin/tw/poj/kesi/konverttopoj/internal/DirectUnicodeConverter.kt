package tw.poj.kesi.konverttopoj.internal

import tw.poj.kesi.konverttopoj.ConvertOptions

/**
 * Direct POJ Unicode ↔ KPL Unicode conversion of a single syllable.
 *
 * The tone is read off the diacritic and carried as a value rather than written into
 * the text as a digit: strip tone mark → convert bare letters between systems →
 * re-place the tone mark by the target system's rules. Callers gate on [SyllableShape]
 * first, so digits never reach here and are never read as tones.
 */
internal object DirectUnicodeConverter {

    fun pojToKpl(syllable: String, options: ConvertOptions): String {
        val (bare, tone) = ToneMarker.stripToneMark(syllable) ?: return syllable
        val isAllUpper = bare.isAllUpper() && bare.count { it.isLetter() } > 1

        var letters = ToneMarker.pojFixJiboToInput(bare, isAllUpper)
        if (options.traditionalNasal) letters = TraditionalNormalizer.normalizeSyllable(letters)
        val kpl = SystemConverter.pojInputToKplInput(letters)
        if (tone == null) return kpl
        return ToneMarker.kplInputToUnicode(kpl + tone).takeUnless { it.last().isDigit() } ?: syllable
    }

    fun kplToPoj(syllable: String): String {
        val (bare, tone) = ToneMarker.stripToneMark(syllable) ?: return syllable

        val poj = SystemConverter.kplInputToPojInput(bare)
        if (tone == null) return ToneMarker.pojInputToUnicode(poj)
        return ToneMarker.pojInputToUnicode(poj + tone).takeUnless { it.last().isDigit() } ?: syllable
    }
}
