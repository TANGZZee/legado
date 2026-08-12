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
    fun calculatesTimeOfDaySummaryAndRatioForSelectedDay() {
        val date = "2026-08-04"
        val hourly = listOf(
            io.legado.app.data.entities.ReadRecordDailyHour(date, 16, 20_000L, 100L),
            io.legado.app.data.entities.ReadRecordDailyHour(date, 18, 60_000L, 390L),
            io.legado.app.data.entities.ReadRecordDailyHour(date, 2, 10_000L, 0L)
        )

        val result = calculateReadRecordTimeOfDay(
            period = ReadRecordStatsPeriod.DAY,
            anchor = LocalDate.of(2026, 8, 4),
            hourlyRecords = hourly
        )

        assertEquals(24, result.hourly.size)
        assertEquals(18, result.summary.peakHour)
        assertEquals(ReadRecordTimeBucket.EVENING, result.summary.peakBucket)
        assertEquals(20_000L, result.ratios[ReadRecordTimeBucket.AFTERNOON])
        assertEquals(60_000L, result.ratios[ReadRecordTimeBucket.EVENING])
        assertEquals(10_000L, result.ratios[ReadRecordTimeBucket.DAWN])
        assertEquals(90_000L, result.summary.totalTime)
        assertEquals(10_000L, result.summary.nightTime)
    }

    @Test
    fun aggregatesTimeOfDayAcrossDaysAndKeepsEmptyBuckets() {
        val hourly = listOf(
            io.legado.app.data.entities.ReadRecordDailyHour("2026-08-03", 13, 30_000L),
            io.legado.app.data.entities.ReadRecordDailyHour("2026-08-04", 18, 60_000L)
        )

        val result = calculateReadRecordTimeOfDay(
            period = ReadRecordStatsPeriod.WEEK,
            anchor = LocalDate.of(2026, 8, 4),
            hourlyRecords = hourly
        )

        assertEquals(30_000L, result.ratios[ReadRecordTimeBucket.AFTERNOON])
        assertEquals(60_000L, result.ratios[ReadRecordTimeBucket.EVENING])
        assertEquals(0L, result.ratios[ReadRecordTimeBucket.MORNING])
        assertEquals(18, result.summary.peakHour)
    }

    @Test
    fun averagesTheFirstReadingHourAcrossDays() {
        val hourly = listOf(
            io.legado.app.data.entities.ReadRecordDailyHour("2026-08-03", 12, 30_000L),
            io.legado.app.data.entities.ReadRecordDailyHour("2026-08-04", 18, 60_000L)
        )

        val result = calculateReadRecordTimeOfDay(
            period = ReadRecordStatsPeriod.WEEK,
            anchor = LocalDate.of(2026, 8, 4),
            hourlyRecords = hourly
        )

        assertEquals(15, result.summary.averageStartHour)
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
