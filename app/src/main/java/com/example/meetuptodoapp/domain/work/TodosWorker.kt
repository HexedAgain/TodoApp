package com.example.meetuptodoapp.domain.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TodosWorker(
    appContext: Context,
    params: WorkerParameters
): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // this guy is gonna create a notification
        return Result.success()
    }
}