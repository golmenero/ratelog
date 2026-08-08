package org.ratelog.import.letterboxd

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class LetterboxdCsvParserTest {

    private lateinit var parser: LetterboxdCsvParser

    @BeforeEach
    fun setUp() {
        parser = LetterboxdCsvParser()
    }

    @Test
    fun `should parse valid CSV with all fields`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,The Shawshank Redemption,1994,https://letterboxd.com/film/the-shawshank-redemption/,5.0
            2024-01-16,The Godfather,1972,https://letterboxd.com/film/the-godfather/,4.5
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(2, entries.size)
        assertEquals("The Shawshank Redemption", entries[0].name)
        assertEquals(1994, entries[0].year)
        assertEquals(5.0, entries[0].rating)
        assertEquals("The Godfather", entries[1].name)
        assertEquals(4.5, entries[1].rating)
    }

    @Test
    fun `should return InvalidFormat when CSV is empty`() {
        val csv = ""

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isLeft())
        assertEquals(LetterboxdParseError.InvalidFormat, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return InvalidFormat when header is missing required fields`() {
        val csv = """
            Date,Name
            2024-01-15,The Shawshank Redemption
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isLeft())
        assertEquals(LetterboxdParseError.InvalidFormat, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return NoValidEntries when CSV has only header`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isLeft())
        assertEquals(LetterboxdParseError.NoValidEntries, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should skip entries with invalid rating`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,The Shawshank Redemption,1994,https://letterboxd.com/film/the-shawshank-redemption/,5.0
            2024-01-16,Invalid Movie,2000,https://letterboxd.com/film/invalid/,6.0
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertEquals("The Shawshank Redemption", entries[0].name)
    }

    @Test
    fun `should handle CSV with quoted fields containing commas`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,"Movie, The",1994,https://letterboxd.com/film/movie/,5.0
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertEquals("Movie, The", entries[0].name)
    }

    @Test
    fun `should handle entries without year`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,Unknown Movie,,https://letterboxd.com/film/unknown/,4.0
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertNull(entries[0].year)
        assertEquals(4.0, entries[0].rating)
    }

    @Test
    fun `should handle minimum valid rating`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,Bad Movie,2000,https://letterboxd.com/film/bad/,0.5
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertEquals(0.5, entries[0].rating)
    }

    @Test
    fun `should handle maximum valid rating`() {
        val csv = """
            Date,Name,Year,Letterboxd URI,Rating
            2024-01-15,Great Movie,2000,https://letterboxd.com/film/great/,5.0
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertEquals(5.0, entries[0].rating)
    }

    @Test
    fun `should parse real Letterboxd CSV format`() {
        val csv = """Date,Name,Year,Letterboxd URI,Rating
2024-01-15,The Shawshank Redemption,1994,https://letterboxd.com/film/the-shawshank-redemption/,5.0
2024-01-16,The Godfather,1972,https://letterboxd.com/film/the-godfather/,4.5
2024-01-17,"12 Angry Men",1957,https://letterboxd.com/film/12-angry-men/,4.0"""

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(3, entries.size)
        assertEquals("The Shawshank Redemption", entries[0].name)
        assertEquals(1994, entries[0].year)
        assertEquals(5.0, entries[0].rating)
        assertEquals("The Godfather", entries[1].name)
        assertEquals("12 Angry Men", entries[2].name)
    }

    @Test
    fun `should handle CSV with BOM`() {
        val csv = """Date,Name,Year,Letterboxd URI,Rating
2024-01-15,The Shawshank Redemption,1994,https://letterboxd.com/film/the-shawshank-redemption/,5.0"""
        
        val csvWithBOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + csv.toByteArray()

        val result = parser.parse(ByteArrayInputStream(csvWithBOM))

        assertTrue(result.isRight())
        val entries = result.getOrNull()!!
        assertEquals(1, entries.size)
        assertEquals("The Shawshank Redemption", entries[0].name)
    }
}
