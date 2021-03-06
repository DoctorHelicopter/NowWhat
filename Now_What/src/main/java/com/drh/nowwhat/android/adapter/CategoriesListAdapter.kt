package com.drh.nowwhat.android.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.CategoriesListActivity
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.google.android.material.button.MaterialButton


class CategoriesListAdapter(
    val context: Context,
    var dataset: List<Category>,
    private val clickListener: (Category) -> Unit,
    private var editButtonsVisible: Boolean,
    private val refreshCallback: () -> Unit
) : RecyclerView.Adapter<CategoriesListAdapter.ItemViewHolder>() {

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
        // set listener for each item click
        holder.itemView.setOnClickListener { clickListener(item) }
        holder.itemToggle.setOnCheckedChangeListener { _, isChecked ->
            toggleItem(item, isChecked)
        }
        if (!editButtonsVisible) {
            holder.editItemButton.visibility = INVISIBLE
            holder.deleteItemButton.visibility = INVISIBLE
            holder.editItemButton.setOnClickListener {}
            holder.deleteItemButton.setOnClickListener {}
        } else {
            holder.editItemButton.visibility = VISIBLE
            holder.deleteItemButton.visibility = VISIBLE
            setClickListeners(holder, item, position)
        }
    }

    fun toggleEditButtons(recyclerView: RecyclerView) {
        (0 until recyclerView.childCount).forEach {
            val view = recyclerView.getChildAt(it)
            val holder = ItemViewHolder(view)
            val position = recyclerView.getChildAdapterPosition(view)
            val category = dataset[position]
            if (editButtonsVisible) {
                holder.editItemButton.visibility = INVISIBLE
                holder.deleteItemButton.visibility = INVISIBLE
                holder.editItemButton.setOnClickListener {}
                holder.deleteItemButton.setOnClickListener {}
            } else {
                holder.editItemButton.visibility = VISIBLE
                holder.deleteItemButton.visibility = VISIBLE
                setClickListeners(holder, category, position)
            }
        }
        editButtonsVisible = !editButtonsVisible
    }

    /**
     * Enable click listeners for edit and delete buttons
     */
    private fun setClickListeners(holder: ItemViewHolder, category: Category, position: Int) {
        holder.editItemButton.setOnClickListener {
            val db = DBHelper(context, null)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(context)
            // Get the layout inflater
            val inputView = LayoutInflater.from(context).inflate(R.layout.text_dialog, null)
            val nameView = inputView.findViewById<TextView>(R.id.text_input_field)
            nameView.text = category.name
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    // save updated category
                    db.updateCategory(category.copy(name = nameView.text.toString()))
                    // re-render category list
                    refreshCallback()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.show()
        }
        holder.deleteItemButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.delete_item, category.name))
            builder.setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                deleteItem(position)
                // re-render category list
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

        db.updateCategorySort(item, to)
        dataset = db.getCategories().sortedBy { it.sort }
    }

    /**
     * Update enabled state of category in DB
     */
    private fun toggleItem(category: Category, isChecked: Boolean) {
        val db = DBHelper(context, null)
        db.updateCategory(category.copy(enabled = isChecked))
    }

    /**
     * Remove an item from the DB
     */
    fun deleteItem(position: Int) {
        val item = dataset[position]
        val db = DBHelper(context, null)

        db.deleteCategory(item)
        dataset = db.getCategories().sortedBy { it.sort }
    }
}