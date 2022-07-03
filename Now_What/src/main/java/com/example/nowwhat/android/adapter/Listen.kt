package com.example.nowwhat.android.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun <T : RecyclerView.ViewHolder> T.listen(event: (category: View) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(this.itemView)
    }
    return this
}