package com.drh.nowwhat.android.model

data class Choice(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val enabled: Boolean
)