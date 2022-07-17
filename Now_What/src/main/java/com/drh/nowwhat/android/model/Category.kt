package com.drh.nowwhat.android.model

data class Category(
    val id: Int,
    val name: String,
    val enabled: Boolean,
    val sort: Int,
    val favorite: Boolean,
    val choices: List<Choice> = emptyList()
)