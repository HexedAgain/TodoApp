package com.example.meetuptodoapp.model

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

object TodosSerializer: Serializer<Todos> {
    override val defaultValue: Todos
        get() = Todos(todos = listOf())

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): Todos {
        return ProtoBuf { }.decodeFromByteArray(Todos.serializer(), input.readBytes())
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: Todos, output: OutputStream) {
        ProtoBuf { }.encodeToByteArray(Todos.serializer(), t).apply {
            output.write(this)
        }
    }
}