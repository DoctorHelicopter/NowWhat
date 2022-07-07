package com.drh.nowwhat.android

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.drh.nowwhat.android.data.DBHelper

class NewCategoryDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = this.context ?: throw IllegalStateException("Context cannot be null")
        return activity?.let {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val inputView = inflater.inflate(R.layout.text_dialog, null)
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save new category
                    val nameView = inputView.findViewById<TextView>(R.id.text_input_field)
                    db.addCategory(nameView.text.toString())
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Use this instance of the interface to deliver action events
    private lateinit var listener: NewCategoryDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the NewCategoryDialogListener in case the host needs to query it. */
    interface NewCategoryDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NewCategoryDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NewCategoryDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NewCategoryDialogListener"))
        }
    }
}