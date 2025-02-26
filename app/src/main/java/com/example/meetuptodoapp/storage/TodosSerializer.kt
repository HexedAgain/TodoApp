package com.example.meetuptodoapp.storage

import android.util.Log
import androidx.datastore.core.Serializer
import com.example.meetuptodoapp.domain.model.Todos
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.io.InputStream
import java.io.OutputStream

object TodosSerializer: Serializer<Todos> {
    override val defaultValue: Todos
        get() = Todos(todos = listOf())

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): Todos {
        return Json.decodeFromStream(input)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: Todos, output: OutputStream) {
        Log.i("COMMIT", "in writeTo")
        Json.encodeToStream(t, output)
    }

//    @OptIn(ExperimentalSerializationApi::class)
//    override suspend fun readFrom(input: InputStream): Todos {
//        //return ProtoBuf { }.decodeFromByteArray(Todos.serializer(), input.readBytes())
//        return Json.decodeFromStream(input)
//    }
//
//    @OptIn(ExperimentalSerializationApi::class)
//    override suspend fun writeTo(t: Todos, output: OutputStream) {
//        Json.encodeToStream(t, output)
////        ProtoBuf { }.encodeToByteArray(Todos.serializer(), t).apply {
////            output.write(this)
////        }
//    }
}