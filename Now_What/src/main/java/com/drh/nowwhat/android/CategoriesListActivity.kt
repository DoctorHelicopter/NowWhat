package com.drh.nowwhat.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.CategoriesListAdapter
import com.drh.nowwhat.android.callback.CategoryTouchHelper
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesListActivity : AppCompatActivity(),
    NewCategoryDialog.NewCategoryDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to categories list
        setContentView(R.layout.item_list)
        displayCategories()

        // configure new item button listener
        val newCategoryButton: FloatingActionButton = findViewById(R.id.newItemButton)
        newCategoryButton.setOnClickListener {
            val dialog = NewCategoryDialog()
            dialog.show(supportFragmentManager, "NewCategoryDialog")
        }
    }

    private fun displayCategories() {
        val db = DBHelper(this, null)
        // initialize data
        val categories = db.getCategories()
        // create view
        val recyclerView = findViewById<RecyclerView>(R.id.item_recycler)
        // for performance, as layout size is fixed
        recyclerView.setHasFixedSize(true)

        val categoryHeader = findViewById<TextView>(R.id.item_header)
        categoryHeader.text = getString(R.string.categories)
        // attach touch helper for drag/drop and swipe
        CategoryTouchHelper { categoryClickListener(it) }.helper.attachToRecyclerView(recyclerView)
        // configure item adapter
        recyclerView.adapter = CategoriesListAdapter(
            this,
            categories,
            clickListener = { categoryClickListener(it) }
        )
    }

    private fun categoryClickListener(category: Category) {
        // set item click listener
        val intent = Intent(this, ChoiceActivity::class.java)
            .putExtra("categoryName", category.name)
            .putExtra("categoryId", category.id)
        startActivity(intent)
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        displayCategories()
    }
}

