package com.example.meetuptodoapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meetuptodoapp.model.Todos
import com.example.meetuptodoapp.model.Todos.TodoItem
import com.example.meetuptodoapp.model.todoDatastore
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class MainActivity: ComponentActivity() {
    // Things we would like to test (and be sure to make them outrageously large):
    // - that when we launch this screen we show all the todos that are available on disk
    // - that when we click the fab that we launch an activity to create a new todo
    // - once we create the new todo it is shown on the prior activity and is also saved to disk
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetupTODOAppTheme {
                val todos by syncTodos().collectAsState()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { TodoFAB(todos.todos.size) }
                ) { innerPadding ->
                    AllTodos(todos = todos, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AllTodos(todos: Todos, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(todos.todos.size) { idx ->
            TodoItem(todos.todos[idx])
        }
    }
}

@Composable
fun Context.TodoFAB(currTodoCount: Int) {
    FloatingActionButton(
        // this needs to launch a new activity where we can write the todoItem it's gonna make an intent
        onClick = {
            runBlocking {
                todoDatastore.updateData { t -> t.copy(t.todos + TodoItem("some-title $currTodoCount")) }
            }
        }
    ) {
        Icon(painter = painterResource(R.drawable.ic_launcher_background), contentDescription = null)
    }
}

@Composable
private fun Context.syncTodos(): StateFlow<Todos> {
    val todoFlow: MutableStateFlow<Todos> = MutableStateFlow(Todos(listOf()))
    LaunchedEffect(null) {
        // gonna require robolectric already to be able to get a context
        todoDatastore.data.collect { latestTodos ->
            todoFlow.value = latestTodos
        }
    }
    return todoFlow.asStateFlow()
}


@Composable
fun TodoItem(item: TodoItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(shape = RectangleShape, color = Color.Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title)
            Text(text = "some description", modifier = Modifier)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MeetupTODOAppTheme {
        AllTodos(Todos(listOf()), Modifier)
    }
}