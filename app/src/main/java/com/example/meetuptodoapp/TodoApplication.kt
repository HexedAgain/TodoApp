package com.example.meetuptodoapp

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.domain.model.TodosSerializer
//import com.google.firebase.FirebaseApp

class TodoApplication: Application() {
//    val todoDataStore: DataStore<Todos> = TodoStore(this)
    val todoDataStore: TodoStore = TodoStore(this)
    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this)
    }
}