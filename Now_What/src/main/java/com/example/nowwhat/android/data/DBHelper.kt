package com.example.nowwhat.android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import com.example.nowwhat.android.R
import com.example.nowwhat.android.model.Category
import com.example.nowwhat.android.model.Choice

class DBHelper(private val context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        val categoriesQuery = """
            CREATE TABLE $CATEGORIES_TABLE (
              $ID_COL INTEGER PRIMARY KEY,
              $NAME_COL TEXT NOT NULL,
              $ENABLED_COL INTEGER DEFAULT 1
            )
        """.trimIndent()
        val choicesQuery = """
            CREATE TABLE $CHOICES_TABLE (
              $ID_COL INTEGER PRIMARY KEY,
              $CATEGORY_ID_COL INTEGER NOT NULL,
              $NAME_COL TEXT NOT NULL,
              $ENABLED_COL INTEGER DEFAULT 1
            )
        """.trimIndent()

        db.execSQL(categoriesQuery)
        db.execSQL(choicesQuery)
        initDb(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS $CATEGORIES_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $CHOICES_TABLE")
        onCreate(db)
    }

    private fun initDb(db: SQLiteDatabase) {
        // create default categories
        context.resources.getStringArray(R.array.categories).map { c ->
            val values = ContentValues()
            values.put(NAME_COL, c)
            db.insert(CATEGORIES_TABLE, null, values)
        }
    }

    fun addCategory(name: String) {
        val values = ContentValues()
        values.put(NAME_COL, name)

        this.writableDatabase.use { db ->
            db.insert(CATEGORIES_TABLE, null, values)
        }
    }

    fun addChoice(name: String, categoryId: Int) {
        val values = ContentValues()
        values.put(NAME_COL, name)
        values.put(CATEGORY_ID_COL, categoryId)

        this.writableDatabase.use { db ->
            db.insert(CHOICES_TABLE, null, values)
        }
    }

    fun getCategories(): List<Category> {
        this.readableDatabase.use { db ->
            val categories: MutableList<Category> = emptyList<Category>().toMutableList()

            db.rawQuery("SELECT * FROM $CATEGORIES_TABLE", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        val c = Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ENABLED_COL)) == 1
                        )
                        categories.add(c)
                    }
                }
            return categories
        }
    }

    fun getCategoryChoices(categoryId: Int): List<Choice> {
        this.readableDatabase.use { db ->
            val choices: MutableList<Choice> = emptyList<Choice>().toMutableList()

            db.rawQuery("SELECT * FROM $CHOICES_TABLE WHERE $CATEGORY_ID_COL = $categoryId", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        val c = Choice(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID_COL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ENABLED_COL)) == 1
                        )
                        choices.add(c)
                    }
                }
            return choices
        }
    }

    companion object{
        private const val DATABASE_NAME = "NOW_WHAT"
        private const val DATABASE_VERSION = 1

        const val CATEGORIES_TABLE = "categories"
        const val CHOICES_TABLE = "choices"
        const val ID_COL = "id"
        const val CATEGORY_ID_COL = "category_id"
        const val NAME_COL = "name"
        const val ENABLED_COL = "enabled"
    }
}