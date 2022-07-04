package com.drh.nowwhat.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import com.drh.nowwhat.android.data.DBHelper
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DBHelper(this, null)

        // set view to main
        setContentView(R.layout.activity_main)

        // configure button listener
        val categoriesButton: Button = findViewById(R.id.categoriesButton)
        categoriesButton.setOnClickListener {
            val intent = Intent(this, CategoriesListActivity::class.java)
            startActivity(intent)
        }

        // configure randomizer
        val randomizerButton: Button = findViewById(R.id.randomizerButton)
        randomizerButton.setOnClickListener {
            val categories = db.getEnabledCategoriesWithChoices()
            val categoryView: TextView = findViewById(R.id.selectedCategory)
            val choiceView: TextView = findViewById(R.id.selectedChoice)
            if (categories.isEmpty()) {
                categoryView.text = getString(R.string.no_categories_error)
                categoryView.visibility = VISIBLE
            } else {
                val r = Random
                val selectedCategory = categories[r.nextInt(categories.size)]
                val choices = selectedCategory.choices
                val selectedChoice = choices[r.nextInt(choices.size)]

                categoryView.text = selectedCategory.name
                choiceView.text = selectedChoice.name
                categoryView.visibility = VISIBLE
                choiceView.visibility = VISIBLE
            }
        }
    }
}

