package tw.poj.kesi.konverttopoj.internal

import tw.poj.kesi.konverttopoj.ConvertOptions
import tw.poj.kesi.konverttopoj.LomajiFormat

/**
 * Validates whether a syllable is a linguistically valid Taiwanese roman orthography syllable.
 *
 * Uses a whitelist of valid rhyme patterns extracted from the 狗公會曉學台語 PDF
 * (pages 78, 82-83: IPA table, study cards with all valid syllable components).
 *
 * Syllable structure: [initial consonant] + [rhyme] + [tone]
 */
internal object SyllableValidator {

    // --- POJ valid initials (longest first for greedy matching) ---
    // Note: "z", "chn", "hn" are used in some historical/dialectal texts (e.g., 台日大辭典)
    private val POJ_INITIALS = listOf(
        "chh", "chn", "ch", "ph", "th", "kh", "hn", "ng", "p", "m", "b", "t", "n", "l", "k", "g", "h", "s", "j", "z"
    )

    // --- KPL valid initials ---
    private val KPL_INITIALS = listOf(
        "tsh", "tsn", "ts", "ph", "th", "kh", "hn", "ng", "p", "m", "b", "t", "n", "l", "k", "g", "h", "s", "j", "z"
    )

    // --- Valid POJ rhymes (base, without Haikhau dialect) ---
    // Source: 狗公會曉學台語 pages 78 (IPA table), 82-83 (study cards)
    private val POJ_RHYMES_BASE = setOf(
        // 母音 Single vowels
        "a", "i", "u", "oo", "e", "o",

        // 複母音 Compound vowels (diphthongs/triphthongs)
        "ai", "au", "ia", "iu", "io", "ioo", "ui", "oa", "oe", "ei", "ona",
        "iau", "oai",

        // 鼻母音 Nasal vowels (with nn = ⁿ in input mode)
        "ann", "inn", "unn", "oonn", "enn", "onn",
        "ainn", "aunn", "iann", "iunn", "ionn", "uinn", "oann", "oenn", "iaunn", "oainn",
        "oiann",

        // 鼻子音尾 Nasal coda finals
        "am", "an", "ang",
        "im", "in", "iam", "iang", "iong",
        "um", "un", "en",
        "om", "ong", "oan", "oang",
        "ian", "eng", "ieng",

        // 束聲 Checked finals
        "ap", "at", "ak", "ah",
        "ip", "it", "ek",
        "ut",
        "op", "ok", "ooh", "eh", "oh",
        "ih", "uh",
        "iap", "iat", "iak", "iok",
        "iah", "ioh", "iooh", "iuh",
        "oat", "oak",
        "oah", "oeh",
        "aih", "auh", "iauh",
        "uih", "oaih",

        // 鼻母音束聲 Nasal + checked
        "annh", "innh", "unnh", "oonnh", "ennh", "onnh",
        "ainnh", "aunnh", "iannh", "iunnh", "uinnh", "oannh", "oainnh", "iaunnh",
        "oennh",

        // Standalone nasals (syllabic consonants)
        "m", "ng",
        "mh", "ngh"
    )

    // --- POJ 海口腔 Haikhau dialect rhymes ---
    private val POJ_HAIKAU_RHYMES = setOf(
        "ur", "or",
        "uri", "urt",
        "ore", "oer", "urinn",
        "urn", "orn",
        "orh", "urh",
        "oreh", "ork"
    )

    // --- Standard nasal+checked POJ rhymes (hnn instead of nnh) ---
    // When traditionalNasal is enabled, both traditional "annh" and standard "ahnn" forms are accepted
    private val POJ_STANDARD_NASAL_CHECKED by lazy {
        POJ_RHYMES_BASE.filter { it.endsWith("nnh") }
            .map { it.dropLast(3) + "hnn" }
            .toSet()
    }

    // --- Valid KPL rhymes (base, without Haikhau dialect) ---
    // oa→ua, oe→ue, ek→ik, eng→ing, oo stays oo
    private val KPL_RHYMES_BASE = setOf(
        // Single vowels (same as POJ)
        "a", "i", "u", "oo", "e", "o",

        // Compound vowels (oa→ua, oe→ue)
        "ai", "au", "ia", "iu", "io", "ioo", "ui", "ua", "ue", "ei", "una",
        "iau", "uai",

        // Nasal vowels (nn for nasal marker)
        "ann", "inn", "unn", "oonn", "enn", "onn",
        "ainn", "aunn", "iann", "iunn", "ionn", "uinn", "uann", "uenn", "iaunn", "uainn",
        "uiann",

        // Nasal coda finals (eng→ing, +ua variants)
        "am", "an", "ang",
        "im", "in", "iam", "iang", "iong",
        "um", "un", "en",
        "om", "ong", "uan", "uang",
        "ian", "ing", "iing",

        // Checked finals (ek→ik, +ua variants)
        "ap", "at", "ak", "ah",
        "ip", "it", "ik",
        "ut",
        "op", "ok", "ooh", "eh", "oh",
        "ih", "uh",
        "iap", "iat", "iak", "iok",
        "iah", "ioh", "iooh", "iuh",
        "uat", "uak",
        "uah", "ueh",
        "aih", "auh", "iauh",
        "uih", "uaih",

        // Nasal + checked
        "annh", "innh", "unnh", "oonnh", "ennh", "onnh",
        "ainnh", "aunnh", "iannh", "iunnh", "uinnh", "uannh", "uainnh", "iaunnh",
        "uennh",

        // Standalone nasals
        "m", "ng",
        "mh", "ngh"
    )

    // --- KPL 海口腔 Haikhau dialect rhymes ---
    private val KPL_HAIKAU_RHYMES = setOf(
        "ir", "er",
        "iri", "irt",
        "ere", "eer", "uer", "irinn",
        "irn", "ern",
        "erh", "irh",
        "ereh", "erk"
    )

    // --- Standard nasal+checked KPL rhymes (hnn instead of nnh) ---
    private val KPL_STANDARD_NASAL_CHECKED by lazy {
        KPL_RHYMES_BASE.filter { it.endsWith("nnh") }
            .map { it.dropLast(3) + "hnn" }
            .toSet()
    }

    // --- Combined initials for permissive mode (used by convertHybrid) ---
    private val PERMISSIVE_INITIALS by lazy {
        (POJ_INITIALS + KPL_INITIALS).distinct()
    }

    // --- Extra non-standard rhymes for permissive mode ---
    private val EXTRA_PERMISSIVE_RHYMES = setOf(
        // Non-standard checked finals (found in 台日大辭典)
        "ig", "eg", "og",
        // Non-standard compound vowels / nasals
        "aiu", "aoann", "auann",
        // Extended vowel combinations (Embree / historical dictionaries)
        "aon", "aom", "iun", "iui", "uii", "ooi",
        "aim", "ain", "oin", "oing",
        "aun", "aum",
        // Additional compound vowels
        "iiu", "oau",
        // Non-standard nasal coda finals
        "uin", "uen", "ien",
        // Additional checked finals
        "oih", "eih",
        "aip", "ait", "aik",
        "oik", "oip",
        "uik", "uit",
        // Additional nasal + checked
        "ainnh", "aunnh",
        // Rare diphthongs
        "uoi"
    )

    // Checked finals end in p, t, k, h
    private val CHECKED_ENDINGS = setOf('p', 't', 'k', 'h')

    // Valid tone numbers
    private val VALID_TONES = setOf('1', '2', '3', '4', '5', '7', '8', '9')

    /**
     * Build the effective rhyme set based on format, mode, and options.
     */
    private fun getRhymes(isPoj: Boolean, permissive: Boolean, options: ConvertOptions): Set<String> {
        val base: Set<String> = if (permissive) {
            POJ_RHYMES_BASE + KPL_RHYMES_BASE + EXTRA_PERMISSIVE_RHYMES
        } else if (isPoj) {
            POJ_RHYMES_BASE
        } else {
            KPL_RHYMES_BASE
        }

        if (!options.haikau && !options.traditionalNasal) return base

        val result = base.toMutableSet()
        if (options.haikau) {
            when {
                permissive -> { result += POJ_HAIKAU_RHYMES; result += KPL_HAIKAU_RHYMES }
                isPoj -> result += POJ_HAIKAU_RHYMES
                else -> result += KPL_HAIKAU_RHYMES
            }
        }
        if (options.traditionalNasal) {
            when {
                permissive -> { result += POJ_STANDARD_NASAL_CHECKED; result += KPL_STANDARD_NASAL_CHECKED }
                isPoj -> result += POJ_STANDARD_NASAL_CHECKED
                else -> result += KPL_STANDARD_NASAL_CHECKED
            }
        }
        return result
    }

    fun isValid(syllable: String, format: LomajiFormat, strictTones: Boolean = true, permissive: Boolean = false, options: ConvertOptions = ConvertOptions()): Boolean {
        if (syllable.isBlank()) return false

        val lower = syllable.lowercase()
        val isInput = format == LomajiFormat.POJ_INPUT || format == LomajiFormat.KPL_INPUT
        val isPoj = format == LomajiFormat.POJ_INPUT || format == LomajiFormat.POJ_UNICODE

        return if (isInput) {
            isValidInput(lower, isPoj, strictTones, permissive, options)
        } else {
            isValidUnicode(lower, isPoj, strictTones, permissive, options)
        }
    }

    private fun isValidInput(lower: String, isPoj: Boolean, strictTones: Boolean = true, permissive: Boolean = false, options: ConvertOptions): Boolean {
        val initials = if (permissive) PERMISSIVE_INITIALS else if (isPoj) POJ_INITIALS else KPL_INITIALS
        val rhymes = getRhymes(isPoj, permissive, options)

        // Extract tone number (last char if digit)
        var base = lower
        var toneChar: Char? = null
        if (base.isNotEmpty() && base.last().isDigit()) {
            toneChar = base.last()
            base = base.dropLast(1)
        }

        // Handle tone 8 checked: "p8", "t8", "k8", "h8" — the 8 is already stripped,
        // but the base should end in p/t/k/h for tone 8
        if (toneChar != null && toneChar !in VALID_TONES) return false

        // Try the base as a standalone rhyme first (handles "m", "ng", "mh", "ngh")
        if (base in rhymes) {
            return !strictTones || validateToneConsistency(base, toneChar)
        }

        // Strip initial consonant (longest match first)
        var rhyme = base
        for (initial in initials) {
            if (base.startsWith(initial)) {
                val candidate = base.removePrefix(initial)
                if (candidate.isNotEmpty() && candidate in rhymes) {
                    rhyme = candidate
                    break
                }
            }
        }

        // If rhyme is still == base (no initial stripped) or not in whitelist, invalid
        if (rhyme !in rhymes) return false

        return !strictTones || validateToneConsistency(rhyme, toneChar)
    }

    private fun validateToneConsistency(rhyme: String, toneChar: Char?): Boolean {
        // Standard nasal+checked rhymes end in "hnn" (h coda + nn nasal marker)
        val isChecked = rhyme.isNotEmpty() && (rhyme.last() in CHECKED_ENDINGS || rhyme.endsWith("hnn"))
        if (toneChar != null) {
            // Checked syllables allow tones 4, 8, and 9
            if (isChecked && toneChar != '4' && toneChar != '8' && toneChar != '9') return false
            // Non-checked syllables cannot have tones 4 or 8 (but 9 is allowed)
            if (!isChecked && (toneChar == '4' || toneChar == '8')) return false
        }
        return true
    }

    private fun isValidUnicode(lower: String, isPoj: Boolean, strictTones: Boolean = true, permissive: Boolean = false, options: ConvertOptions): Boolean {
        // For unicode validation, first convert to input form, then validate
        val u2n: Map<String, String> = if (isPoj) PojToneMap.unicodeToNumber else KplToneMap.unicodeToNumber

        // Try to find and remove the tone diacritical
        var inputForm = lower
        var foundTone = false
        for ((unicode, numberStr) in u2n) {
            val unicodeLower = unicode.lowercase()
            if (lower.contains(unicodeLower)) {
                val base = numberStr.dropLast(1).lowercase()
                val tone = numberStr.last()
                inputForm = lower.replace(unicodeLower, base) + tone
                foundTone = true
                break
            }
        }

        // Handle POJ-specific unicode characters
        if (isPoj) {
            inputForm = inputForm
                .replace("\u207F", "nn") // ⁿ → nn
                .replace(normalizeNfc("o\u0358"), "oo") // o͘ → oo
                .replace("ṳ", "ur")
                .replace("o\u0324", "or")
        }

        return isValidInput(inputForm, isPoj, strictTones, permissive, options)
    }
}
