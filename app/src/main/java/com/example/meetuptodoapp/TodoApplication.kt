package com.example.meetuptodoapp

import android.app.Application
import com.example.meetuptodoapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

//import com.google.firebase.FirebaseApp

class TodoApplication: Application() {
//    val todoDataStore: DataStore<Todos> = TodoStore(this)
    override fun onCreate() {
    super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TodoApplication)
            modules(appModule)
        }
    }
}