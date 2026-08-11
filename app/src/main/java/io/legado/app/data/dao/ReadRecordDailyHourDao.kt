package io.legado.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.legado.app.data.entities.ReadRecordDailyHour

@Dao
interface ReadRecordDailyHourDao {

    @get:Query("select * from readRecordDailyHour")
    val all: List<ReadRecordDailyHour>

    @Query(
        "select * from readRecordDailyHour " +
            "where date = :date and hour = :hour limit 1"
    )
    fun get(date: String, hour: Int): ReadRecordDailyHour?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: ReadRecordDailyHour)

    @Query("delete from readRecordDailyHour")
    fun clear()
}
