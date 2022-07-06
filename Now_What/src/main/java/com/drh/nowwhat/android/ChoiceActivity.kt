package com.drh.nowwhat.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.ChoiceAdapter
import com.drh.nowwhat.android.callback.ChoiceTouchHelper
import com.drh.nowwhat.android.data.DBHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChoiceActivity : AppCompatActivity(),
    NewChoiceDialog.NewChoiceDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to category content
        setContentView(R.layout.item_list)
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
        val category = db.getCategory(categoryId)
        // create view
        val recyclerView = findViewById<RecyclerView>(R.id.item_recycler)
        // for performance, as layout size is fixed
        recyclerView.setHasFixedSize(true)
        // link dataset to recycler
        val categoryHeader = findViewById<TextView>(R.id.item_header_text)
        categoryHeader.text = intent.extras?.getString("categoryName")

        // attach touch helper for drag/drop and swipe
        ChoiceTouchHelper.helper.attachToRecyclerView(recyclerView)
        // attach item adapter
        val adapter = ChoiceAdapter(this, choices)
        recyclerView.adapter = adapter

        // configure edit button listener
        val editItemsButton: MaterialButton = findViewById(R.id.toggle_edit_items_button)
        editItemsButton.setOnClickListener {
            // need to toggle edit buttons on each list item
            adapter.toggleEditButtons(recyclerView, category)
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment, categoryId: Int) {
        // User touched the dialog's positive button
        displayItems(categoryId)
    }
}

