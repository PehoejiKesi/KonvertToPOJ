package tw.poj.kesi.konverttopoj.internal

import java.text.Normalizer

internal actual fun normalizeNfc(str: String): String = Normalizer.normalize(str, Normalizer.Form.NFC)
