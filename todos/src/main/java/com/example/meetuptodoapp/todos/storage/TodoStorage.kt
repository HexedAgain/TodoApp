package com.example.meetuptodoapp.todos.storage

import com.example.meetuptodoapp.todos.domain.TodoItem
import com.example.meetuptodoapp.todos.domain.Todos
import kotlinx.coroutines.flow.Flow

class TodoStorage(
    private val todoStore: TodoStore
) {
    fun readAsFlow(): Flow<Todos> {
        return todoStore.data
    }

    suspend fun write(todos: List<TodoItem>): Todos {
        return todoStore.updateData { it.copy(todos = todos) }
    }

    suspend fun setMigrated() {
        todoStore.updateData { it.copy(isMigrated = true) }
    }
}