package com.example.meetuptodoapp.model

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.*
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