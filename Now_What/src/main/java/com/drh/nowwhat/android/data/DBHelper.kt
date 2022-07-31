package com.drh.nowwhat.android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.model.Category
import com.drh.nowwhat.android.model.Choice
import java.lang.IllegalArgumentException

class DBHelper(private val context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CATEGORIES_TABLE_CREATE_SQL)
        db.execSQL(CHOICES_TABLE_CREATE_SQL)
        db.execSQL(PLATFORMS_TABLE_CREATE_SQL)

        // only init data if there's none present
        db.rawQuery("SELECT COUNT(*) FROM $CATEGORIES_TABLE", null).use { cursor ->
            cursor.moveToFirst()
            val rowCount = cursor.getInt(0)
            if (rowCount == 0)
                initDb(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        val dbUpgrades = DBUpgrades(db)
        for (v in p1 until p2) { // upgrade one version at a time if catchup is needed
            when (v) {
                // current version -> upgrade to new version
                2 -> dbUpgrades.upgradeFrom2()
                3 -> dbUpgrades.upgradeFrom3()
                // support more migrations as needed
                else -> throw IllegalStateException("Invalid DB migration path: $p1 to $p2")
            }
        }
    }

    private fun initDb(db: SQLiteDatabase) {
        // create default categories
        context.resources.getStringArray(R.array.default_categories).mapIndexed { i, c ->
            val values = ContentValues()
            values.put(NAME_COL, c)
            values.put(SORT_COL, i)
            db.insert(CATEGORIES_TABLE, null, values)
        }
    }

    fun addCategory(name: String) {
        val values = ContentValues()
        values.put(NAME_COL, name)

        // get max sort and increment
        this.readableDatabase.use { db ->
            db.rawQuery("SELECT MAX($SORT_COL) AS $SORT_COL FROM $CATEGORIES_TABLE", null)
                .use { cursor ->
                    cursor.moveToFirst()
                    values.put(SORT_COL, cursor.getInt(cursor.getColumnIndexOrThrow(SORT_COL)) + 1)
                }
        }

        this.writableDatabase.use { db ->
            db.insert(CATEGORIES_TABLE, null, values)
        }
    }

    fun deleteCategory(category: Category) {
        this.writableDatabase.use { db ->
            db.delete(CATEGORIES_TABLE, "$ID_COL = ${category.id}", null)
            db.delete(CHOICES_TABLE, "$CATEGORY_ID_COL = ${category.id}", null)
        }
        reconcileCategorySort()
    }

    fun updateCategory(category: Category) {
        val values = ContentValues()
        values.put(NAME_COL, category.name)
        values.put(ENABLED_COL, category.enabled)
        values.put(FAVORITE_COL, category.favorite)
        this.writableDatabase.use { db ->
            db.update(CATEGORIES_TABLE, values, "$ID_COL = ${category.id}", null)
        }
    }

    fun updateCategorySort(category: Category, newSort: Int) {
        // anything with sort gte new sort gets incremented
        this.writableDatabase.use { db ->
            db.execSQL("UPDATE $CATEGORIES_TABLE SET $SORT_COL = $SORT_COL + 1 WHERE $SORT_COL >= $newSort")
            db.execSQL("UPDATE $CATEGORIES_TABLE SET $SORT_COL = $newSort WHERE $ID_COL = ${category.id}")
        }
        reconcileCategorySort()
    }

    private fun reconcileCategorySort() {
        // shove all sorts down to remove gaps
        val categories = getCategories()
        this.writableDatabase.use { db ->
            categories
                .sortedBy { it.sort }
                .mapIndexed { i, c ->
                    db.execSQL("UPDATE $CATEGORIES_TABLE SET $SORT_COL = $i WHERE $ID_COL = ${c.id}")
                }
        }
    }

    fun getCategory(id: Int): Category {
        var category: Category? = null
        this.readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM $CATEGORIES_TABLE WHERE $ID_COL = $id", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        category = Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ENABLED_COL)) == 1,
                            cursor.getInt(cursor.getColumnIndexOrThrow(SORT_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITE_COL)) == 1
                        )
                    }
                }
        }
        return category ?: throw IllegalArgumentException("Invalid category ID")
    }

    fun getCategories(): List<Category> {
        val categories: MutableList<Category> = emptyList<Category>().toMutableList()
        this.readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM $CATEGORIES_TABLE ORDER BY $SORT_COL", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        val c = Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ENABLED_COL)) == 1,
                            cursor.getInt(cursor.getColumnIndexOrThrow(SORT_COL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITE_COL)) == 1
                        )
                        categories.add(c)
                    }
                }
        }
        return categories
    }

    fun addChoice(name: String, categoryId: Int) {
        val values = ContentValues()
        values.put(NAME_COL, name)
        values.put(CATEGORY_ID_COL, categoryId)

        // get max sort and increment
        this.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT MAX($SORT_COL) AS $SORT_COL FROM $CHOICES_TABLE WHERE $CATEGORY_ID_COL = $categoryId",
                null
            ).use { cursor ->
                cursor.moveToFirst()
                values.put(SORT_COL, cursor.getInt(cursor.getColumnIndexOrThrow(SORT_COL)) + 1)
            }
        }

        this.writableDatabase.use { db ->
            db.insert(CHOICES_TABLE, null, values)
        }
    }

    fun deleteChoice(choice: Choice) {
        this.writableDatabase.use { db ->
            db.delete(CHOICES_TABLE, "$ID_COL = ${choice.id}", null)
        }
        reconcileChoiceSort(choice.categoryId)
    }

    fun updateChoice(choice: Choice) {
        val values = ContentValues()
        values.put(NAME_COL, choice.name)
        values.put(ENABLED_COL, choice.enabled)
        values.put(FAVORITE_COL, choice.favorite)
        this.writableDatabase.use { db ->
            db.update(CHOICES_TABLE, values, "$ID_COL = ${choice.id}", null)
        }
    }

    fun updateChoiceSort(choice: Choice, newSort: Int) {
        // anything with sort gte new sort gets incremented
        this.writableDatabase.use { db ->
            db.execSQL("UPDATE $CHOICES_TABLE SET $SORT_COL = $SORT_COL + 1 WHERE $CATEGORY_ID_COL = ${choice.categoryId} AND $SORT_COL >= $newSort")
            db.execSQL("UPDATE $CHOICES_TABLE SET $SORT_COL = $newSort WHERE $ID_COL = ${choice.id}")
        }
        reconcileChoiceSort(choice.categoryId)
    }

    private fun reconcileChoiceSort(categoryId: Int) {
        // shove all sorts down to remove gaps
        val choices = getCategoryChoices(categoryId)
        this.writableDatabase.use { db ->
            choices
                .sortedBy { it.sort }
                .mapIndexed { i, c ->
                    db.execSQL("UPDATE $CHOICES_TABLE SET $SORT_COL = $i WHERE $ID_COL = ${c.id}")
                }
        }
    }

    fun getCategoryChoices(categoryId: Int): List<Choice> {
        val choices: MutableList<Choice> = emptyList<Choice>().toMutableList()
        this.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT * FROM $CHOICES_TABLE WHERE $CATEGORY_ID_COL = $categoryId ORDER BY $SORT_COL",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val c = Choice(
                        cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ENABLED_COL)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(SORT_COL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITE_COL)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID_COL))
                    )
                    choices.add(c)
                }
            }
        }
        return choices
    }

    fun getEnabledCategoriesWithChoices(): List<Category> {
        val pairs: MutableList<Pair<Category, Choice>> = emptyList<Pair<Category, Choice>>().toMutableList()
        this.readableDatabase.use { db ->
            db.rawQuery(
                """
                SELECT
                    ca.$NAME_COL AS $CATEGORY,
                    ca.$SORT_COL AS $CATEGORY$SORT_COL,
                    ca.$FAVORITE_COL AS $CATEGORY$FAVORITE_COL,
                    ch.$NAME_COL AS $CHOICE,
                    ch.$SORT_COL AS $CHOICE$SORT_COL,
                    ch.$FAVORITE_COL AS $CHOICE$FAVORITE_COL,
                    ch.$CATEGORY_ID_COL,
                    ch.$ID_COL
                FROM $CATEGORIES_TABLE ca 
                INNER JOIN $CHOICES_TABLE ch ON ca.$ID_COL = ch.$CATEGORY_ID_COL
                WHERE ca.$ENABLED_COL = 1 AND ch.$ENABLED_COL = 1
                """.trimIndent(),
            null
            ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY))
                        val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID_COL))
                        val categorySort = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY+SORT_COL))
                        val categoryFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY+FAVORITE_COL)) == 1
                        val choiceName = cursor.getString(cursor.getColumnIndexOrThrow(CHOICE))
                        val choiceId = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL))
                        val choiceSort = cursor.getInt(cursor.getColumnIndexOrThrow(CHOICE+SORT_COL))
                        val choiceFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(CHOICE+FAVORITE_COL)) == 1
                        val category = Category(categoryId, categoryName, true, categorySort, categoryFavorite, emptyList())
                        val choice = Choice(choiceId, choiceName, true, choiceSort, choiceFavorite, categoryId)
                        pairs += Pair(category, choice)
                    }
                }
        }
        val mapper = pairs
            .groupBy { it.first }
            .map { (category, items) -> category to items.map { it.second } }
        return mapper.map { (category, choices) -> category.copy(choices = choices) }
    }

    companion object {
        private const val DATABASE_NAME = "NOW_WHAT"
        private const val DATABASE_VERSION = 4
    }
}