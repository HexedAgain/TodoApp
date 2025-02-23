package com.example.meetuptodoapp.api

import android.net.http.HttpEngine
import android.util.Log
import com.example.meetuptodoapp.domain.model.TodoItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

class TodoClient {
    suspend fun <T: Any> post(postBody: T, url: String) {
        client.post<TodoItem>(url) {
            body = postBody
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        val x = 5
    }

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })

            engine {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }
        }

        //Http Response
        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP status:", "${response.status.value}")
            }
        }

        // Headers
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}