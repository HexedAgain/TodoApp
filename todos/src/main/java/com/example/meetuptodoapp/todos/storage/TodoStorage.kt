package com.example.meetuptodoapp.todos.storage

import com.example.meetuptodoapp.todos.domain.model.TodoItem
import com.example.meetuptodoapp.todos.domain.model.Todos
import kotlinx.coroutines.flow.Flow

class TodoStorage(
    private val todoStore: TodoStore
) {
    fun readAsFlow(): Flow<com.example.meetuptodoapp.todos.domain.model.Todos> {
        return todoStore.data
    }

    suspend fun write(todos: List<com.example.meetuptodoapp.todos.domain.model.TodoItem>): com.example.meetuptodoapp.todos.domain.model.Todos {
        return todoStore.updateData { it.copy(todos = todos) }
    }

    suspend fun setMigrated() {
        todoStore.updateData { it.copy(isMigrated = true) }
    }
}