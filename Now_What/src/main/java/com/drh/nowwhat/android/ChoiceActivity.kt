package com.drh.nowwhat.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.ChoiceAdapter
import com.drh.nowwhat.android.callback.ChoiceTouchHelper
import com.drh.nowwhat.android.data.DBHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChoiceActivity : AppCompatActivity(),
    NewChoiceDialog.NoticeDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to category content
        setContentView(R.layout.category_content)
        val categoryId = intent.extras?.getInt("categoryId")!! // TODO better null safety
        displayItems(categoryId)

        // configure button listener
        val newChoiceButton: FloatingActionButton = findViewById(R.id.newItemButton)
        newChoiceButton.setOnClickListener {
            val dialog = NewChoiceDialog(categoryId)
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

        // attach touch helper for drag/drop and swipe
        ChoiceTouchHelper.helper.attachToRecyclerView(recyclerView)
        // attach item adapter
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

