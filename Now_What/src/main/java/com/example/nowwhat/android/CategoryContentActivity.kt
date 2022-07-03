package com.example.nowwhat.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.nowwhat.android.adapter.ChoiceAdapter
import com.example.nowwhat.android.data.DBHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoryContentActivity : AppCompatActivity(),
    NewItemDialog.NoticeDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to category content
        setContentView(R.layout.category_content)
        val categoryId = intent.extras?.getInt("categoryId")!! // TODO better null safety
        displayItems(categoryId)

        // configure button listener
        val newCategoryButton: FloatingActionButton = findViewById(R.id.newItemButton)
        newCategoryButton.setOnClickListener {
            val dialog = NewItemDialog(categoryId)
            dialog.show(supportFragmentManager, "NewItemDialog")
        }
    }

    private fun displayItems(categoryId: Int) {
        val db = DBHelper(this, null)
        val choices = db.getCategoryChoices(categoryId)
        // create view
        val recyclerView = findViewById<RecyclerView>(R.id.category_content)
        // for performance, as layout size is fixed
        recyclerView.setHasFixedSize(true)
        // link dataset to recycler
        val categoryHeader = findViewById<TextView>(R.id.categoryHeader)
        categoryHeader.text = intent.extras?.getString("categoryName")
        recyclerView.adapter = ChoiceAdapter(this, choices)
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment, categoryId: Int) {
        // User touched the dialog's positive button
        displayItems(categoryId)
    }
}

