package com.example.meetuptodoapp.api

import com.example.meetuptodoapp.domain.model.TodoItem
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class TodoRepository(
) {
    private val todoClient = TodoClient()
//    private val client: Ktor
    suspend fun logTodoStats(todoItem: TodoItem, onSuccess: () -> Unit) {
        todoClient.post(todoItem, "https://httpbin.org/anything")
//        val response = todoClient.client.post<TodoItem>("https://httpbin.org/anything") {
//            body = todoItem
//            header(HttpHeaders.ContentType, ContentType.Application.Json)
//        }
        onSuccess()
    }
}