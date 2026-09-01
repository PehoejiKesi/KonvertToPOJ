package tw.poj.kesi.konverttopoj.internal

import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.precomposedStringWithCanonicalMapping

@OptIn(kotlinx.cinterop.BetaInteropApi::class)
internal actual fun normalizeNfc(str: String): String =
    NSString.create(string = str).precomposedStringWithCanonicalMapping
