@file:OptIn(ExperimentalJsExport::class)

package tw.poj.kesi.konverttopoj

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("KonvertToPoj")
object KonvertToPojJs {

    val POJ_INPUT = "POJ_INPUT"
    val POJ_UNICODE = "POJ_UNICODE"
    val KPL_INPUT = "KPL_INPUT"
    val KPL_UNICODE = "KPL_UNICODE"

    fun convert(text: String, from: String, to: String, traditionalNasal: Boolean = false, haikau: Boolean = false): String =
        KonvertToPoj.convert(text, LomajiFormat.valueOf(from), LomajiFormat.valueOf(to), ConvertOptions(traditionalNasal, haikau))

    fun convertHybrid(text: String, from: String, to: String, traditionalNasal: Boolean = false, haikau: Boolean = false): String =
        KonvertToPoj.convertHybrid(text, LomajiFormat.valueOf(from), LomajiFormat.valueOf(to), ConvertOptions(traditionalNasal, haikau))

    fun isValidSyllable(syllable: String, format: String, traditionalNasal: Boolean = false, haikau: Boolean = false): Boolean =
        KonvertToPoj.isValidSyllable(syllable, LomajiFormat.valueOf(format), ConvertOptions(traditionalNasal, haikau))

    fun isValidText(text: String, format: String, traditionalNasal: Boolean = false, haikau: Boolean = false): Boolean =
        KonvertToPoj.isValidText(text, LomajiFormat.valueOf(format), ConvertOptions(traditionalNasal, haikau))

    fun normalizePoj(text: String, traditionalNasal: Boolean = false, haikau: Boolean = false): String =
        KonvertToPoj.normalizePoj(text, ConvertOptions(traditionalNasal, haikau))

    fun normalizePojHybrid(text: String, traditionalNasal: Boolean = false, haikau: Boolean = false): String =
        KonvertToPoj.normalizePojHybrid(text, ConvertOptions(traditionalNasal, haikau))

    fun normalizePojHanLoForceUsingFullwidthPunctuation(
        text: String,
        traditionalNasal: Boolean = false,
        haikau: Boolean = false,
        aggressiveWhitespace: Boolean = true
    ): String =
        KonvertToPoj.normalizePojHanLoForceUsingFullwidthPunctuation(
            text,
            ConvertOptions(traditionalNasal, haikau, aggressiveWhitespace)
        )

    fun normalizePojHanLoAutoChoanLoOrHanLoPunctuation(
        text: String,
        traditionalNasal: Boolean = false,
        haikau: Boolean = false,
        aggressiveWhitespace: Boolean = true
    ): String =
        KonvertToPoj.normalizePojHanLoAutoChoanLoOrHanLoPunctuation(
            text,
            ConvertOptions(traditionalNasal, haikau, aggressiveWhitespace)
        )

    fun normalizePojHybridHanLoForceUsingFullwidthPunctuation(
        text: String,
        traditionalNasal: Boolean = false,
        haikau: Boolean = false,
        aggressiveWhitespace: Boolean = true
    ): String =
        KonvertToPoj.normalizePojHybridHanLoForceUsingFullwidthPunctuation(
            text,
            ConvertOptions(traditionalNasal, haikau, aggressiveWhitespace)
        )

    fun normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
        text: String,
        traditionalNasal: Boolean = false,
        haikau: Boolean = false,
        aggressiveWhitespace: Boolean = true
    ): String =
        KonvertToPoj.normalizePojHybridHanLoAutoChoanLoOrHanLoPunctuation(
            text,
            ConvertOptions(traditionalNasal, haikau, aggressiveWhitespace)
        )
}
