package com.yeet.notified

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.yeet.notified.Models.NotificationReceived
import com.yeet.notified.Models.NotifcationRemoved

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "Notified.db"
private const val TABLE_NOTIFICATION_RECEIVED = "notification_received"
private const val COL_ID = "id"
private const val COL_KEY = "key"
private const val COL_PACKAGE_NAME = "package_name"
private const val COL_POST_TIME = "post_time"
private const val COL_TICKER_TEXT = "ticker_text"
private const val COL_TITLE = "title"
private const val COL_TEXT = "text"
private const val COL_PRIORITY = "priority"
private const val COL_CATEGORY = "category"
private const val TABLE_NOTIFICATION_REMOVED = "notification_removed"
private const val TABLE_SMS_NOTIFICATION = "sms_notification"

class DBHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL("""
                CREATE TABLE $TABLE_NOTIFICATION_RECEIVED
                ($COL_ID INTEGER PRIMARY KEY,
                $COL_KEY TEXT,
                $COL_PACKAGE_NAME TEXT,
                $COL_POST_TIME DATETIME,
                $COL_TICKER_TEXT TEXT,
                $COL_TITLE TEXT,
                $COL_TEXT TEXT,
                $COL_PRIORITY INTEGER)
                """
        )

        db?.execSQL("""
                CREATE TABLE $TABLE_NOTIFICATION_REMOVED
                ($COL_ID INTEGER PRIMARY KEY,
                $COL_KEY TEXT,
                $COL_PACKAGE_NAME TEXT,
                $COL_POST_TIME DATETIME,
                $COL_TICKER_TEXT TEXT,
                $COL_TITLE TEXT,
                $COL_TEXT TEXT,
                $COL_PRIORITY INTEGER,
                $COL_CATEGORY TEXT)
                """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // TODO: Write a proper migration switch-case statement
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION_RECEIVED)
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION_REMOVED)
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_NOTIFICATION)
        onCreate(db)
    }

    fun insertNotificationReceived(notification: NotificationReceived) {
        val values = ContentValues()

        values.put(COL_KEY, notification.key)
        values.put(COL_PACKAGE_NAME, notification.packageName)
        values.put(COL_POST_TIME, notification.postTime)
        values.put(COL_TICKER_TEXT, notification.tickerText)
        values.put(COL_TITLE, notification.title)
        values.put(COL_TEXT, notification.text)
        values.put(COL_PRIORITY, notification.priority)

        val db = writableDatabase

        db.insert(TABLE_NOTIFICATION_RECEIVED, null, values)
        db.close()
    }

    fun insertNotificationRemoved(notification: NotifcationRemoved) {
        val values = ContentValues()

        values.put(COL_KEY, notification.key)
        values.put(COL_PACKAGE_NAME, notification.packageName)
        values.put(COL_POST_TIME, notification.postTime)
        values.put(COL_TICKER_TEXT, notification.tickerText)
        values.put(COL_TITLE, notification.title)
        values.put(COL_TEXT, notification.text)
        values.put(COL_PRIORITY, notification.priority)
        values.put(COL_CATEGORY, notification.category)

        val db = writableDatabase

        db.insert(TABLE_NOTIFICATION_RECEIVED, null, values)
        db.close()
    }

    // NOTE: Caller MUST close return value
    private fun getLastNDaysData(days: int) {
        val db = readableDatabase

        val cursor = db.query(
                TABLE_NOTIFICATION_RECEIVED,
                null,
                "$COL_POST_TIME >= DATEADD(day,-$days, GETDATE())",
                null,
                null,
                "$COL_POST_TIME DESC",
                null
        )

        db.close()

        return cursor
    }

    // NOTE: Caller MUST close return value
    fun getLastWeekData() {
        return getLastNDaysData(7)
    }

    // NOTE: Caller MUST close return value
    fun getLastDayData() {
        return getLastNDaysData(1)
    }

    // NOTE: Caller MUST close return value
    fun getMostFrequent(category: String, isAsc: Boolean) {
        val db = readableDatabase
        val sort = if (isAsc) "ASC" else "DESC"

        val rawSql = "SELECT $category, COUNT($category) FROM $TABLE_NOTIFICATION_RECEIVED GROUP BY $category ORDER BY COUNT($category) $sort"
        val cursor = db.rawQuery(rawSql, null)

        db.close()

        return cursor
    }

    // NOTE: Caller MUST close return value
    fun getMostPopularTime(isDay: Boolean, isAsc: Boolean) {
        val db = readableDatabase
        val sort = if (isAsc) "ASC" else "DESC"
        val day = if (isDay) "DAYNAME($COL_POST_TIME)" else "HOUR($COL_POST_TIME)"

        val rawSql = "SELECT $day, COUNT($day) FROM $TABLE_NOTIFICATION_RECEIVED GROUP BY $day ORDER BY COUNT($day) $sort"
        val cursor = db.rawQuery(rawSql, null)

        db.close()

        return cursor
    }

}