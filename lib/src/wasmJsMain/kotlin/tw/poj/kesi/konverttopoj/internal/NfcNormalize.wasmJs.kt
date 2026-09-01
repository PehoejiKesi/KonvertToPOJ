package tw.poj.kesi.konverttopoj.internal

internal actual fun normalizeNfc(str: String): String =
    jsNormalizeNfc(str)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsNormalizeNfc(str: String): String =
    js("str.normalize('NFC')")
