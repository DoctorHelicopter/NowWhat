package com.drh.nowwhat.android.callback

import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drh.nowwhat.android.adapter.CategoriesListAdapter
import com.drh.nowwhat.android.adapter.ChoiceAdapter

object ChoiceTouchHelper {
    val helper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    //on move lets you check if an item has been moved from its position either up or down
                    // getting the adapter
                    val adapter = recyclerView.adapter as ChoiceAdapter

                    //the position from where item has been moved
                    val from = viewHolder.adapterPosition

                    //the position where the item is moved
                    val to = target.adapterPosition

                    //telling the adapter to move the item in the DB
                    // TODO something about this is buggy
                    //  moving items to position 1 doesn't always stick properly
                    adapter.moveItem(from, to)
                    //telling the adapter to move the item in the UI
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    //on swipe tells you when an item is swiped left or right from its position ( swipe to delete)
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    //when an item changes its location that is currently selected this funtion is called
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    //when we stop dragging , swiping or moving an item this function is called
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f
                }
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }
}