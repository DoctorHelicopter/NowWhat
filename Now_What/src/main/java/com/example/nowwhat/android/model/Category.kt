package com.example.nowwhat.android.model

data class Category(
    val id: Int,
    val name: String,
    val enabled: Boolean,
    val choices: List<Choice> = emptyList()
)