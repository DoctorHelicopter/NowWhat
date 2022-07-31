package com.drh.nowwhat.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.*
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Choice
import com.drh.nowwhat.android.model.ListItem

class ChoiceAdapter(
    context: Context,
    private var dataset: MutableList<Choice>,
    private var editButtonsVisible: Boolean,
    private val refreshCallback: () -> Unit
) : RecyclerView.Adapter<ListItemViewHolder>(){

    private val helper = AdapterHelper(context, dataset as MutableList<ListItem>)
    private val db = DBHelper(context, null)
    private var currentCategoryId: Int = -1

    private val datasetCallback: () -> Unit = { 
        dataset = db.getCategoryChoices(currentCategoryId).sortedBy { c -> c.sort }.toMutableList() 
    }
    
    private val currentItem: (Int) -> ListItem = { dataset[it] }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_toggle_item, parent, false)
        return ListItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = item.name
        holder.itemToggle.isChecked = item.enabled
        holder.itemToggle.setOnCheckedChangeListener { _, isChecked ->
            helper.toggleItem(item, isChecked)
        }
        currentCategoryId = item.categoryId
        helper.setEditButtonVisibility(holder, position, editButtonsVisible, refreshCallback,)
        helper.setFavoriteClickListener(holder, position)
        helper.setFavoriteButton(holder, item.favorite)
    }

    fun toggleEditButtons(recyclerView: RecyclerView) {
        editButtonsVisible = !editButtonsVisible
        (0 until recyclerView.childCount).forEach {
            val view = recyclerView.getChildAt(it)
            val holder = ListItemViewHolder(view)
            val position = recyclerView.getChildAdapterPosition(view)
            val choice = dataset[position]
            currentCategoryId = choice.categoryId
            helper.setEditButtonVisibility(holder, position, editButtonsVisible, refreshCallback)
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

        db.updateChoiceSort(item, to)
        currentCategoryId = item.categoryId
        datasetCallback()
    }
}