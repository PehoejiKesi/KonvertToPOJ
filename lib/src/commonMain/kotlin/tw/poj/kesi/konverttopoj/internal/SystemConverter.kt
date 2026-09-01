package tw.poj.kesi.konverttopoj.internal

/**
 * Converts between POJ and KPL (Kàu-io̍k Pō͘ Lô-má-jī) orthography at the input (number) level.
 *
 * Differences (Section 44 of 狗公會曉學台語):
 *   POJ → KPL: ch→ts, chh→tsh, oa→ua, oe→ue, ek→ik, eng→ing, o͘→oo, ⁿ→nn, ur→ir, or→er
 *   Tone 9: ă (breve) → a̋ (double acute)
 */
internal object SystemConverter {

    private val LOWERCASE_OA_OE = "o([aAeE])".toRegex()
    private val UPPERCASE_OA_OE = "O([aAeE])".toRegex()
    private val LOWERCASE_UA_UE = "u([aAeE])".toRegex()
    private val UPPERCASE_UA_UE = "U([aAeE])".toRegex()

    fun pojInputToKplInput(poj: String): String {
        return poj
            .replace("chh", "tsh").replace("Chh", "Tsh").replace("CHH", "TSH")
            .replace("ch", "ts").replace("Ch", "Ts").replace("CH", "TS")
            .replace(LOWERCASE_OA_OE) { "u" + it.value.substring(1) }
            .replace(UPPERCASE_OA_OE) { "U" + it.value.substring(1) }
            .replace("ek", "ik").replace("Ek", "Ik").replace("EK", "IK")
            .replace("eng", "ing").replace("Eng", "Ing").replace("ENG", "ING")
            .replace("UR", "IR").replace("Ur", "Ir").replace("ur", "ir")
            .replace("OR", "ER").replace("Or", "Er").replace("or", "er")
    }

    fun kplInputToPojInput(kpl: String): String {
        return kpl
            .replace("tsh", "chh").replace("Tsh", "Chh").replace("TSH", "CHH")
            .replace("ts", "ch").replace("Ts", "Ch").replace("TS", "CH")
            .replace(LOWERCASE_UA_UE) { "o" + it.value.substring(1) }
            .replace(UPPERCASE_UA_UE) { "O" + it.value.substring(1) }
            .replace("ik", "ek").replace("Ik", "Ek").replace("IK", "EK")
            .replace("ing", "eng").replace("Ing", "Eng").replace("ING", "ENG")
            .replace("IR", "UR").replace("Ir", "Ur").replace("ir", "ur")
            .replace("ER", "OR").replace("Er", "Or").replace("er", "or")
    }
}
