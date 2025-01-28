package com.example.meetuptodoapp.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

val Context.todoDatastore: DataStore<Todos> by dataStore(
    fileName = "TODOS",
    serializer = TodosSerializer
)