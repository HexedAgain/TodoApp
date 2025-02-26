package com.example.meetuptodoapp.api

import com.example.meetuptodoapp.domain.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(
    private val todoClient: TodoClient
) {
//    private val client: Ktor
    suspend fun logTodoStats(todoItem: TodoItem, onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            todoClient.post(todoItem, "https://httpbin.org/anything")
            onSuccess()
        }
    }
}