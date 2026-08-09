package io.legado.app.help

import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.ReadRecordDailyBook
import io.legado.app.data.entities.ReadRecordDaily
import io.legado.app.receiver.ReadGoalWidgetProvider
import io.legado.app.receiver.ReadRankWidgetProvider
import splitties.init.appCtx
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReadRecordDailyHelper {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun record(
        readTime: Long,
        timestamp: Long = System.currentTimeMillis(),
        forceWidgetUpdate: Boolean = false
    ) {
        record(
            book = null,
            readTime = readTime,
            readWords = 0L,
            finished = false,
            timestamp = timestamp,
            forceWidgetUpdate = forceWidgetUpdate
        )
    }

    fun record(
        book: Book?,
        readTime: Long,
        readWords: Long = 0L,
        finished: Boolean = false,
        timestamp: Long = System.currentTimeMillis(),
        forceWidgetUpdate: Boolean = false
    ) {
        if (readTime <= 0L) return
        val dateKey = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(dateFormatter)
        val current = appDb.readRecordDailyDao.get(dateKey)
        val record = if (current == null) {
            ReadRecordDaily(
                date = dateKey,
                readTime = readTime,
                updatedAt = timestamp
            )
        } else {
            current.copy(
                readTime = current.readTime + readTime,
                updatedAt = timestamp
            )
        }
        appDb.readRecordDailyDao.insert(record)
        if (book != null) {
            val currentBook = appDb.readRecordDailyBookDao.get(dateKey, book.bookUrl)
            appDb.readRecordDailyBookDao.insert(
                if (currentBook == null) {
                    ReadRecordDailyBook(
                        date = dateKey,
                        bookUrl = book.bookUrl,
                        bookName = book.name,
                        readTime = readTime,
                        readWords = readWords.coerceAtLeast(0L),
                        finished = finished,
                        updatedAt = timestamp
                    )
                } else {
                    currentBook.copy(
                        bookName = book.name,
                        readTime = currentBook.readTime + readTime,
                        readWords = currentBook.readWords + readWords.coerceAtLeast(0L),
                        finished = currentBook.finished || finished,
                        updatedAt = timestamp
                    )
                }
            )
        }
        ReadGoalWidgetProvider.updateAll(appCtx, force = forceWidgetUpdate)
        ReadRankWidgetProvider.updateAll(appCtx, force = forceWidgetUpdate)
    }
}
