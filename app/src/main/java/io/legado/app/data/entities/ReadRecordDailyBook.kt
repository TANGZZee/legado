package io.legado.app.data.entities

import androidx.room.Entity

@Entity(
    tableName = "readRecordDailyBooks",
    primaryKeys = ["date", "bookUrl"]
)
data class ReadRecordDailyBook(
    var date: String = "",
    var bookUrl: String = "",
    var bookName: String = "",
    var readTime: Long = 0L,
    var readWords: Long = 0L,
    var finished: Boolean = false,
    var updatedAt: Long = System.currentTimeMillis()
)
