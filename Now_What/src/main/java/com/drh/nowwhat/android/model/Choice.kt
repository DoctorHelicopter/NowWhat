package com.drh.nowwhat.android.model

data class Choice(
    override val id: Int,
    override val name: String,
    override val enabled: Boolean,
    override val sort: Int,
    override val favorite: Boolean,
    val categoryId: Int
): ListItem