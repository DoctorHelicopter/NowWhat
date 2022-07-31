package com.drh.nowwhat.android.data

// tables
const val CATEGORIES_TABLE = "categories"
const val CHOICES_TABLE = "choices"
const val PLATFORMS_TABLE = "platforms"

// ID columns
const val ID_COL = "id"
const val CATEGORY_ID_COL = "category_id"
const val PLATFORM_ID_COL = "platform_id"

// other columns
const val NAME_COL = "name"
const val ENABLED_COL = "enabled"
const val SORT_COL = "sort"
const val FAVORITE_COL = "favorite"

// prefixes
const val CATEGORY = "category"
const val CHOICE = "choice"

// table create statements
val CATEGORIES_TABLE_CREATE_SQL = """
    CREATE TABLE IF NOT EXISTS $CATEGORIES_TABLE  (
        $ID_COL INTEGER PRIMARY KEY,
        $NAME_COL TEXT NOT NULL,
        $ENABLED_COL INTEGER DEFAULT 1,
        $SORT_COL INTEGER DEFAULT 0,
        $FAVORITE_COL INTEGER DEFAULT 0
    )
""".trimIndent()
val CHOICES_TABLE_CREATE_SQL = """
    CREATE TABLE IF NOT EXISTS $CHOICES_TABLE (
        $ID_COL INTEGER PRIMARY KEY,
        $CATEGORY_ID_COL INTEGER NOT NULL,
        $PLATFORM_ID_COL INTEGER DEFAULT NULL,
        $NAME_COL TEXT NOT NULL,
        $ENABLED_COL INTEGER DEFAULT 1,
        $SORT_COL INTEGER DEFAULT 0,
        $FAVORITE_COL INTEGER DEFAULT 0
    )
""".trimIndent()
val PLATFORMS_TABLE_CREATE_SQL = """
    CREATE TABLE IF NOT EXISTS $PLATFORMS_TABLE (
        $ID_COL INTEGER PRIMARY KEY,
        $NAME_COL TEXT NOT NULL,
        $ENABLED_COL INTEGER DEFAULT 1,
        $SORT_COL INTEGER DEFAULT 0
    )
""".trimIndent()
