package com.example.meetuptodoapp.todos.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatcherSupplier {
    fun ioDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
    fun defaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }
    fun mainDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }
    fun unconfinedDispatcher(): CoroutineDispatcher {
        return Dispatchers.Unconfined
    }
}