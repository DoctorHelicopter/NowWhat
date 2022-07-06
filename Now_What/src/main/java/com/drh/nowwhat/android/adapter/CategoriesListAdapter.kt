package com.drh.nowwhat.android.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.CategoriesListActivity
import com.drh.nowwhat.android.EditCategoryDialog
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.google.android.material.button.MaterialButton


class CategoriesListAdapter(
    val context: Context,
    var dataset: List<Category>,
    private val clickListener: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesListAdapter.ItemViewHolder>() {

    private var editButtonsVisible: Boolean = false

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemText)
        val itemToggle: SwitchCompat = view.findViewById(R.id.itemToggle)
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
    }

    fun toggleEditButtons(recyclerView: RecyclerView) {
        (0 until recyclerView.childCount).forEach {
            val view = recyclerView.getChildAt(it)
            val position = recyclerView.getChildAdapterPosition(view)
            val category = dataset[position]
            val editItemButton: MaterialButton = view.findViewById(R.id.edit_item_button)
            val deleteItemButton: MaterialButton = view.findViewById(R.id.delete_item_button)
            if (editButtonsVisible) {
                editItemButton.visibility = INVISIBLE
                editItemButton.setOnClickListener {}
                deleteItemButton.visibility = INVISIBLE
                deleteItemButton.setOnClickListener {}
            } else {
                editItemButton.visibility = VISIBLE
                editItemButton.setOnClickListener {
                    val dialog = EditCategoryDialog(category)
                    context as FragmentActivity
                    dialog.show(context.supportFragmentManager, "EditCategoryDialog")
                }
                deleteItemButton.visibility = VISIBLE
                deleteItemButton.setOnClickListener {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setMessage(context.getString(R.string.delete_item, category.name))
                    builder.setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                        deleteItem(position)
                        // re-render category list
                        val intent = Intent(context, CategoriesListActivity::class.java)
                        context.startActivity(intent)
                    }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }.show() //show alert dialog
                }
            }
        }
        editButtonsVisible = !editButtonsVisible
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