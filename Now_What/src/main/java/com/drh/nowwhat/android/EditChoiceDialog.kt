package com.drh.nowwhat.android

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.drh.nowwhat.android.model.Choice

class EditChoiceDialog(private val choice: Choice) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = this.context ?: throw IllegalStateException("Context cannot be null")
        return activity?.let {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val inputView = inflater.inflate(R.layout.new_item, null)
            val nameView = inputView.findViewById<TextView>(R.id.newItemName)
            nameView.text = choice.name
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save updated category
                    db.updateChoice(choice.copy(name = nameView.text.toString()))
                    val category = db.getCategory(choice.categoryId)
                    // re-render category list
                    val intent = Intent(context, ChoiceActivity::class.java)
                        .putExtra("categoryName", category.name)
                        .putExtra("categoryId", category.id)
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