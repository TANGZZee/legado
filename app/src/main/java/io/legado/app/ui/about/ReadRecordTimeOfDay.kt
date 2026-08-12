package io.legado.app.ui.about

import io.legado.app.data.entities.ReadRecordDailyHour
import java.time.LocalDate

/** Four buckets used by the reading-time distribution card. */
enum class ReadRecordTimeBucket(
    val label: String,
    val startHour: Int,
    val endHourInclusive: Int
) {
    DAWN("凌晨", 0, 5),
    MORNING("上午", 6, 11),
    AFTERNOON("下午", 12, 17),
    EVENING("晚上", 18, 23);

    companion object {
        fun ofHour(hour: Int): ReadRecordTimeBucket {
            return entries.first { hour.coerceIn(0, 23) in it.startHour..it.endHourInclusive }
        }
    }
}

data class ReadRecordHourlyValue(
    val hour: Int,
    val readTime: Long
)

data class ReadRecordTimeOfDaySummary(
    val peakHour: Int?,
    val peakBucket: ReadRecordTimeBucket?,
    val averageStartHour: Int?,
    val totalTime: Long,
    val nightTime: Long
) {
    val nightRatioPercent: Int
        get() = if (totalTime <= 0L) 0 else ((nightTime * 100L) / totalTime).toInt()
}

data class ReadRecordTimeOfDayResult(
    val hourly: List<ReadRecordHourlyValue>,
    val ratios: Map<ReadRecordTimeBucket, Long>,
    val summary: ReadRecordTimeOfDaySummary
)

/**
 * Aggregates hourly records for a selected statistics period.
 * Records outside the period are ignored; empty hours and buckets remain present.
 */
fun calculateReadRecordTimeOfDay(
    period: ReadRecordStatsPeriod,
    anchor: LocalDate,
    hourlyRecords: List<ReadRecordDailyHour>
): ReadRecordTimeOfDayResult {
    val range = ReadRecordStatsRange.of(period, anchor)
    val selected = hourlyRecords.filter { record ->
        runCatching { range.contains(LocalDate.parse(record.date)) }.getOrDefault(false)
    }
    val hourly = (0..23).map { hour ->
        ReadRecordHourlyValue(
            hour = hour,
            readTime = selected.filter { it.hour == hour }.sumOf { it.readTime.coerceAtLeast(0L) }
        )
    }
    val ratios = ReadRecordTimeBucket.entries.associateWith { bucket ->
        selected.filter { ReadRecordTimeBucket.ofHour(it.hour) == bucket }
            .sumOf { it.readTime.coerceAtLeast(0L) }
    }
    val totalTime = hourly.sumOf { it.readTime }
    val peak = hourly.maxByOrNull { it.readTime }
    val peakHour = peak?.takeIf { it.readTime > 0L }?.hour
    val peakBucket = peakHour?.let(ReadRecordTimeBucket::ofHour)
    val averageStartHour = selected
        .filter { it.readTime > 0L }
        .groupBy { it.date }
        .values
        .mapNotNull { dayRecords -> dayRecords.minByOrNull { it.hour }?.hour }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.toInt()
    val nightTime = ratios[ReadRecordTimeBucket.DAWN] ?: 0L
    return ReadRecordTimeOfDayResult(
        hourly = hourly,
        ratios = ratios,
        summary = ReadRecordTimeOfDaySummary(
            peakHour = peakHour,
            peakBucket = peakBucket,
            averageStartHour = averageStartHour,
            totalTime = totalTime,
            nightTime = nightTime
        )
    )
}

fun formatReadRecordClock(hour: Int?): String {
    return hour?.let { "${it.coerceIn(0, 23).toString().padStart(2, '0')}:00" } ?: "--"
}

fun formatReadRecordBucket(bucket: ReadRecordTimeBucket?): String = bucket?.label ?: "--"

fun formatReadRecordDuration(millis: Long): String {
    val seconds = millis.coerceAtLeast(0L) / 1000L
    return when {
        seconds >= 3600L -> "${seconds / 3600L}小时${(seconds % 3600L) / 60L}分钟"
        seconds >= 60L -> "${seconds / 60L}分${seconds % 60L}秒"
        else -> "${seconds}秒"
    }
}
