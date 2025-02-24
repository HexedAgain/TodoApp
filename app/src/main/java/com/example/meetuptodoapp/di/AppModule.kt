package com.example.meetuptodoapp.di

import com.example.meetuptodoapp.TodoViewModel
import com.example.meetuptodoapp.api.TodoClient
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoStore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        TodoRepository()
    }

    single {
        TodoClient()
    }

    single {
        TodoStore(get())
    }

    viewModel {
        TodoViewModel(get(), get())
    }
}