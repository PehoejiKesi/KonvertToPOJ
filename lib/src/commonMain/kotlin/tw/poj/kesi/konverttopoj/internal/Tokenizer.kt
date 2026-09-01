package tw.poj.kesi.konverttopoj.internal

internal data class Token(val text: String, val isLomaji: Boolean)

internal object Tokenizer {

    // Lomaji characters: letters, combining marks, and special precomposed characters.
    // Using a Set<Char> for O(1) lookup instead of per-character regex matching.
    internal val LOMAJI_CHARS: Set<Char> = buildSet {
        // Basic Latin letters
        addAll('a'..'z')
        addAll('A'..'Z')
        // Special base characters
        add('ṳ'); add('Ṳ')
        // Combining marks
        add('\u0358') // combining dot above right (o͘)
        add('\u0324') // combining diaeresis below (o̤)
        add('\u207F') // superscript n (ⁿ)
        add('\u030D') // combining vertical line above (tone 8)
        add('\u0301') // combining acute accent (tone 2)
        add('\u0300') // combining grave accent (tone 3)
        add('\u0302') // combining circumflex accent (tone 5)
        add('\u0304') // combining macron (tone 7)
        add('\u0306') // combining breve (tone 9 POJ)
        add('\u030B') // combining double acute accent (tone 9 KPL)
        // Precomposed accented vowels (tone 2: acute)
        add('á'); add('Á'); add('í'); add('Í'); add('ú'); add('Ú')
        add('é'); add('É'); add('ó'); add('Ó'); add('ḿ'); add('Ḿ')
        add('ń'); add('Ń')
        // Precomposed accented vowels (tone 3: grave)
        add('à'); add('À'); add('ì'); add('Ì'); add('ù'); add('Ù')
        add('è'); add('È'); add('ò'); add('Ò'); add('ǹ'); add('Ǹ')
        // Precomposed accented vowels (tone 5: circumflex)
        add('â'); add('Â'); add('î'); add('Î'); add('û'); add('Û')
        add('ê'); add('Ê'); add('ô'); add('Ô')
        // Precomposed accented vowels (tone 7: macron)
        add('ā'); add('Ā'); add('ī'); add('Ī'); add('ū'); add('Ū')
        add('ē'); add('Ē'); add('ō'); add('Ō')
        // Precomposed accented vowels (tone 9: breve/double acute)
        add('ă'); add('Ă'); add('ĭ'); add('Ĭ'); add('ŭ'); add('Ŭ')
        add('ĕ'); add('Ĕ'); add('ŏ'); add('Ŏ')
        add('ű'); add('Ű'); add('ő'); add('Ő') // KPL double acute
    }

    /**
     * Split pure lomaji text by delimiters (hyphens, spaces, punctuation, etc.).
     * Each syllable is returned as a token. Any non-lomaji character (including /,
     * parentheses, brackets, etc.) is returned as a non-lomaji delimiter token.
     */
    fun splitSyllables(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val current = StringBuilder()

        for (c in text) {
            val isLomajiChar = c in LOMAJI_CHARS || c.isDigit()
            if (isLomajiChar) {
                current.append(c)
            } else {
                if (current.isNotEmpty()) {
                    tokens.add(Token(current.toString(), true))
                    current.clear()
                }
                tokens.add(Token(c.toString(), false))
            }
        }

        if (current.isNotEmpty()) {
            tokens.add(Token(current.toString(), true))
        }

        return tokens
    }

    /**
     * Split hybrid text (lomaji mixed with non-lomaji content like CJK, numbers, etc.)
     * into alternating lomaji and non-lomaji segments.
     */
    fun splitHybrid(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val current = StringBuilder()
        var currentIsLomaji = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            val isLomajiChar = c in LOMAJI_CHARS ||
                (c == '-' && isHyphenBetweenLomaji(text, i)) ||
                c.isDigit() && current.isNotEmpty() && currentIsLomaji

            if (current.isEmpty()) {
                currentIsLomaji = isLomajiChar
                current.append(c)
            } else if (isLomajiChar == currentIsLomaji) {
                current.append(c)
            } else {
                tokens.add(Token(current.toString(), currentIsLomaji))
                current.clear()
                currentIsLomaji = isLomajiChar
                current.append(c)
            }
            i++
        }

        if (current.isNotEmpty()) {
            tokens.add(Token(current.toString(), currentIsLomaji))
        }

        return tokens
    }

    private fun isHyphenBetweenLomaji(text: String, idx: Int): Boolean {
        if (idx <= 0 || idx >= text.length - 1) return false
        return text[idx - 1] in LOMAJI_CHARS && text[idx + 1] in LOMAJI_CHARS
    }
}
