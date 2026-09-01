package tw.poj.kesi.konverttopoj.internal

internal object PojToneMap {
    // numberToUnicode: e.g. "a2" -> "á", "Oo5" -> "Ô͘"
    val numberToUnicode: Map<String, String>
    // unicodeToNumber: e.g. "á" -> "a2", "Ô͘" -> "Oo5"
    // IMPORTANT: This is a LinkedHashMap — iteration order matters.
    // When iterating u2n for tone lookup (e.g., in pojUnicodeToInput or SyllableValidator),
    // plain vowel entries (like "ó" → "o2") are checked BEFORE their special variants
    // (like "ó͘" → "oo2"). This works because post-processing in pojFixJiboToInput()
    // separately handles o͘→oo, ṳ→ur, o̤→or conversion after tone stripping.
    // Do not reorder entries without updating the post-processing logic.
    val unicodeToNumber: Map<String, String>

    init {
        val u2n = LinkedHashMap<String, String>()

        // A
        u2n["Á"] = "A2"; u2n["À"] = "A3"; u2n["Â"] = "A5"
        u2n["Ā"] = "A7"; u2n["A\u030D"] = "A8"; u2n["Ă"] = "A9"
        u2n["á"] = "a2"; u2n["à"] = "a3"; u2n["â"] = "a5"
        u2n["ā"] = "a7"; u2n["a\u030D"] = "a8"; u2n["ă"] = "a9"

        // I
        u2n["Í"] = "I2"; u2n["Ì"] = "I3"; u2n["Î"] = "I5"
        u2n["Ī"] = "I7"; u2n["I\u030D"] = "I8"; u2n["Ĭ"] = "I9"
        u2n["í"] = "i2"; u2n["ì"] = "i3"; u2n["î"] = "i5"
        u2n["ī"] = "i7"; u2n["i\u030D"] = "i8"; u2n["ĭ"] = "i9"

        // U
        u2n["Ú"] = "U2"; u2n["Ù"] = "U3"; u2n["Û"] = "U5"
        u2n["Ū"] = "U7"; u2n["U\u030D"] = "U8"; u2n["Ŭ"] = "U9"
        u2n["ú"] = "u2"; u2n["ù"] = "u3"; u2n["û"] = "u5"
        u2n["ū"] = "u7"; u2n["u\u030D"] = "u8"; u2n["ŭ"] = "u9"

        // E
        u2n["É"] = "E2"; u2n["È"] = "E3"; u2n["Ê"] = "E5"
        u2n["Ē"] = "E7"; u2n["E\u030D"] = "E8"; u2n["Ĕ"] = "E9"
        u2n["é"] = "e2"; u2n["è"] = "e3"; u2n["ê"] = "e5"
        u2n["ē"] = "e7"; u2n["e\u030D"] = "e8"; u2n["ĕ"] = "e9"

        // O
        u2n["Ó"] = "O2"; u2n["Ò"] = "O3"; u2n["Ô"] = "O5"
        u2n["Ō"] = "O7"; u2n["O\u030D"] = "O8"; u2n["Ŏ"] = "O9"
        u2n["ó"] = "o2"; u2n["ò"] = "o3"; u2n["ô"] = "o5"
        u2n["ō"] = "o7"; u2n["o\u030D"] = "o8"; u2n["ŏ"] = "o9"

        // O͘ (o + combining dot above right U+0358) — uppercase Oo
        u2n[normalizeNfc("Ó\u0358")] = "Oo2"; u2n[normalizeNfc("Ò\u0358")] = "Oo3"
        u2n[normalizeNfc("Ô\u0358")] = "Oo5"; u2n[normalizeNfc("Ō\u0358")] = "Oo7"
        u2n[normalizeNfc("O\u030D\u0358")] = "Oo8"; u2n[normalizeNfc("Ŏ\u0358")] = "Oo9"
        // o͘ — lowercase oo
        u2n[normalizeNfc("ó\u0358")] = "oo2"; u2n[normalizeNfc("ò\u0358")] = "oo3"
        u2n[normalizeNfc("ô\u0358")] = "oo5"; u2n[normalizeNfc("ō\u0358")] = "oo7"
        u2n[normalizeNfc("o\u030D\u0358")] = "oo8"; u2n[normalizeNfc("ŏ\u0358")] = "oo9"

        // Ṳ (u + combining low line, for Hái-kháu-khiuⁿ ur)
        u2n["Ṳ\u0301"] = "Ur2"; u2n["Ṳ\u0300"] = "Ur3"
        u2n["Ṳ\u0302"] = "Ur5"; u2n["Ṳ\u0304"] = "Ur7"
        u2n["Ṳ\u030D"] = "Ur8"; u2n["Ṳ\u0306"] = "Ur9"
        u2n["ṳ\u0301"] = "ur2"; u2n["ṳ\u0300"] = "ur3"
        u2n["ṳ\u0302"] = "ur5"; u2n["ṳ\u0304"] = "ur7"
        u2n["ṳ\u030D"] = "ur8"; u2n["ṳ\u0306"] = "ur9"

        // O̤ (o + combining diaeresis below, for Hái-kháu-khiuⁿ or)
        u2n["O\u0324\u0301"] = "Or2"; u2n["O\u0324\u0300"] = "Or3"
        u2n["O\u0324\u0302"] = "Or5"; u2n["O\u0324\u0304"] = "Or7"
        // Double U+030D for or8 — matches lib-pehoeji/ChhoeTaigi convention
        u2n[normalizeNfc("O\u0324\u030D\u030D")] = "Or8"; u2n["O\u0324\u0306"] = "Or9"
        u2n["o\u0324\u0301"] = "or2"; u2n["o\u0324\u0300"] = "or3"
        u2n["o\u0324\u0302"] = "or5"; u2n["o\u0324\u0304"] = "or7"
        u2n[normalizeNfc("o\u0324\u030D\u030D")] = "or8"; u2n["o\u0324\u0306"] = "or9"

        // M
        u2n["Ḿ"] = "M2"; u2n["M\u0300"] = "M3"; u2n["M\u0302"] = "M5"
        u2n["M\u0304"] = "M7"; u2n["M\u030D"] = "M8"; u2n["M\u0306"] = "M9"
        u2n["ḿ"] = "m2"; u2n["m\u0300"] = "m3"; u2n["m\u0302"] = "m5"
        u2n["m\u0304"] = "m7"; u2n["m\u030D"] = "m8"; u2n["m\u0306"] = "m9"

        // N
        u2n["Ń"] = "N2"; u2n["Ǹ"] = "N3"; u2n["N\u0302"] = "N5"
        u2n["N\u0304"] = "N7"; u2n["N\u030D"] = "N8"; u2n["N\u030B"] = "N9"
        u2n["ń"] = "n2"; u2n["ǹ"] = "n3"; u2n["n\u0302"] = "n5"
        u2n["n\u0304"] = "n7"; u2n["n\u030D"] = "n8"; u2n["n\u030B"] = "n9"

        // Ng (tone mark on N, g appended)
        u2n["Ńg"] = "Ng2"; u2n["Ǹg"] = "Ng3"; u2n["N\u0302g"] = "Ng5"
        u2n["N\u0304g"] = "Ng7"; u2n["N\u030Dg"] = "Ng8"; u2n["N\u0306g"] = "Ng9"
        // NG
        u2n["ŃG"] = "NG2"; u2n["ǸG"] = "NG3"; u2n["N\u0302G"] = "NG5"
        u2n["N\u0304G"] = "NG7"; u2n["N\u030DG"] = "NG8"; u2n["N\u0306G"] = "NG9"
        // ng
        u2n["ńg"] = "ng2"; u2n["ǹg"] = "ng3"; u2n["n\u0302g"] = "ng5"
        u2n["n\u0304g"] = "ng7"; u2n["n\u030Dg"] = "ng8"; u2n["n\u0306g"] = "ng9"

        unicodeToNumber = u2n

        val n2u = LinkedHashMap<String, String>()
        for ((key, value) in u2n) {
            n2u[value] = key
        }
        numberToUnicode = n2u
    }
}

internal object KplToneMap {
    val numberToUnicode: Map<String, String>
    val unicodeToNumber: Map<String, String>

    init {
        val u2n = LinkedHashMap<String, String>()

        // A
        u2n["Á"] = "A2"; u2n["À"] = "A3"; u2n["Â"] = "A5"
        u2n["Ā"] = "A7"; u2n["A\u030D"] = "A8"; u2n["A\u030B"] = "A9"
        u2n["á"] = "a2"; u2n["à"] = "a3"; u2n["â"] = "a5"
        u2n["ā"] = "a7"; u2n["a\u030D"] = "a8"; u2n["a\u030B"] = "a9"

        // I
        u2n["Í"] = "I2"; u2n["Ì"] = "I3"; u2n["Î"] = "I5"
        u2n["Ī"] = "I7"; u2n["I\u030D"] = "I8"; u2n["I\u030B"] = "I9"
        u2n["í"] = "i2"; u2n["ì"] = "i3"; u2n["î"] = "i5"
        u2n["ī"] = "i7"; u2n["i\u030D"] = "i8"; u2n["i\u030B"] = "i9"

        // U
        u2n["Ú"] = "U2"; u2n["Ù"] = "U3"; u2n["Û"] = "U5"
        u2n["Ū"] = "U7"; u2n["U\u030D"] = "U8"; u2n["Ű"] = "U9"
        u2n["ú"] = "u2"; u2n["ù"] = "u3"; u2n["û"] = "u5"
        u2n["ū"] = "u7"; u2n["u\u030D"] = "u8"; u2n["ű"] = "u9"

        // E
        u2n["É"] = "E2"; u2n["È"] = "E3"; u2n["Ê"] = "E5"
        u2n["Ē"] = "E7"; u2n["E\u030D"] = "E8"; u2n["E\u030B"] = "E9"
        u2n["é"] = "e2"; u2n["è"] = "e3"; u2n["ê"] = "e5"
        u2n["ē"] = "e7"; u2n["e\u030D"] = "e8"; u2n["e\u030B"] = "e9"

        // O
        u2n["Ó"] = "O2"; u2n["Ò"] = "O3"; u2n["Ô"] = "O5"
        u2n["Ō"] = "O7"; u2n["O\u030D"] = "O8"; u2n["Ő"] = "O9"
        u2n["ó"] = "o2"; u2n["ò"] = "o3"; u2n["ô"] = "o5"
        u2n["ō"] = "o7"; u2n["o\u030D"] = "o8"; u2n["ő"] = "o9"

        // Oo (KPL uses literal "oo", tone on first O)
        u2n["Óo"] = "Oo2"; u2n["Òo"] = "Oo3"; u2n["Ôo"] = "Oo5"
        u2n["Ōo"] = "Oo7"; u2n["O\u030Do"] = "Oo8"; u2n["Őo"] = "Oo9"
        // OO
        u2n["ÓO"] = "OO2"; u2n["ÒO"] = "OO3"; u2n["ÔO"] = "OO5"
        u2n["ŌO"] = "OO7"; u2n["O\u030DO"] = "OO8"; u2n["ŐO"] = "OO9"
        // oo
        u2n["óo"] = "oo2"; u2n["òo"] = "oo3"; u2n["ôo"] = "oo5"
        u2n["ōo"] = "oo7"; u2n["o\u030Do"] = "oo8"; u2n["őo"] = "oo9"

        // M
        u2n["Ḿ"] = "M2"; u2n["M\u0300"] = "M3"; u2n["M\u0302"] = "M5"
        u2n["M\u0304"] = "M7"; u2n["M\u030D"] = "M8"; u2n["M\u030B"] = "M9"
        u2n["ḿ"] = "m2"; u2n["m\u0300"] = "m3"; u2n["m\u0302"] = "m5"
        u2n["m\u0304"] = "m7"; u2n["m\u030D"] = "m8"; u2n["m\u030B"] = "m9"

        // N
        u2n["Ń"] = "N2"; u2n["Ǹ"] = "N3"; u2n["N\u0302"] = "N5"
        u2n["N\u0304"] = "N7"; u2n["N\u030D"] = "N8"; u2n["N\u030B"] = "N9"
        u2n["ń"] = "n2"; u2n["ǹ"] = "n3"; u2n["n\u0302"] = "n5"
        u2n["n\u0304"] = "n7"; u2n["n\u030D"] = "n8"; u2n["n\u030B"] = "n9"

        // Ng
        u2n["Ńg"] = "Ng2"; u2n["Ǹg"] = "Ng3"; u2n["N\u0302g"] = "Ng5"
        u2n["N\u0304g"] = "Ng7"; u2n["N\u030Dg"] = "Ng8"; u2n["N\u030Bg"] = "Ng9"
        // NG
        u2n["ŃG"] = "NG2"; u2n["ǸG"] = "NG3"; u2n["N\u0302G"] = "NG5"
        u2n["N\u0304G"] = "NG7"; u2n["N\u030DG"] = "NG8"; u2n["N\u030BG"] = "NG9"
        // ng
        u2n["ńg"] = "ng2"; u2n["ǹg"] = "ng3"; u2n["n\u0302g"] = "ng5"
        u2n["n\u0304g"] = "ng7"; u2n["n\u030Dg"] = "ng8"; u2n["n\u030Bg"] = "ng9"

        unicodeToNumber = u2n

        val n2u = LinkedHashMap<String, String>()
        for ((key, value) in u2n) {
            n2u[value] = key
        }
        numberToUnicode = n2u
    }
}
