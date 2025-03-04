package com.example.meetuptodoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.example.meetuptodoapp.ui.screen.TodoScreen
import com.example.meetuptodoapp.ui.viewmodel.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

// FIXME - move strings to resources
// FIXME - remove workmanager
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TodoViewModel by viewModel()
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) { viewModel.collectTodos() }
            TodoScreen(viewModel)
        }
    }
}
