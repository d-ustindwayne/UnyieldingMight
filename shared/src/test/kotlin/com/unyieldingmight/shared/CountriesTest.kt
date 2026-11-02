package com.unyieldingmight.shared

import com.unyieldingmight.shared.domain.Countries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CountriesTest {

    @Test
    fun `address names map to ISO codes`() {
        assertEquals("NZ", Countries.codeFor("New Zealand"))
        assertEquals("JP", Countries.codeFor(" japan "))
        assertNull(Countries.codeFor("Atlantis"))
        assertNull(Countries.codeFor(null))
    }

    @Test
    fun `phone regions are validated`() {
        assertEquals("JP", Countries.validCode("jp"))
        assertNull(Countries.validCode("ZZ"))
        assertNull(Countries.validCode(""))
    }

    @Test
    fun `flags are built from the country code`() {
        // Regional indicator N (U+1F1F3) followed by Z (U+1F1FF).
        assertEquals(String(Character.toChars(0x1F1F3)) + String(Character.toChars(0x1F1FF)), Countries.flag("nz"))
    }

    @Test
    fun `search puts names starting with the query first`() {
        val results = Countries.search("ven")
        assertEquals("Venezuela", results.first().name)
        assertTrue(results.any { it.name == "Slovenia" })
        assertEquals("US", Countries.search("us").first().code)
    }

    @Test
    fun `every country name fits the address column`() {
        assertTrue(Countries.all.size > 200)
        assertTrue(Countries.all.all { it.name.length <= 50 }, Countries.all.maxBy { it.name.length }.name)
    }
}
