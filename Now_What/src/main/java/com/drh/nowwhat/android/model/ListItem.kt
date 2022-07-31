package com.drh.nowwhat.android.model

interface ListItem {
    val id: Int
    val name: String
    val enabled: Boolean
    val sort: Int
    val favorite: Boolean
}