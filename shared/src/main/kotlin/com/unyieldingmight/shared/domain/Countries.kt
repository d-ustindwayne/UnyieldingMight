package com.unyieldingmight.shared.domain

import java.util.Locale

/** ISO 3166 countries. Addresses store the English name; the news feed works with the two-letter code. */
object Countries {
    data class Country(val code: String, val name: String)

    val all: List<Country> = Locale.getISOCountries()
        .map { Country(it, nameOf(it)) }
        .filter { it.name.isNotBlank() && it.name != it.code }
        .sortedBy { it.name }

    private val byName = all.associateBy { it.name.lowercase() }

    fun nameOf(code: String): String = Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.ENGLISH)

    /** "New Zealand" -> "NZ". Null when the name isn't a known country. */
    fun codeFor(name: String?): String? = name?.trim()?.lowercase()?.let { byName[it]?.code }

    /** The flag emoji for a two-letter code, built from Unicode regional indicator letters ("NZ" -> 🇳🇿). */
    fun flag(code: String): String = buildString {
        code.uppercase().forEach { appendCodePoint(0x1F1E6 + (it - 'A')) }
    }

    /** Countries matching what the user typed: names starting with it first, then names containing it, or an exact code. */
    fun search(query: String): List<Country> {
        val q = query.trim()
        if (q.isEmpty()) return all
        val (starts, rest) = all.filter { it.name.contains(q, ignoreCase = true) || it.code.equals(q, ignoreCase = true) }
            .partition { it.name.startsWith(q, ignoreCase = true) || it.code.equals(q, ignoreCase = true) }
        return starts + rest
    }

    /** Normalises a region such as "nz" to "NZ", or null when it isn't an ISO country. */
    fun validCode(code: String?): String? = code?.trim()?.uppercase()?.takeIf { c -> all.any { it.code == c } }
}
