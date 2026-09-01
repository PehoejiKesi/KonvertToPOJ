package tw.poj.kesi.konverttopoj.internal

/**
 * Tone mark placement and removal for POJ and KPL syllables.
 *
 * POJ rules (from 狗公會曉學台語 Section 21):
 *   1. Single vowel → mark it
 *   2. No vowel → mark nasal (m, n, ng as 1 unit)
 *   3. ur/or → mark as 2-char unit
 *   4. "oo" → mark as 2-char unit
 *   5. Compound vowels: mark 2nd letter from right (skip ⁿ, ng=1 unit)
 *      Exception 1: if 2nd from right is "i" → mark 1st (last) letter
 *      Exception 2: checked syllable + 2nd from right is "i"/"u" (not "iu"/"iuh") → mark 3rd letter
 *      Special: "iuh" → mark 2nd from right
 *
 * KPL rules (from 狗公會曉學台語 Section 45):
 *   1. If 'a' present → mark the 'a'
 *   2. Else mark rightmost vowel (treating "oo" as unit, mark left 'o')
 *   3. Else mark nasal (m, n, ng — mark left letter of "ng")
 */
internal object ToneMarker {

    private val IU_REGEX = "[iu]".toRegex()
    private val PTKH_REGEX = "[ptkh]".toRegex()
    private val VOWEL_REGEX = "[aiueo]".toRegex()

    // --- POJ: input (number) → unicode ---

    fun pojInputToUnicode(syllable: String): String {
        var s = pojFixNn(syllable)
        if (s.length <= 1) return s

        val last = s.last()
        if (!last.isDigit()) return pojFixOo(s)

        val toneNumber = last.toString()
        val base = s.dropLast(1)
        // Nothing that can carry a tone mark — a bare numeral like "2024", say.
        // Leave the token exactly as it came in; the trailing digit is not a tone.
        val pos = findPojTonePosition(base.lowercase()) ?: return pojFixOo(s)

        // Tones 1 and 4 carry no diacritical — drop the tone number, mark nothing.
        if (last == '1' || last == '4') return pojFixOo(base)

        val target = base.substring(pos.offset, pos.offset + pos.length)
        val key = target + toneNumber
        val replacement = PojToneMap.numberToUnicode[key] ?: return base

        val result = base.substring(0, pos.offset) + replacement +
            base.substring(pos.offset + pos.length)
        return normalizeNfc(result)
    }

    private fun pojFixNn(s: String): String {
        if (!s.contains("nng", ignoreCase = true)) {
            return s.replace("nn", "\u207F").replace("NN", "\u207F").replace("Nn", "\u207F")
        }
        return s
    }

    private fun pojFixOo(s: String): String {
        return if (s.isAllUpper()) {
            s.replace("OO", "O\u0358")
                .replace("UR", "\u1E72") // Ṳ uppercase
                .replace("OR", "O\u0324")
        } else {
            s.replace("Oo", "O\u0358")
                .replace("oo", "o\u0358")
                .replace("Ur", "Ṳ")
                .replace("ur", "ṳ")
                .replace("Or", "O\u0324")
                .replace("or", "o\u0324")
        }
    }

    // --- POJ: unicode → input (number) ---

    fun pojUnicodeToInput(syllable: String, isAllUppercase: Boolean = false): String {
        for ((unicode, numberStr) in PojToneMap.unicodeToNumber) {
            if (syllable.contains(unicode)) {
                val base = numberStr.dropLast(1)
                val tone = numberStr.last().toString()
                val result = syllable.replace(unicode, base) + tone
                return pojFixJiboToInput(result, isAllUppercase)
            }
        }
        return pojFixJiboToInput(syllable, isAllUppercase)
    }

    private fun pojFixJiboToInput(s: String, isAllUppercase: Boolean): String {
        var result = s.replace("\u207F", "nn") // ⁿ → nn
        if (isAllUppercase) {
            result = result.replace(normalizeNfc("O\u0358"), "OO")
                .replace("Ṳ", "UR").replace("Ur", "UR").replace("Oo", "OO")
        } else {
            result = result.replace(normalizeNfc("O\u0358"), "Oo")
        }
        return result.replace(normalizeNfc("o\u0358"), "oo")
            .replace("ṳ", "ur")
            .replace("o\u0324", "or")
    }

    // --- KPL: input (number) → unicode ---

    fun kplInputToUnicode(syllable: String): String {
        if (syllable.length <= 1) return syllable

        val last = syllable.last()
        if (!last.isDigit()) return syllable

        val toneNumber = last.toString()
        val base = syllable.dropLast(1)
        return kplPlaceTone(base, toneNumber)
    }

    private fun kplPlaceTone(base: String, toneNumber: String): String {
        val lower = base.lowercase()

        // Rule 1: if 'a' present → mark it
        val idxA = lower.lastIndexOf('a')
        if (idxA >= 0) {
            return kplReplace(base, toneNumber, idxA, 1)
        }

        // Rule 2: rightmost vowel (i, u, o, e), treating "oo" as unit
        val idxVowel = lower.lastIndexOfAny(charArrayOf('i', 'u', 'o', 'e'))
        if (idxVowel >= 0) {
            // Check for "oo" — mark the first 'o'
            if (idxVowel > 0 && lower.substring(idxVowel - 1, idxVowel + 1) == "oo") {
                return kplReplace(base, toneNumber, idxVowel - 1, 2)
            }
            return kplReplace(base, toneNumber, idxVowel, 1)
        }

        // Rule 3: nasal consonant (ng as unit, then m, n)
        val idxNg = lower.indexOf("ng")
        if (idxNg >= 0) {
            return kplReplace(base, toneNumber, idxNg, 2)
        }
        val idxM = lower.indexOf('m')
        if (idxM >= 0) {
            return kplReplace(base, toneNumber, idxM, 1)
        }
        val idxN = lower.indexOf('n')
        if (idxN >= 0) {
            return kplReplace(base, toneNumber, idxN, 1)
        }

        return base + toneNumber
    }

    private fun kplReplace(base: String, toneNumber: String, offset: Int, length: Int): String {
        val target = base.substring(offset, offset + length)
        val key = target + toneNumber
        val replacement = KplToneMap.numberToUnicode[key] ?: return base
        return base.substring(0, offset) + replacement + base.substring(offset + length)
    }

    // --- KPL: unicode → input (number) ---

    fun kplUnicodeToInput(syllable: String): String {
        for ((unicode, numberStr) in KplToneMap.unicodeToNumber) {
            if (syllable.contains(unicode)) {
                val base = numberStr.dropLast(1)
                val tone = numberStr.last().toString()
                return syllable.replace(unicode, base) + tone
            }
        }
        return syllable
    }

    // --- POJ tone position finder ---

    internal data class TonePosition(val offset: Int, val length: Int)

    internal fun findPojTonePosition(lowerBase: String): TonePosition? {
        val vowelList = listOf("a", "i", "u", "o", "e")
        val semivowelList = listOf("m", "ng", "n")
        val haikhauVowelList = listOf("ur", "or")

        val lastVowelIdx = lowerBase.lastIndexOfAny(vowelList)

        if (lastVowelIdx == -1) {
            // No vowel — try semivowel
            val lastSemiIdx = lowerBase.lastIndexOfAny(semivowelList)
            if (lastSemiIdx == -1) return null
            val len = if (lowerBase.contains("ng")) 2 else 1
            return TonePosition(lastSemiIdx, len)
        }

        // Handle ur/or special vowels
        if (lowerBase.contains(Regex("(ur|or)"))) {
            val idx = lowerBase.lastIndexOfAny(haikhauVowelList)
            return TonePosition(idx, 2)
        }

        val vowelCount = countAdjacentVowels(lowerBase, lastVowelIdx)

        if (vowelCount == 1) {
            return TonePosition(lastVowelIdx, 1)
        }

        if (vowelCount == 2 && lowerBase.contains("oo")) {
            return TonePosition(lastVowelIdx - 1, 2)
        }

        // Compound vowel (diphthong/triphthong)
        val pos2nd = findLetterFromRight(lowerBase, 2)
        val char2nd = lowerBase.substring(pos2nd, pos2nd + 1)

        if (isCheckedSyllable(lowerBase)) {
            // Checked syllable compound vowel — ⁿ is transparent to the pattern check,
            // so "iuⁿh" behaves like "iuh" (hiu\u030Dⁿh, not hi\u030Duⁿh).
            if (stripNasal(lowerBase).contains("iuh")) {
                return TonePosition(findLetterFromRight(lowerBase, 2), 1)
            }
            if (char2nd.matches(IU_REGEX)) {
                return TonePosition(findLetterFromRight(lowerBase, 3), 1)
            }
            return TonePosition(pos2nd, 1)
        } else {
            // Open syllable compound vowel
            if (char2nd == "i") {
                return TonePosition(findLetterFromRight(lowerBase, 1), 1)
            }
            return TonePosition(findLetterFromRight(lowerBase, 2), 1)
        }
    }

    private fun isCheckedSyllable(lowerBase: String): Boolean {
        val cleaned = stripNasal(lowerBase)
        return cleaned.isNotEmpty() && cleaned.last().toString().matches(PTKH_REGEX)
    }

    /** Drop the ⁿ nasal marker so rhyme patterns can be matched on the bare letters. */
    private fun stripNasal(s: String): String = s.replace("\u207F", "")

    /**
     * Find the position of the Nth letter from the right,
     * skipping ⁿ (U+207F) and treating "ng" as 1 letter and o͘ dot (U+0358) as part of o.
     */
    private fun findLetterFromRight(s: String, n: Int): Int {
        var pos = s.length - 1
        var count = 0

        while (pos >= 0) {
            val c = s[pos]
            when {
                c == '\u207F' -> { /* skip ⁿ */ }
                c == '\u0358' -> { /* skip o͘ dot, part of previous letter */ }
                c.lowercaseChar() == 'g' && pos > 0 && s[pos - 1].lowercaseChar() == 'n' -> {
                    // "ng" = 1 letter
                    count++
                    if (count == n) { pos--; break }
                    pos -= 2
                    continue
                }
                else -> {
                    count++
                    if (count == n) break
                }
            }
            pos--
        }
        return maxOf(pos, 0)
    }

    private fun countAdjacentVowels(s: String, lastVowelIdx: Int): Int {
        if (lastVowelIdx == 0) return 1
        val prevChar = s[lastVowelIdx - 1].toString()
        return if (prevChar.contains(VOWEL_REGEX)) 2 else 1
    }

    // --- String extensions for lastIndexOfAny(List<String>) ---

    private fun String.lastIndexOfAny(strings: List<String>): Int {
        var maxIdx = -1
        for (s in strings) {
            val idx = this.lastIndexOf(s)
            if (idx > maxIdx) maxIdx = idx
        }
        return maxIdx
    }
}
