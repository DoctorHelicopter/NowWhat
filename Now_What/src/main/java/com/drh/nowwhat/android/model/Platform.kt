package com.drh.nowwhat.android.model

data class Platform(
    override val id: Int,
    override val name: String,
    override val enabled: Boolean,
    override val sort: Int,
    override val favorite: Boolean
): ListItem