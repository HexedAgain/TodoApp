package com.example.meetuptodoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meetuptodoapp.model.Todos
import com.example.meetuptodoapp.model.TodoItem
import com.example.meetuptodoapp.model.todoDatastore
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

//@Composable
//fun AllTodos() {
//    val todos by syncTodos().collectAsState()
//    Scaffold(
//        modifier = Modifier.fillMaxSize(),
//        floatingActionButton = { TodoFAB(todos.todos.size) }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            items(todos.todos.size) { idx ->
//                TodoItem(todos.todos[idx])
//            }
//        }
//    }
//}
//
//@Composable
//fun TodoFAB(currTodoCount: Int) {
//    val context = LocalContext.current
//    FloatingActionButton(
//        // this needs to launch a new activity where we can write the todoItem it's gonna make an intent
//        onClick = {
//            runBlocking {
//                context.todoDatastore.updateData { t -> t.copy(t.todos + TodoItem("some-title $currTodoCount")) }
//            }
//        }
//    ) {
//        Icon(painter = painterResource(R.drawable.ic_launcher_background), contentDescription = null)
//    }
//}
//
//@Composable
//fun syncTodos(): StateFlow<Todos> {
//    val todoFlow: MutableStateFlow<Todos> = MutableStateFlow(Todos(listOf()))
//    val context = LocalContext.current
//    LaunchedEffect(null) {
//        // gonna require robolectric already to be able to get a context
//        context.todoDatastore.data.collect { latestTodos ->
//            todoFlow.value = latestTodos
//        }
//    }
//    return todoFlow.asStateFlow()
//}
//
//
//@Composable
//fun TodoItem(item: TodoItem) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp, horizontal = 16.dp)
//            .background(shape = RectangleShape, color = Color.Gray)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(item.title)
//            Text(text = "some description", modifier = Modifier)
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MeetupTODOAppTheme {
//        AllTodos()
//    }
//}
