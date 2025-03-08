package com.example.meetuptodoapp.todos.domain.model

@Serializable
data class TodoItem(
    val id: String,
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val completionTime: Long = -1L,
    val completedTime: Long = Long.MAX_VALUE
)