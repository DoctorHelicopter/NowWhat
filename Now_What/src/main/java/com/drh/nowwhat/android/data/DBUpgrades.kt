package com.drh.nowwhat.android.data

import android.database.sqlite.SQLiteDatabase

class DBUpgrades(private val db: SQLiteDatabase) {
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
        db.execSQL("""
            ALTER TABLE $CHOICES_TABLE
            ADD COLUMN $PLATFORM_ID_COL INTEGER DEFAULT NULL
        """.trimIndent())
        db.execSQL(PLATFORMS_TABLE_CREATE_SQL)
    }
}