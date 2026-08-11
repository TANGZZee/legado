package io.legado.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "readRecordDailyHour",
    primaryKeys = ["date", "hour"]
)
data class ReadRecordDailyHour(
    var date: String = "",
    var hour: Int = 0,
    @ColumnInfo(defaultValue = "0")
    var readTime: Long = 0L,
    @ColumnInfo(defaultValue = "0")
    var readWords: Long = 0L,
    var updatedAt: Long = System.currentTimeMillis()
)
