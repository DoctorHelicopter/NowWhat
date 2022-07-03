package com.example.nowwhat.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to main
        setContentView(R.layout.activity_main)

        // configure button listener
        val categoriesButton: Button = findViewById(R.id.categoriesButton)
        categoriesButton.setOnClickListener {
            val intent = Intent(this, CategoriesListActivity::class.java)
            startActivity(intent)
        }

        // TODO setup randomizer
    }
}

