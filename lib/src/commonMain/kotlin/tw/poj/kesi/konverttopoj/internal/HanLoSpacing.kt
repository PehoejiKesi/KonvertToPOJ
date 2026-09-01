package tw.poj.kesi.konverttopoj.internal

internal enum class HanLoTokenType {
    LOMAJI,
    HANJI,
    PUNCTUATION,
    WHITESPACE,
    OTHER
}

internal data class HanLoToken(val text: String, val type: HanLoTokenType)

internal enum class HanLoPunctuationMode {
    /** Always emit full-width punctuation (Han-Lo standard). */
    FORCE_FULLWIDTH,

    /**
     * Per-sentence: full-width punctuation if the sentence contains any Hanji,
     * half-width punctuation if the sentence is pure lomaji.
     */
    AUTO_PER_SENTENCE,
}

internal object HanLoSpacing {

    // Half-width → full-width punctuation conversion map
    private val HALF_TO_FULL: Map<Char, Char> = mapOf(
        ',' to '，', // ，
        '.' to '。', // 。
        '!' to '！', // ！
        '?' to '？', // ？
        ';' to '；', // ；
        ':' to '：', // ：
        '(' to '（', // （
        ')' to '）', // ）
    )

    private val FULL_TO_HALF: Map<Char, Char> =
        HALF_TO_FULL.entries.associate { (k, v) -> v to k } + mapOf(
            '、' to ',', // 、 → ,
        )

    // Characters that terminate a sentence (used by AUTO_PER_SENTENCE mode).
    private val SENTENCE_TERMINATORS: Set<Char> = setOf(
        '.', '!', '?',
        '。', // 。
        '！', // ！
        '？', // ？
    )

    fun fix(
        text: String,
        mode: HanLoPunctuationMode = HanLoPunctuationMode.FORCE_FULLWIDTH,
        aggressive: Boolean = true
    ): String {
        if (text.isEmpty()) return text
        // Process line-by-line to preserve newlines. Strip a trailing \r from
        // CRLF lines before tokenizing and re-append after, so aggressive mode
        // doesn't silently swallow it as whitespace.
        return text.split('\n').joinToString("\n") { rawLine ->
            if (rawLine.endsWith('\r')) {
                fixLine(rawLine.dropLast(1), mode, aggressive) + "\r"
            } else {
                fixLine(rawLine, mode, aggressive)
            }
        }
    }

    private fun fixLine(line: String, mode: HanLoPunctuationMode, aggressive: Boolean): String {
        if (line.isEmpty()) return line
        val tokens = tokenize(line)
        // Aggressive: drop whitespace tokens up front and rebuild canonical spacing.
        // Conservative: keep whitespace tokens; only drop where Han-Lo rules forbid them.
        val working = if (aggressive) tokens.filter { it.type != HanLoTokenType.WHITESPACE } else tokens
        if (working.isEmpty() || working.all { it.type == HanLoTokenType.WHITESPACE }) return line

        val transformed = when (mode) {
            HanLoPunctuationMode.FORCE_FULLWIDTH ->
                working.map { convertPunctuation(it, useFullwidth = true) }
            HanLoPunctuationMode.AUTO_PER_SENTENCE ->
                applyAutoPunctuation(working)
        }

        return if (aggressive) assembleWithSpacing(transformed) else assemblePreservingWhitespace(transformed)
    }

    private fun applyAutoPunctuation(content: List<HanLoToken>): List<HanLoToken> {
        // Split tokens into sentences. A sentence ends at (and includes) a sentence-terminator
        // punctuation token. Trailing tokens with no terminator form a final sentence.
        val sentences = mutableListOf<MutableList<HanLoToken>>()
        var current = mutableListOf<HanLoToken>()
        for (tok in content) {
            current.add(tok)
            if (tok.type == HanLoTokenType.PUNCTUATION &&
                tok.text.length == 1 &&
                tok.text[0] in SENTENCE_TERMINATORS
            ) {
                sentences.add(current)
                current = mutableListOf()
            }
        }
        if (current.isNotEmpty()) sentences.add(current)

        val result = mutableListOf<HanLoToken>()
        for (sentence in sentences) {
            val hasHanji = sentence.any { it.type == HanLoTokenType.HANJI }
            for (tok in sentence) {
                result.add(convertPunctuation(tok, useFullwidth = hasHanji))
            }
        }
        return result
    }

    private fun convertPunctuation(tok: HanLoToken, useFullwidth: Boolean): HanLoToken {
        if (tok.type != HanLoTokenType.PUNCTUATION) return tok
        val map = if (useFullwidth) HALF_TO_FULL else FULL_TO_HALF
        val sb = StringBuilder()
        var changed = false
        for (c in tok.text) {
            val replacement = map[c]
            if (replacement != null) {
                sb.append(replacement)
                changed = true
            } else {
                sb.append(c)
            }
        }
        return if (changed) HanLoToken(sb.toString(), tok.type) else tok
    }

    private fun assemblePreservingWhitespace(content: List<HanLoToken>): String {
        if (content.isEmpty()) return ""
        val sb = StringBuilder()
        for (i in content.indices) {
            val tok = content[i]
            // Adjacent WS tokens cannot exist (the tokenizer merges them),
            // so the immediate neighbor is always non-WS or out-of-bounds.
            if (tok.type == HanLoTokenType.WHITESPACE &&
                shouldDropWhitespaceBetween(content.getOrNull(i - 1), content.getOrNull(i + 1))
            ) continue
            sb.append(tok.text)
        }
        return sb.toString()
    }

    private fun shouldDropWhitespaceBetween(left: HanLoToken?, right: HanLoToken?): Boolean {
        // Leading or trailing whitespace at line edge — preserve
        if (left == null || right == null) return false
        // Conservative drop-rule is the inverse of aggressive insert-rule for
        // non-edge cases: if Han-Lo would not insert a space here, the user's
        // whitespace violates the rule and gets dropped.
        return !needsSpace(left, right)
    }

    private fun assembleWithSpacing(content: List<HanLoToken>): String {
        if (content.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append(content[0].text)
        for (i in 1 until content.size) {
            if (needsSpace(content[i - 1], content[i])) {
                sb.append(' ')
            }
            sb.append(content[i].text)
        }
        return sb.toString()
    }

    // Half-width clause/sentence punctuation that takes a trailing space before
    // a word-like token (lomaji or digit/symbol).
    private val HALFWIDTH_TRAILING_SPACE: Set<Char> = setOf(',', '.', '!', '?', ';', ':')

    private fun needsSpace(left: HanLoToken, right: HanLoToken): Boolean {
        val lt = left.type
        val rt = right.type
        // Hanji adjacency: never a space
        if (lt == HanLoTokenType.HANJI || rt == HanLoTokenType.HANJI) return false
        // Punctuation: half-width clause punctuation gets a trailing space before
        // a word-like token (lomaji or digit/symbol); otherwise punctuation never
        // gets surrounding spaces.
        if (lt == HanLoTokenType.PUNCTUATION) {
            return (rt == HanLoTokenType.LOMAJI || rt == HanLoTokenType.OTHER) &&
                left.text.length == 1 &&
                left.text[0] in HALFWIDTH_TRAILING_SPACE
        }
        if (rt == HanLoTokenType.PUNCTUATION) return false
        // Otherwise (LOMAJI/OTHER on both sides): two non-merging tokens can only
        // arise from whitespace between them in the input — keep them separate.
        return true
    }

    private fun tokenize(text: String): List<HanLoToken> {
        val tokens = mutableListOf<HanLoToken>()
        val current = StringBuilder()
        var currentType: HanLoTokenType? = null
        var i = 0

        while (i < text.length) {
            val c = text[i]
            val charType = classifyChar(c, text, i, currentType)

            if (currentType == null) {
                currentType = charType
                current.append(c)
            } else if (charType == currentType && canMerge(charType)) {
                current.append(c)
            } else {
                tokens.add(HanLoToken(current.toString(), currentType))
                current.clear()
                currentType = charType
                current.append(c)
            }
            i++
        }

        if (current.isNotEmpty() && currentType != null) {
            tokens.add(HanLoToken(current.toString(), currentType))
        }

        return tokens
    }

    private fun canMerge(type: HanLoTokenType): Boolean = when (type) {
        HanLoTokenType.LOMAJI -> true
        HanLoTokenType.HANJI -> true
        HanLoTokenType.WHITESPACE -> true
        HanLoTokenType.OTHER -> true
        HanLoTokenType.PUNCTUATION -> false // Each punctuation is its own token
    }

    private fun classifyChar(c: Char, text: String, idx: Int, currentType: HanLoTokenType?): HanLoTokenType {
        val code = c.code
        return when {
            // Whitespace, including ideographic space U+3000
            c == ' ' || c == '\t' || c == '\r' || code == 0x3000 -> HanLoTokenType.WHITESPACE

            // Lomaji character (reuse the shared set)
            c in Tokenizer.LOMAJI_CHARS -> HanLoTokenType.LOMAJI

            // Hyphen between lomaji characters → part of lomaji token
            c == '-' && isHyphenBetweenLomaji(text, idx) -> HanLoTokenType.LOMAJI

            // Digit following lomaji → tone number, part of lomaji token
            c.isDigit() && currentType == HanLoTokenType.LOMAJI -> HanLoTokenType.LOMAJI

            // CJK Unified Ideographs
            code in 0x4E00..0x9FFF -> HanLoTokenType.HANJI
            // CJK Extension A
            code in 0x3400..0x4DBF -> HanLoTokenType.HANJI
            // CJK Compatibility Ideographs
            code in 0xF900..0xFAFF -> HanLoTokenType.HANJI
            // CJK Radicals Supplement
            code in 0x2E80..0x2EFF -> HanLoTokenType.HANJI
            // Kangxi Radicals
            code in 0x2F00..0x2FDF -> HanLoTokenType.HANJI

            // Half-width punctuation that gets converted to full-width
            c in HALF_TO_FULL -> HanLoTokenType.PUNCTUATION

            // CJK Symbols and Punctuation (、。〃 etc.)
            code in 0x3001..0x303F -> HanLoTokenType.PUNCTUATION
            // Fullwidth forms (！＂＃ through ～)
            code in 0xFF01..0xFF60 -> HanLoTokenType.PUNCTUATION
            // CJK Compatibility Forms
            code in 0xFE30..0xFE4F -> HanLoTokenType.PUNCTUATION
            // Vertical forms
            code in 0xFE10..0xFE1F -> HanLoTokenType.PUNCTUATION

            // Standalone digits (not after lomaji) → OTHER (gets a space when adjacent
            // to lomaji or another OTHER; no space when adjacent to Hanji or punctuation)
            c.isDigit() -> HanLoTokenType.OTHER

            // Hyphen not between lomaji
            c == '-' -> HanLoTokenType.PUNCTUATION

            else -> HanLoTokenType.OTHER
        }
    }

    private fun isHyphenBetweenLomaji(text: String, idx: Int): Boolean {
        if (idx <= 0 || idx >= text.length - 1) return false
        return text[idx - 1] in Tokenizer.LOMAJI_CHARS && text[idx + 1] in Tokenizer.LOMAJI_CHARS
    }
}
