package com.example.meetuptodoapp

import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
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
}