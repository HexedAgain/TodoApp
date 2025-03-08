package com.example.meetuptodoapp.todos.ui.widgets

import androidx.compose.runtime.Composable

class MockDateSelector(): DateSelector {
    var initialTimestampSupplied: Long? = null
    var onCloseFn: ((Long?) -> Unit)? = null
    @Composable
    override fun invoke(initialTimestamp: Long, onClose: (Long?) -> Unit) {
        initialTimestampSupplied = initialTimestamp
        onCloseFn = onClose
    }
}