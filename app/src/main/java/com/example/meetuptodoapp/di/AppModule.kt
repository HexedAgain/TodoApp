package com.example.meetuptodoapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.meetuptodoapp.R
import com.example.meetuptodoapp.todos.storage.TodoSharedPrefs
import com.example.meetuptodoapp.todos.storage.TodoStorage
import com.example.meetuptodoapp.todos.storage.TodoStore
import com.example.meetuptodoapp.todos.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

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