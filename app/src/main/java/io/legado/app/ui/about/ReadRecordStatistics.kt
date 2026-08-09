package io.legado.app.ui.about

import io.legado.app.data.entities.ReadRecordDaily
import io.legado.app.data.entities.ReadRecordDailyBook
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

enum class ReadRecordStatsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    TOTAL
}

data class ReadRecordStatsRange(
    val start: LocalDate?,
    val end: LocalDate?
) {
    fun contains(date: LocalDate): Boolean {
        return (start == null || !date.isBefore(start)) &&
            (end == null || !date.isAfter(end))
    }

    companion object {
        fun of(period: ReadRecordStatsPeriod, anchor: LocalDate): ReadRecordStatsRange {
            return when (period) {
                ReadRecordStatsPeriod.DAY -> ReadRecordStatsRange(anchor, anchor)
                ReadRecordStatsPeriod.WEEK -> {
                    val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                    val start = anchor.with(TemporalAdjusters.previousOrSame(firstDay))
                    ReadRecordStatsRange(start, start.plusDays(6))
                }
                ReadRecordStatsPeriod.MONTH -> {
                    val month = YearMonth.from(anchor)
                    ReadRecordStatsRange(month.atDay(1), month.atEndOfMonth())
                }
                ReadRecordStatsPeriod.YEAR -> {
                    ReadRecordStatsRange(
                        LocalDate.of(anchor.year, 1, 1),
                        LocalDate.of(anchor.year, 12, 31)
                    )
                }
                ReadRecordStatsPeriod.TOTAL -> ReadRecordStatsRange(null, null)
            }
        }
    }
}

data class ReadRecordStatistics(
    val readingTime: Long,
    val readingDays: Int,
    val totalReadBooks: Int,
    val finishedBooks: Int,
    val inProgressBooks: Int,
    val readingWords: Long,
    val noteCount: Int,
    val readingSpeed: Long
)

fun calculateReadRecordStatistics(
    period: ReadRecordStatsPeriod,
    anchor: LocalDate,
    dailyRecords: List<ReadRecordDaily>,
    dailyBooks: List<ReadRecordDailyBook>,
    bookmarkTimes: List<Long>
): ReadRecordStatistics {
    val range = ReadRecordStatsRange.of(period, anchor)
    val records = dailyRecords.filter { record ->
        runCatching { range.contains(LocalDate.parse(record.date)) }.getOrDefault(false)
    }
    val books = dailyBooks.filter { record ->
        runCatching { range.contains(LocalDate.parse(record.date)) }.getOrDefault(false)
    }.groupBy { it.bookUrl }
        .values
        .map { records ->
            records.reduce { accumulated, record ->
                accumulated.copy(
                    bookName = record.bookName.ifBlank { accumulated.bookName },
                    readTime = accumulated.readTime + record.readTime.coerceAtLeast(0L),
                    readWords = accumulated.readWords + record.readWords.coerceAtLeast(0L),
                    finished = accumulated.finished || record.finished,
                    updatedAt = maxOf(accumulated.updatedAt, record.updatedAt)
                )
            }
        }
    val notes = bookmarkTimes.count { time ->
        val date = java.time.Instant.ofEpochMilli(time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        range.contains(date)
    }
    val readingTime = records.sumOf { it.readTime.coerceAtLeast(0L) }
    val readingWords = books.sumOf { it.readWords.coerceAtLeast(0L) }
    val totalReadBooks = books.size
    val finishedBooks = books.count { it.finished }
    return ReadRecordStatistics(
        readingTime = readingTime,
        readingDays = records.count { it.readTime > 0L },
        totalReadBooks = totalReadBooks,
        finishedBooks = finishedBooks,
        inProgressBooks = (totalReadBooks - finishedBooks).coerceAtLeast(0),
        readingWords = readingWords,
        noteCount = notes,
        readingSpeed = if (readingTime > 0L) {
            (readingWords * 60_000L / readingTime).coerceAtLeast(0L)
        } else {
            0L
        }
    )
}
