package com.drh.nowwhat.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.CategoriesListAdapter
import com.drh.nowwhat.android.data.DBHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesListActivity : AppCompatActivity(),
    NewCategoryDialog.NoticeDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to categories list
        setContentView(R.layout.categories_list)
        displayCategories()

        // configure new item button listener
        val newCategoryButton: FloatingActionButton = findViewById(R.id.newCategoryButton)
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
        val recyclerView = findViewById<RecyclerView>(R.id.categories_list)
        // for performance, as layout size is fixed
        recyclerView.setHasFixedSize(true)

        // configure item adapter
        recyclerView.adapter = CategoriesListAdapter(this, categories) {
            // set item click listener
            val intent = Intent(this, CategoryContentActivity::class.java)
                .putExtra("categoryName", it.name)
                .putExtra("categoryId", it.id)
            startActivity(intent)
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        displayCategories()
    }
}

