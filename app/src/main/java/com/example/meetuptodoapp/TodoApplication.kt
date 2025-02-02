package com.example.meetuptodoapp

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.example.meetuptodoapp.model.TodoStore
import com.example.meetuptodoapp.model.Todos
import com.example.meetuptodoapp.model.TodosSerializer

class TodoApplication: Application() {
    val todoDataStore: DataStore<Todos> = TodoStore(this)
}