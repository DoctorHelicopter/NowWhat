package com.drh.nowwhat.android

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category

class EditCategoryDialog(private val category: Category) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = this.context ?: throw IllegalStateException("Context cannot be null")
        return activity?.let {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val inputView = inflater.inflate(R.layout.text_dialog, null)
            val nameView = inputView.findViewById<TextView>(R.id.text_input_field)
            nameView.text = category.name
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save updated category
                    db.updateCategory(category.copy(name = nameView.text.toString()))
                    // re-render category list
                    val intent = Intent(context, CategoriesListActivity::class.java)
                    context.startActivity(intent)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}