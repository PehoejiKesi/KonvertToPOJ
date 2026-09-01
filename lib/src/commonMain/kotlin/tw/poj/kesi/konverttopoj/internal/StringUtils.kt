package tw.poj.kesi.konverttopoj.internal

internal fun String.isAllUpper(): Boolean = this.uppercase() == this

private val CONFUSABLE_MAP = mapOf(
    'ı' to 'i', // ı Latin small letter dotless i
    'İ' to 'I', // İ Latin capital letter I with dot above
)

internal fun normalizeConfusables(str: String): String {
    if (str.isEmpty()) return str
    val sb = StringBuilder(str.length)
    for (ch in str) {
        sb.append(CONFUSABLE_MAP[ch] ?: ch)
    }
    return sb.toString()
}
