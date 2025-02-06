package com.example.meetuptodoapp.domain.model

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.uuid.Uuid

@Serializable
data class TodoItem(
    val id: String? = UUID.randomUUID().toString(), // how can we reliably set this at construction time?
    val title: String? = null,
    val description: String? = null,
    val timestamp: Long = 0L,
    val completionTime: Long = Long.MAX_VALUE
) {
    companion object {
        fun default(): TodoItem {
            return TodoItem(
                timestamp = Instant.now().toEpochMilli()
            )
        }
    }
}