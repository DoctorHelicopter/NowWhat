package com.drh.nowwhat.android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.drh.nowwhat.android.R

class DBUpgrades(private val db: SQLiteDatabase, private val context: Context) {
    // add favorite column
    fun upgradeFrom2() {
        db.execSQL("""
            ALTER TABLE $CATEGORIES_TABLE
            ADD COLUMN $FAVORITE_COL INTEGER DEFAULT 0
        """.trimIndent())
        db.execSQL("""
            ALTER TABLE $CHOICES_TABLE
            ADD COLUMN $FAVORITE_COL INTEGER DEFAULT 0
        """.trimIndent())
    }
    // add platforms table and foreign key
    fun upgradeFrom3() {
        db.execSQL(PLATFORMS_TABLE_CREATE_SQL)
        context.resources.getStringArray(R.array.default_platforms).mapIndexed { i, p ->
            val values = ContentValues()
            values.put(NAME_COL, p)
            values.put(SORT_COL, i)
            db.insert(PLATFORMS_TABLE, null, values)
        }
        db.execSQL("""
            ALTER TABLE $CHOICES_TABLE
            ADD COLUMN $PLATFORM_ID_COL INTEGER DEFAULT 0
        """.trimIndent())
    }
}