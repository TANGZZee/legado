package io.legado.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.legado.app.data.entities.ReadRecordDailyBook

@Dao
interface ReadRecordDailyBookDao {

    @get:Query("select * from readRecordDailyBooks")
    val all: List<ReadRecordDailyBook>

    @Query(
        "select * from readRecordDailyBooks " +
            "where date >= :startDate and date <= :endDate"
    )
    fun between(startDate: String, endDate: String): List<ReadRecordDailyBook>

    @Query(
        "select * from readRecordDailyBooks " +
            "where date = :date and bookUrl = :bookUrl limit 1"
    )
    fun get(date: String, bookUrl: String): ReadRecordDailyBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: ReadRecordDailyBook)

    @Query("delete from readRecordDailyBooks")
    fun clear()

    @Query("delete from readRecordDailyBooks where date = :date")
    fun delete(date: String)
}
