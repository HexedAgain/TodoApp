package com.example.meetuptodoapp.di

import com.example.meetuptodoapp.TodoStorage
import com.example.meetuptodoapp.TodoViewModel
import com.example.meetuptodoapp.api.TodoClient
import com.example.meetuptodoapp.api.TodoClientImpl
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoStore
import org.koin.core.module.dsl.viewModel
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

    viewModel {
        TodoViewModel(get(), get())
    }
}