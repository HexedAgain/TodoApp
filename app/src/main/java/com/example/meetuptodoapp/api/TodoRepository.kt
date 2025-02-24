package com.example.meetuptodoapp.api

import com.example.meetuptodoapp.domain.model.TodoItem

class TodoRepository(
    private val todoClient: TodoClient
) {
//    private val client: Ktor
    suspend fun logTodoStats(todoItem: TodoItem, onSuccess: () -> Unit) {
        todoClient.post(todoItem, "https://httpbin.org/anything")
        onSuccess()
    }
}