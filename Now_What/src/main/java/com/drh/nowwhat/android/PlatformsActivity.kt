package com.drh.nowwhat.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.PlatformsListAdapter
import com.drh.nowwhat.android.callback.PlatformTouchHelper
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.dialog.NewPlatformDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PlatformsActivity : AppCompatActivity(),
    NewPlatformDialog.NewPlatformDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to categories list
        setContentView(R.layout.item_list)
        displayPlatforms()

        // configure new item button listener
        val newCategoryButton: FloatingActionButton = findViewById(R.id.new_item_button)
        newCategoryButton.setOnClickListener {
            val dialog = NewPlatformDialog()
            dialog.show(supportFragmentManager, "NewCategoryDialog")
        }
    }

    private fun displayPlatforms() {
        val db = DBHelper(this, null)
        // initialize data
        val platforms = db.getPlatforms()
        // create view
        val recyclerView = findViewById<RecyclerView>(R.id.item_recycler)
        // for performance, as layout size is fixed
        recyclerView.setHasFixedSize(true)

        val platformHeader = findViewById<TextView>(R.id.item_header_text)
        platformHeader.text = getString(R.string.platform_list_header)
        // attach touch helper for drag/drop and swipe
        PlatformTouchHelper.helper.attachToRecyclerView(recyclerView)
        // configure item adapter
        val editButtonsVisible = intent.extras?.getBoolean("editButtonsVisible") ?: false
        val adapter = PlatformsListAdapter(
            this,
            platforms.toMutableList(),
            editButtonsVisible = editButtonsVisible,
            refreshCallback = ::refreshList
        )
        recyclerView.adapter = adapter

        // configure edit button listener
        val editItemsButton: MaterialButton = findViewById(R.id.toggle_edit_items_button)
        editItemsButton.setOnClickListener {
            // need to toggle edit buttons on each list item
            adapter.toggleEditButtons(recyclerView)
        }
    }

    private fun refreshList() {
        finish()
        overridePendingTransition(0, 0);
        intent.putExtra("editButtonsVisible", true)
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the DialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        displayPlatforms()
    }
}

