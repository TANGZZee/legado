package io.legado.app.ui.about

import kotlin.math.pow

import io.legado.app.data.entities.ReadRecordDailyBook
import java.time.LocalDate
import java.time.YearMonth

enum class ReadRecordTrendMetric {
    TIME,
    WORDS,
    SPEED
}

enum class ReadRecordTrendChartType {
    BAR,
    LINE
}

data class ReadRecordTrendPoint(
    val date: LocalDate,
    val readTime: Long,
    val readWords: Long
) {
    val speed: Long
        get() = if (readTime > 0L) {
            (readWords * 60_000L / readTime).coerceAtLeast(0L)
        } else {
            0L
        }

    fun value(metric: ReadRecordTrendMetric): Long {
        return when (metric) {
            ReadRecordTrendMetric.TIME -> readTime
            ReadRecordTrendMetric.WORDS -> readWords
            ReadRecordTrendMetric.SPEED -> speed
        }
    }
}

data class ReadRecordTrendData(
    val anchorMonth: YearMonth,
    val points: List<ReadRecordTrendPoint>
)

data class ReadRecordTrendUi(
    val period: ReadRecordStatsPeriod,
    val data: ReadRecordTrendData
)

fun calculateReadRecordTrend(
    period: ReadRecordStatsPeriod,
    anchor: LocalDate,
    dailyBooks: List<ReadRecordDailyBook>
): ReadRecordTrendData {
    val grouped = dailyBooks.groupByDate()
    return when (period) {
        ReadRecordStatsPeriod.DAY -> {
            val date = anchor
            val now = java.time.LocalDateTime.now()
            val isToday = date == now.toLocalDate()
            val activeHour = if (isToday) now.hour.coerceIn(0, 23) else 12
            val dailyPoint = grouped[date] ?: ReadRecordTrendPoint(date, 0L, 0L)
            val points = (0..24).map { hour ->
                if (hour == activeHour) dailyPoint
                else ReadRecordTrendPoint(date, 0L, 0L)
            }
            ReadRecordTrendData(YearMonth.from(date), points)
        }
        ReadRecordStatsPeriod.WEEK -> {
            val range = ReadRecordStatsRange.of(period, anchor)
            val start = range.start ?: anchor
            val end = range.end ?: anchor
            val points = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .map { grouped[it] ?: ReadRecordTrendPoint(it, 0L, 0L) }
                .toList()
            ReadRecordTrendData(YearMonth.from(anchor), points)
        }
        ReadRecordStatsPeriod.MONTH -> {
            val month = YearMonth.from(anchor)
            val points = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                grouped[date] ?: ReadRecordTrendPoint(date, 0L, 0L)
            }
            ReadRecordTrendData(month, points)
        }
        ReadRecordStatsPeriod.YEAR -> {
            val year = anchor.year
            val points = (1..12).map { monthValue ->
                val start = LocalDate.of(year, monthValue, 1)
                val end = start.withDayOfMonth(start.lengthOfMonth())
                val time = grouped.filterKeys { !it.isBefore(start) && !it.isAfter(end) }
                    .values.sumOf { it.readTime.coerceAtLeast(0L) }
                val words = grouped.filterKeys { !it.isBefore(start) && !it.isAfter(end) }
                    .values.sumOf { it.readWords.coerceAtLeast(0L) }
                ReadRecordTrendPoint(start, time, words)
            }
            ReadRecordTrendData(YearMonth.of(year, 1), points)
        }
        ReadRecordStatsPeriod.TOTAL -> {
            val allYears = grouped.keys.map { it.year }.plus(anchor.year).distinct().sorted()
            val years = allYears.ifEmpty { listOf(anchor.year) }
            val points = years.map { year ->
                val start = LocalDate.of(year, 1, 1)
                val end = LocalDate.of(year, 12, 31)
                val time = grouped.filterKeys { !it.isBefore(start) && !it.isAfter(end) }
                    .values.sumOf { it.readTime.coerceAtLeast(0L) }
                val words = grouped.filterKeys { !it.isBefore(start) && !it.isAfter(end) }
                    .values.sumOf { it.readWords.coerceAtLeast(0L) }
                ReadRecordTrendPoint(start, time, words)
            }
            ReadRecordTrendData(YearMonth.from(anchor), points)
        }
    }
}

private fun List<ReadRecordDailyBook>.groupByDate(): Map<LocalDate, ReadRecordTrendPoint> {
    return asSequence()
        .mapNotNull { record ->
            runCatching {
                LocalDate.parse(record.date) to record
            }.getOrNull()
        }
        .groupBy { it.first }
        .mapValues { (_, pairs) ->
            val date = pairs.first().first
            val records = pairs.map { it.second }
            ReadRecordTrendPoint(
                date = date,
                readTime = records.sumOf { it.readTime.coerceAtLeast(0L) },
                readWords = records.sumOf { it.readWords.coerceAtLeast(0L) }
            )
        }
}

private fun log10(value: Double): Double = kotlin.math.log10(value)
private fun floor(value: Double): Double = kotlin.math.floor(value)
private fun pow10(exponent: Int): Double = 10.0.pow(exponent.toDouble())

data class TrendAxisTicks(
    val maxValue: Long,
    val ticks: List<Long>,
    val format: (Long) -> String
)

fun numberAxisTicks(rawMax: Long, targetTicks: Int = 5): TrendAxisTicks {
    if (rawMax <= 0L) {
        val defaultMax = 4L
        return TrendAxisTicks(
            maxValue = defaultMax,
            ticks = (0L..defaultMax).toList(),
            format = { it.toString() }
        )
    }
    val magnitude = pow10(floor(log10(rawMax.toDouble())).toInt()).toLong()
    val normalized = rawMax.toDouble() / magnitude
    val (stepBase, maxMultiplier) = when {
        normalized <= 1.0 -> 0.25 to 1.0
        normalized <= 2.0 -> 0.5 to 2.0
        normalized <= 5.0 -> 1.0 to 5.0
        else -> 2.0 to 10.0
    }
    var step = (magnitude * stepBase).toLong().coerceAtLeast(1L)
    var maxValue = (magnitude * maxMultiplier).toLong()
    if (maxValue < rawMax) {
        maxValue = ((rawMax / step) + 1L) * step
    }
    val count = (maxValue / step).toInt()
    val ticks = (0..count).map { it * step }
    return TrendAxisTicks(maxValue, ticks) { it.toString() }
}

fun timeAxisTicks(rawMaxMs: Long): TrendAxisTicks {
    return when {
        rawMaxMs <= 0L -> TrendAxisTicks(
            maxValue = 60_000L,
            ticks = listOf(0L, 15_000L, 30_000L, 45_000L, 60_000L),
            format = ::formatTrendTime
        )
        rawMaxMs < 60_000L -> {
            val rawSec = rawMaxMs / 1000.0
            val ticks = numberAxisTicks(kotlin.math.ceil(rawSec).toLong(), 5)
            val maxMs = ticks.maxValue * 1000L
            TrendAxisTicks(maxMs, ticks.ticks.map { it * 1000L }, ::formatTrendTime)
        }
        rawMaxMs < 3_600_000L -> {
            val rawMin = rawMaxMs / 60_000.0
            val ticks = numberAxisTicks(kotlin.math.ceil(rawMin).toLong(), 5)
            val maxMs = ticks.maxValue * 60_000L
            TrendAxisTicks(maxMs, ticks.ticks.map { it * 60_000L }, ::formatTrendTime)
        }
        else -> {
            val rawHour = rawMaxMs / 3_600_000.0
            val ticks = numberAxisTicks(kotlin.math.ceil(rawHour).toLong(), 5)
            val maxMs = ticks.maxValue * 3_600_000L
            TrendAxisTicks(maxMs, ticks.ticks.map { it * 3_600_000L }, ::formatTrendTime)
        }
    }
}

fun formatTrendTime(ms: Long): String {
    val totalSeconds = ms / 1000.0
    val totalMinutes = totalSeconds / 60.0
    val totalHours = totalMinutes / 60.0
    return when {
        totalHours >= 1.0 -> {
            val hours = totalHours.toInt()
            val rem = ((totalMinutes - hours * 60) / 60 * 10).toInt()
            if (rem > 0) "${hours}.${rem}小时" else "${hours}小时"
        }
        totalMinutes >= 1.0 -> {
            val minutes = totalMinutes.toInt()
            val seconds = (totalSeconds - minutes * 60).toInt()
            when {
                seconds == 30 -> "${minutes}.5分钟"
                seconds == 0 -> "${minutes}分钟"
                else -> "${minutes}分${seconds}秒"
            }
        }
        else -> "${totalSeconds.toInt()}秒"
    }
}

fun formatTrendWords(words: Long): String {
    return "${words.coerceAtLeast(0L)}字"
}

fun formatTrendSpeed(speed: Long): String {
    return speed.coerceAtLeast(0L).toString()
}

fun trendAxisTicks(metric: ReadRecordTrendMetric, rawMax: Long): TrendAxisTicks {
    return when (metric) {
        ReadRecordTrendMetric.TIME -> timeAxisTicks(rawMax)
        ReadRecordTrendMetric.WORDS -> numberAxisTicks(rawMax).let {
            TrendAxisTicks(it.maxValue, it.ticks, ::formatTrendWords)
        }
        ReadRecordTrendMetric.SPEED -> numberAxisTicks(rawMax).let {
            TrendAxisTicks(it.maxValue, it.ticks, ::formatTrendSpeed)
        }
    }
}