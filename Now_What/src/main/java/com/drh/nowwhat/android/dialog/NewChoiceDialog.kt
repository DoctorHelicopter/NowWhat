package com.drh.nowwhat.android.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper

class NewChoiceDialog(private val categoryId: Int) : DialogFragment() {
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
    private lateinit var listener: NewChoiceDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NewChoiceDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, categoryId: Int)
    }

    // Override the Fragment.onAttach() method to instantiate the NewChoiceDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NewChoiceDialogListener so we can send events to the host
            listener = context as NewChoiceDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NewChoiceDialogListener"))
        }
    }
}