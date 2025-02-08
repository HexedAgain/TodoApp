package com.example.meetuptodoapp.domain.service

//import com.google.firebase.messaging.FirebaseMessaging

// will be invoked when they hit bell icon (shows a calendar)
object ReminderService {
//    fun setReminderTime(todoId: String, reminderTime: Long, onComplete: (Boolean) -> Unit) {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    DummyClient().post(token)
//                    onComplete(true)
//                } else {
//                    onComplete(false)
//                }
//            }
//    }
}

// replace with ktor
class DummyClient {
    fun post(token: String) {

    }
}