package com.example.meetuptodoapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.meetuptodoapp.R
import com.example.meetuptodoapp.storage.TodoStorage
import com.example.meetuptodoapp.ui.viewmodel.TodoViewModel
import com.example.meetuptodoapp.api.TodoClient
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.storage.TodoSharedPrefs
import com.example.meetuptodoapp.storage.TodoStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single {
        TodoRepository(get())
    }

    single<TodoClient> {
        // Just a stub
        object : TodoClient {
            override suspend fun <T: Any> post(postBody: T, url: String) {
                println("test")
            }
        }
    }

    factory {
        TodoStorage(get())
    }

    single {
        TodoStore(get())
    }

    single<TodoSharedPrefs> {
        val context: Context = get()
        TodoSharedPrefs(
            sharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.prefs), MODE_PRIVATE)
        )
    }

    factory<CoroutineDispatcher>(named("ioDispatcher")) {
        Dispatchers.IO
    }

    factory<CoroutineDispatcher>(named("mainDispatcher")) {
        Dispatchers.Main
    }

    viewModel {
        TodoViewModel(get(), get(named("ioDispatcher")), get() )
    }
}