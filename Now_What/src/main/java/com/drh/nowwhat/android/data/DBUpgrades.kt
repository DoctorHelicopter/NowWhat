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
}