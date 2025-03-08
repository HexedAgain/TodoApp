package com.example.meetuptodoapp.todos.storage

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.example.meetuptodoapp.todos.domain.Todos
import kotlinx.coroutines.flow.Flow
import java.io.File

//val Context.todoDatastore: DataStore<Todos> by dataStore(
//    fileName = "TODOS",
//    serializer = TodosSerializer
//)

class TodoStore(val context: Context): DataStore<Todos> {
    val impl = DataStoreFactory.create(

        serializer = TodosSerializer,
        produceFile = { File(context.filesDir, "datastore/TODOS") }
    )
    override val data: Flow<Todos>
        get() = impl.data

    override suspend fun updateData(transform: suspend (t: Todos) -> Todos): Todos {
        Log.i("COMMIT", "inside updateData")
        return impl.updateData { transform(it) }
    }
}