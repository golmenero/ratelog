package org.ratelog.import.letterboxd

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.InputStream

@Component
class LetterboxdCsvParser {

    private val csvMapper = CsvMapper()

    fun parse(inputStream: InputStream): Either<LetterboxdParseError, List<LetterboxdEntry>> = either {
        val bytes = inputStream.readBytes()
        val cleanBytes = removeBOM(bytes)
        val cleanInputStream = ByteArrayInputStream(cleanBytes)
        
        val schema = CsvSchema.emptySchema().withHeader()

        val entries = try {
            csvMapper.readerFor(LetterboxdEntry::class.java)
                .with(schema)
                .readValues<LetterboxdEntry>(cleanInputStream)
                .readAll()
                .filter { it.rating in 0.5..5.0 }
        } catch (e: Exception) {
            raise(LetterboxdParseError.InvalidFormat)
        }

        ensure(entries.isNotEmpty()) { LetterboxdParseError.NoValidEntries }

        entries
    }

    private fun removeBOM(bytes: ByteArray): ByteArray {
        return if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            bytes.copyOfRange(3, bytes.size)
        } else {
            bytes
        }
    }
}

sealed interface LetterboxdParseError {
    data object EmptyFile : LetterboxdParseError
    data object InvalidFormat : LetterboxdParseError
    data object NoValidEntries : LetterboxdParseError
}
