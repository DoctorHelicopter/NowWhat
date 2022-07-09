package com.drh.nowwhat.android.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.*
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Choice
import com.google.android.material.button.MaterialButton

class ChoiceAdapter(
    private val context: Context,
    private var dataset: List<Choice>,
    private var editButtonsVisible: Boolean,
    private val refreshCallback: () -> Unit
) : RecyclerView.Adapter<ChoiceAdapter.ItemViewHolder>(){


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_text)
        val itemToggle: SwitchCompat = view.findViewById(R.id.item_toggle)
        val editItemButton: MaterialButton = view.findViewById(R.id.edit_item_button)
        val deleteItemButton: MaterialButton = view.findViewById(R.id.delete_item_button)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_toggle_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = item.name
        holder.itemToggle.isChecked = item.enabled
        holder.itemToggle.setOnCheckedChangeListener { _, isChecked ->
            toggleItem(item, isChecked)
        }
        if (!editButtonsVisible) { // inverse of toggle case
            holder.editItemButton.visibility = View.INVISIBLE
            holder.deleteItemButton.visibility = View.INVISIBLE
            holder.editItemButton.setOnClickListener {}
            holder.deleteItemButton.setOnClickListener {}
        } else {
            holder.editItemButton.visibility = View.VISIBLE
            holder.deleteItemButton.visibility = View.VISIBLE
            setClickListeners(holder, item, position)
        }
    }

    /**
     * Toggle edit and delete button visibility and click listeners
     */
    fun toggleEditButtons(recyclerView: RecyclerView) {
        (0 until recyclerView.childCount).forEach {
            val view = recyclerView.getChildAt(it)
            val holder = ItemViewHolder(view)
            val position = recyclerView.getChildAdapterPosition(view)
            val choice = dataset[position]
            if (editButtonsVisible) {
                holder.editItemButton.visibility = View.INVISIBLE
                holder.deleteItemButton.visibility = View.INVISIBLE
                holder.editItemButton.setOnClickListener {}
                holder.deleteItemButton.setOnClickListener {}
            } else {
                holder.editItemButton.visibility = View.VISIBLE
                holder.deleteItemButton.visibility = View.VISIBLE
                setClickListeners(holder, choice, position)
            }
        }
        editButtonsVisible = !editButtonsVisible
    }

    /**
     * Enable click listeners for edit and delete buttons
     */
    private fun setClickListeners(holder: ItemViewHolder, choice: Choice, position: Int) {
        holder.editItemButton.setOnClickListener {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(context)
            // Get the layout inflater
            val inputView = LayoutInflater.from(context).inflate(R.layout.text_dialog, null)
            val nameView = inputView.findViewById<TextView>(R.id.text_input_field)
            nameView.text = choice.name
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save updated category
                    db.updateChoice(choice.copy(name = nameView.text.toString()))
                    // re-render choice list
                    refreshCallback()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        holder.deleteItemButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.delete_item, choice.name))
            builder.setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                deleteItem(position)
                // re-render choice list
                refreshCallback()
            }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.show() //show alert dialog
        }
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size

    /**
     * Update the database with new sort value after moving item
     */
    fun moveItem(from: Int, to: Int) {
        val item = dataset[from]
        val db = DBHelper(context, null)

        db.updateChoiceSort(item, to)
        dataset = db.getCategoryChoices(item.categoryId).sortedBy { it.sort }
    }

    /**
     * Update enabled state of category in DB
     */
    private fun toggleItem(choice: Choice, isChecked: Boolean) {
        val db = DBHelper(context, null)
        db.updateChoice(choice.copy(enabled = isChecked))
    }

    /**
     * Remove an item from the DB
     */
    fun deleteItem(position: Int) {
        val item = dataset[position]
        val db = DBHelper(context, null)

        db.deleteChoice(item)
    }
}