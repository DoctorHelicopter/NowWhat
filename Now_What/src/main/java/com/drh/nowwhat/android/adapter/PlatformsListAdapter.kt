package com.drh.nowwhat.android.adapter

import android.content.Context
import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.ChoiceActivity
import com.drh.nowwhat.android.R
import com.drh.nowwhat.android.data.DBHelper
import com.drh.nowwhat.android.model.Category
import com.drh.nowwhat.android.model.ListItem
import com.drh.nowwhat.android.model.Platform


class PlatformsListAdapter(
    val context: Context,
    var dataset: MutableList<Platform>,
    private var editButtonsVisible: Boolean,
    private val refreshCallback: () -> Unit
) : RecyclerView.Adapter<ListItemViewHolder>() {

    private val helper = AdapterHelper(context, dataset as MutableList<ListItem>)
    private val db = DBHelper(context, null)

    private val datasetCallback: () -> Unit = {
        dataset = db.getPlatforms().sortedBy { it.sort }.toMutableList()
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
        // set listener for each item click
        // TODO eventually make this open an integration dialog
        // holder.itemView.setOnClickListener {}
        holder.itemToggle.setOnCheckedChangeListener { _, isChecked ->
            helper.toggleItem(item, isChecked)
        }
        helper.setEditButtonVisibility(holder, position, editButtonsVisible, refreshCallback)
        helper.setFavoriteClickListener(holder, position)
        helper.setFavoriteButton(holder, item.favorite)
    }

    fun toggleEditButtons(recyclerView: RecyclerView) {
        editButtonsVisible = !editButtonsVisible
        (0 until recyclerView.childCount).forEach { i ->
            val view = recyclerView.getChildAt(i)
            val holder = ListItemViewHolder(view)
            val position = recyclerView.getChildAdapterPosition(view)
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

        db.updatePlatformSort(item, to)
        datasetCallback()
    }
}