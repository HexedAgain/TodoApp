package com.example.meetuptodoapp.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val title: String
)