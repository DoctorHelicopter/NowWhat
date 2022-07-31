package com.drh.nowwhat.android.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.drh.nowwhat.android.model.Choice
import com.drh.nowwhat.android.model.ListItem
import com.google.android.material.button.MaterialButton
import java.io.InvalidObjectException

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder.
// Each data item is just an Affirmation object.
class ListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView = view.findViewById(R.id.item_text)
    val itemToggle: SwitchCompat = view.findViewById(R.id.item_toggle)
    val editItemButton: MaterialButton = view.findViewById(R.id.edit_item_button)
    val deleteItemButton: MaterialButton = view.findViewById(R.id.delete_item_button)
    val favoriteButton: ImageView = view.findViewById(R.id.favorite_icon)
    val constraintLayout: ConstraintLayout = view.findViewById(R.id.item_constraint_layout)
}

class AdapterHelper(val context: Context, private var dataset: MutableList<ListItem>) {
    val db = DBHelper(context, null)
    private var categoryId: Int = -1

    private fun datasetCallback(item: ListItem) {
        dataset = when (item) {
            is Category -> db.getCategories().sortedBy { it.sort }.toMutableList()
            is Choice -> db.getCategoryChoices(categoryId).sortedBy { it.sort }.toMutableList()
            else -> throw InvalidObjectException("Item is neither category nor choice")
        }
    }

    fun setFavoriteButton(holder: ListItemViewHolder, favorite: Boolean) {
        if (favorite) {
            holder.favoriteButton.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_baseline_star_24
                )
            )
        } else {
            holder.favoriteButton.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_baseline_star_border_24
                )
            )
        }
    }

    fun setFavoriteClickListener(
        holder: ListItemViewHolder,
        position: Int
    ) {
        holder.favoriteButton.setOnClickListener {
            val item = dataset[position]
            when (item) {
                is Category -> db.updateCategory(item.copy(favorite = !item.favorite))
                is Choice ->  {
                    db.updateChoice(item.copy(favorite = !item.favorite))
                    categoryId = item.categoryId
                }
                else -> throw InvalidObjectException("Item is neither category nor choice")
            }
            datasetCallback(item)
            setFavoriteButton(holder, !item.favorite)
        }
    }

    fun setEditButtonVisibility(
        holder: ListItemViewHolder,
        position: Int,
        visible: Boolean,
        refreshCallback: () -> Unit
    ) {
        if (!visible) {
            holder.editItemButton.visibility = View.INVISIBLE
            holder.deleteItemButton.visibility = View.INVISIBLE
            holder.favoriteButton.visibility = View.VISIBLE
            // expand text box to cover buttons
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.constraintLayout)
            constraintSet.connect(holder.textView.id, ConstraintSet.END, holder.favoriteButton.id, ConstraintSet.START,8)
            constraintSet.applyTo(holder.constraintLayout)
            holder.editItemButton.setOnClickListener {}
            holder.deleteItemButton.setOnClickListener {}
            setFavoriteClickListener(holder, position)
        } else {
            holder.editItemButton.visibility = View.VISIBLE
            holder.deleteItemButton.visibility = View.VISIBLE
            holder.favoriteButton.visibility = View.INVISIBLE
            // shrink text box to leave room for buttons
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.constraintLayout)
            constraintSet.connect(holder.textView.id, ConstraintSet.END, holder.editItemButton.id, ConstraintSet.START,8)
            constraintSet.applyTo(holder.constraintLayout)
            holder.favoriteButton.setOnClickListener {}
            setEditClickListeners(holder, position, refreshCallback)
        }
    }

    /**
     * Enable click listeners for edit and delete buttons
     */
    private fun setEditClickListeners(
        holder: ListItemViewHolder,
        position: Int,
        refreshCallback: () -> Unit
    ) {
        val item = dataset[position]
        holder.editItemButton.setOnClickListener {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(context)
            // Get the layout inflater
            val inputView = LayoutInflater.from(context).inflate(R.layout.text_dialog, null)
            val nameView = inputView.findViewById<TextView>(R.id.text_input_field)
            nameView.text = item.name
            builder.setView(inputView)
                .setPositiveButton(R.string.save) { _, _ ->
                    when (item) {
                        is Category -> db.updateCategory(item.copy(name = nameView.text.toString()))
                        is Choice -> {
                            db.updateChoice(item.copy(name = nameView.text.toString()))
                            categoryId = item.categoryId
                        }
                        else -> throw InvalidObjectException("Item is neither category nor choice")
                    }
                    // re-render list
                    refreshCallback()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.show()
        }
        holder.deleteItemButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.delete_item, item.name))
            builder.setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                deleteItem(item)
                // re-render category list
                refreshCallback()
            }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.show() //show alert dialog
        }
    }

    /**
     * Update enabled state of category in DB
     */
    fun toggleItem(item: ListItem, isChecked: Boolean) {
        when (item) {
            is Category -> db.updateCategory(item.copy(enabled = isChecked))
            is Choice -> db.updateChoice(item.copy(enabled = isChecked))
            else -> throw InvalidObjectException("Item is neither category nor choice")
        }
    }

    /**
     * Remove an item from the DB
     */
    private fun deleteItem(item: ListItem) {
        when (item) {
            is Category -> db.deleteCategory(item)
            is Choice ->  {
                db.deleteChoice(item)
                categoryId = item.categoryId
            }
            else -> throw InvalidObjectException("Item is neither category nor choice")
        }
        datasetCallback(item)
    }
}