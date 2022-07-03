package com.example.nowwhat.android

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.nowwhat.android.data.DBHelper

class NewItemDialog(private val categoryId: Int) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = this.context ?: throw IllegalStateException("Context cannot be null")
        return activity?.let {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val inputView = inflater.inflate(R.layout.new_item, null)
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save new category
                    val nameView = inputView.findViewById<TextView>(R.id.newItemName)
                    db.addChoice(nameView.text.toString(), categoryId)
                    listener.onDialogPositiveClick(this, categoryId)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Use this instance of the interface to deliver action events
    internal lateinit var listener: NoticeDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, categoryId: Int)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}