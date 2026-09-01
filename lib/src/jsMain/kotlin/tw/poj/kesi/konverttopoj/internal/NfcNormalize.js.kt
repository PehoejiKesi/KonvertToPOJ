package tw.poj.kesi.konverttopoj.internal

internal actual fun normalizeNfc(str: String): String =
    str.asDynamic().normalize("NFC") as String
