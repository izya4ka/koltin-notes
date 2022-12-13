package com.gd.database.tasks

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val label: String,
    val text: String?,
)

@Serializable
data class RawTask(
    val label: String,
    val text: String?,
)