package com.example.meetuptodoapp.storage

//import android.content.SharedPreferences
//import com.example.meetuptodoapp.domain.model.Todos
//import kotlinx.coroutines.delay
//import kotlinx.serialization.json.Json
//
//class TodoSharedPrefs(val sharedPreferences: SharedPreferences) {
//    fun writeTodos(todos: Todos) {
//        val json = Json.encodeToString(Todos)
//        sharedPreferences.edit().putString(TODOS, json).apply()
//    }
//    suspend fun readTodos(): Todos {
//        delay(10000L) // just to add a noticeable delay :P
//        val rawJson = sharedPreferences.getString(TODOS, null) ?: "{\"todos\":[]}"
//        return Json.decodeFromString<Todos>(rawJson)
//    }
//
//    companion object {
//        const val TODOS = "TODOS"
//    }
//}