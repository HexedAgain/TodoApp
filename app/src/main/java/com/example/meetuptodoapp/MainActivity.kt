package com.example.meetuptodoapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import com.example.meetuptodoapp.model.TodoItem
import com.example.meetuptodoapp.model.TodoStore
import com.example.meetuptodoapp.model.Todos
//import com.example.meetuptodoapp.model.todoDataStore
//import com.example.meetuptodoapp.model.todoDatastore
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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
            var todoFlow by remember { mutableStateOf<StateFlow<Todos>?>(null) }
            LaunchedEffect(null) {
                todoFlow = syncTodos(this@MainActivity).stateIn(lifecycleScope)
            }
            todoFlow?.let {
                val todos = todoFlow?.collectAsState()?.value ?: Todos(listOf())
                MeetupTODOAppTheme {
                    AllTodos(todos)
                }
            }
        }
    }
}

//@Composable
//fun syncTodos(todoFlow: MutableStateFlow<Todos>): StateFlow<Todos> {
fun syncTodos(context: Context): Flow<Todos> {
//    val context = LocalContext.current
    return flow {
        (context.applicationContext as TodoApplication).todoDataStore.data.collect { latestTodos ->
            emit(latestTodos)
        }
    }
//    LaunchedEffect(null) {
////        context.todoDatastore.data.collect { latestTodos ->
////            todoFlow.value = latestTodos
////        }
//        (context.applicationContext as TodoApplication).todoDataStore.data.collect { latestTodos ->
//            //todoFlow.value = latestTodos
//            todoFlow.emit
//        }
//    }
//    return todoFlow.asStateFlow()
}

@Composable
fun AllTodos(todos: Todos) {
    val context = LocalContext.current
//    val flow = (context as MainActivity).todoFlow
    //val todos by syncTodos(flow).collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
//        floatingActionButton = { TodoFAB(todos.todos.size) }
        floatingActionButton = { TodoFAB(context, todos.todos.size) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(count = todos.todos.size) { idx ->
                val item = todos.todos[idx]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .background(shape = RectangleShape, color = Color.Gray)
                ) {
                    // could make this a fragment
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.title)
                        Text(text = "some description", modifier = Modifier)
                    }
                }
            }
        }
    }
}

@Composable
fun TodoFAB(context: Context, currTodoCount: Int) {
    FloatingActionButton(
        // this needs to launch a new activity where we can write the todoItem it's gonna make an intent
        onClick = {
            runBlocking {
                //context.todoDatastore.updateData { t -> t.copy(t.todos + TodoItem("some-title $currTodoCount")) }
                (context.applicationContext as TodoApplication).todoDataStore.updateData { todos ->
                    todos.copy(todos = todos.todos + TodoItem("some-title $currTodoCount"))
                }
            }
        },
        shape = CircleShape,
        modifier = Modifier.size(72.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_add_24),
            modifier = Modifier.size(36.dp),
            contentDescription = null,
            tint = Color.Black,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MeetupTODOAppTheme {
        AllTodos(Todos(todos = listOf()))
    }
}
