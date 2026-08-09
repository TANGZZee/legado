package io.legado.app.ui.about

import io.legado.app.data.entities.ReadRecordDaily
import io.legado.app.data.entities.ReadRecordDailyBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReadRecordStatisticsTest {

    @Test
    fun aggregatesBookRowsAcrossDaysAndCountsNotesInRange() {
        val statistics = calculateReadRecordStatistics(
            period = ReadRecordStatsPeriod.MONTH,
            anchor = LocalDate.of(2026, 8, 15),
            dailyRecords = listOf(
                ReadRecordDaily(date = "2026-08-01", readTime = 60_000L),
                ReadRecordDaily(date = "2026-08-02", readTime = 30_000L),
                ReadRecordDaily(date = "2026-07-31", readTime = 90_000L)
            ),
            dailyBooks = listOf(
                ReadRecordDailyBook(
                    date = "2026-08-01",
                    bookUrl = "book-a",
                    readWords = 100L,
                    finished = false
                ),
                ReadRecordDailyBook(
                    date = "2026-08-02",
                    bookUrl = "book-a",
                    readWords = 200L,
                    finished = true
                ),
                ReadRecordDailyBook(
                    date = "2026-08-02",
                    bookUrl = "book-b",
                    readWords = 50L,
                    finished = false
                ),
                ReadRecordDailyBook(
                    date = "2026-07-31",
                    bookUrl = "book-c",
                    readWords = 999L,
                    finished = true
                )
            ),
            bookmarkTimes = listOf(
                LocalDate.of(2026, 8, 2).atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli(),
                LocalDate.of(2026, 7, 31).atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            )
        )

        assertEquals(90_000L, statistics.readingTime)
        assertEquals(2, statistics.readingDays)
        assertEquals(2, statistics.totalReadBooks)
        assertEquals(1, statistics.finishedBooks)
        assertEquals(1, statistics.inProgressBooks)
        assertEquals(350L, statistics.readingWords)
        assertEquals(1, statistics.noteCount)
        assertEquals(233L, statistics.readingSpeed)
    }

    @Test
    fun periodRangesMoveByTheirNaturalUnit() {
        val anchor = LocalDate.of(2026, 8, 8)

        assertEquals(
            ReadRecordStatsRange(anchor, anchor),
            ReadRecordStatsRange.of(ReadRecordStatsPeriod.DAY, anchor)
        )
        val week = ReadRecordStatsRange.of(ReadRecordStatsPeriod.WEEK, anchor)
        assertTrue(week.contains(anchor))
        assertEquals(7L, ChronoUnit.DAYS.between(week.start, week.end) + 1L)
        assertEquals(
            ReadRecordStatsRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)),
            ReadRecordStatsRange.of(ReadRecordStatsPeriod.MONTH, anchor)
        )
        assertEquals(
            ReadRecordStatsRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
            ReadRecordStatsRange.of(ReadRecordStatsPeriod.YEAR, anchor)
        )
        assertEquals(
            ReadRecordStatsRange(null, null),
            ReadRecordStatsRange.of(ReadRecordStatsPeriod.TOTAL, anchor)
        )
    }
}
