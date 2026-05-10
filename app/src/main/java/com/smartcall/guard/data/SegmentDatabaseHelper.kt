package com.smartcall.guard.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.smartcall.guard.data.entity.Segment
import java.io.FileOutputStream

class SegmentDatabaseHelper {

    private var db: SQLiteDatabase? = null
    private var dbPath: String? = null

    companion object {
        private const val DB_NAME = "phone_segments.db"
    }

    fun copyDatabaseIfNeeded(context: Context) {
        val path = context.getDatabasePath(DB_NAME)
        dbPath = path.absolutePath
        if (!path.exists()) {
            path.parentFile?.mkdirs()
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(path).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun openDatabase(): SQLiteDatabase {
        return db ?: SQLiteDatabase.openDatabase(
            dbPath!!,
            null,
            SQLiteDatabase.OPEN_READONLY
        ).also { db = it }
    }

    fun querySegment(prefix: String): Segment? {
        val database = openDatabase()
        val cursor = database.rawQuery(
            "SELECT * FROM segments WHERE prefix = ?",
            arrayOf(prefix)
        )
        return cursor.use { c ->
            if (c.moveToFirst()) {
                cursorToSegment(c)
            } else {
                null
            }
        }
    }

    fun querySegmentWithRetry(prefix: String): Segment? {
        val database = openDatabase()

        val prefixes = listOfNotNull(
            prefix.take(7),
            prefix.take(5).takeIf { it.length >= 3 },
            prefix.take(3).takeIf { it.isNotEmpty() }
        ).distinct()

        for (p in prefixes) {
            val cursor = database.rawQuery(
                "SELECT * FROM segments WHERE prefix = ?",
                arrayOf(p)
            )
            cursor.use { c ->
                if (c.moveToFirst()) {
                    return cursorToSegment(c)
                }
            }
        }

        return null
    }

    fun querySegmentsByPrefix(prefixMatch: String): List<Segment> {
        val database = openDatabase()
        val cursor = database.rawQuery(
            "SELECT * FROM segments WHERE prefix LIKE ?",
            arrayOf("$prefixMatch%")
        )
        val results = mutableListOf<Segment>()
        cursor.use { c ->
            while (c.moveToNext()) {
                results.add(cursorToSegment(c))
            }
        }
        return results
    }

    fun close() {
        db?.close()
        db = null
    }

    private fun cursorToSegment(cursor: android.database.Cursor): Segment {
        val operatorIndex = cursor.getColumnIndexOrThrow("operator")
        val areaCodeIndex = cursor.getColumnIndexOrThrow("area_code")
        return Segment(
            prefix = cursor.getString(cursor.getColumnIndexOrThrow("prefix")),
            province = cursor.getString(cursor.getColumnIndexOrThrow("province")),
            city = cursor.getString(cursor.getColumnIndexOrThrow("city")),
            operator = if (cursor.isNull(operatorIndex)) null else cursor.getString(operatorIndex),
            areaCode = if (cursor.isNull(areaCodeIndex)) null else cursor.getString(areaCodeIndex)
        )
    }
}
