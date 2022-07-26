package com.drh.nowwhat.android.model

data class Category(
    override val id: Int,
    override val name: String,
    override val enabled: Boolean,
    override val sort: Int,
    override val favorite: Boolean,
    val choices: List<Choice> = emptyList()
): ListItem