package com.example.meetuptodoapp.domain.model

//import com.example.meetuptodoapp.ui.model.UITodo
//import com.example.meetuptodoapp.utils.formatDate
//import kotlinx.serialization.Serializable
//import java.time.Instant
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.util.UUID
//
//@Serializable
//data class TodoItem(
//    //val id: String = UUID.randomUUID().toString(), // how can we reliably set this at construction time?
//    val id: String,
//    val title: String = "",
//    val description: String = "",
//    val timestamp: Long = 0L,
//    val completionTime: Long = -1L,
//    val completedTime: Long = Long.MAX_VALUE
//) {
//    fun toUI(todoItem: TodoItem): UITodo {
//        return UITodo(
//            id = todoItem.id,
//            title = todoItem.title.takeIf { it.isNotEmpty() } ?: "No title",
//            description = todoItem.description.takeIf { it.isNotEmpty() } ?: "No description",
//            date = formatDate(todoItem.timestamp),
//            completedTimestamp = todoItem.completedTime
//        )
//    }
////    companion object {
////        fun default(idSupplier: IdSupplier = IdSupplier()): TodoItem {
////            return TodoItem(
////                id = idSupplier.id(),
////                timestamp = Instant.now().toEpochMilli()
////            )
////        }
////    }
//
////    fun formattedDate(isVerbose: Boolean = true): String {
////        val format = if (isVerbose) "EE, dd MMMM yyyy HH:mm" else "EE, dd/MM/yyyy"
////        return Instant
////            .ofEpochMilli(timestamp)
////            .atZone(ZoneId.of("GMT"))
////            .toLocalDateTime()
////            .format(DateTimeFormatter.ofPattern(format))
////    }
//}