package com.example.meetuptodoapp.todos.domain.model

@Serializable
data class Todos(
    val isMigrated: Boolean = false,
    val todos: List<TodoItem>
) {
    companion object {
        fun default(): Todos {
            return Todos(todos = listOf())
        }
    }
}