package com.drh.nowwhat.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Choice

class ChoiceAdapter(
    private val context: Context, // TODO use for translations or something
    private var dataset: List<Choice>
) : RecyclerView.Adapter<ChoiceAdapter.ItemViewHolder>(){

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemText)
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
     * Remove an item from the DB
     */
    fun deleteItem(position: Int) {
        val item = dataset[position]
        val db = DBHelper(context, null)

        db.deleteChoice(item)
    }
}