package com.example.meetuptodoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.ui.model.UITodo
import com.example.meetuptodoapp.domain.service.ReminderService
import com.example.meetuptodoapp.domain.work.TodosWorker
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import com.example.meetuptodoapp.utils.toUI
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.Duration

class MainActivity: ComponentActivity() {
    // Things we would like to test (and be sure to make them outrageously large):
    // - that when we launch this screen we show all the todos that are available on disk
    // - that when we click the fab that we launch an activity to create a new todo
    // - once we create the new todo it is shown on the prior activity and is also saved to disk

    // To-be-done-by field, which if set will has a bell icon, if clicked then it raises
    // a calendar to set a notification time. Doing this will send device token to some server,
    // in order to issue a push notification at that date
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetupTODOAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { TodoFAB() }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TodoScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun TodoScreen() {
    val todoFlow = (LocalContext.current.applicationContext as TodoApplication).todoDataStore.data
    var todoList: List<UITodo> by remember { mutableStateOf(listOf()) }
    LaunchedEffect(null) {
        todoFlow.distinctUntilChanged().map { it.toUI() }.collect { latestTodos ->
            todoList = latestTodos
        }
    }
    var showDoneTodos by remember { mutableStateOf(false) }
    Header(showDoneTodos) {
        showDoneTodos = it
    }
    TodoList(todoList)
    // Or use workmanager here
    val context = LocalContext.current
    WorkManager.getInstance(context).enqueueUniqueWork(
        uniqueWorkName = "my work",
        existingWorkPolicy = ExistingWorkPolicy.KEEP,
        request = OneTimeWorkRequestBuilder<TodosWorker>()
            .setInitialDelay(Duration.ofMillis(1000000))
            .build()
    )
    ReminderService.setReminderTime("", 123L) {

    }
}

@Composable
private fun Header(currChecked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Todo List",
                color = Color.Black,
                fontSize = 24.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show done todos?")
                Checkbox(checked = currChecked, onCheckedChange = onChecked)
            }
        }
    }
}

@Composable
private fun TodoList(todos: List<UITodo>) {
    LazyColumn {
        items(count = todos.size) { idx ->
            val item = todos[idx]
            Column(
                modifier = Modifier
                    .fillMaxWidth().fillMaxHeight()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .background(shape = RoundedCornerShape(8.dp), color = Color.Gray),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(.8f)) {
                        Text(text = item.date)
                        Text(item.title)
                        Text(text = item.description, modifier = Modifier)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_clear_24),
                                modifier = Modifier.size(36.dp),
                                contentDescription = null,
                                tint = Color.Black,
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_edit_24),
                                modifier = Modifier.size(36.dp),
                                contentDescription = null,
                                tint = Color.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoFAB() {
    val context = LocalContext.current
    FloatingActionButton(
        // this needs to launch a new activity where we can write the todoItem it's gonna make an intent
        onClick = {
            runBlocking {
                (context.applicationContext as TodoApplication).todoDataStore.updateData { todos ->
                    todos.copy(todos = todos.todos + TodoItem.default())
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
//        AllTodos(Todos(todos = listOf()))
    }
}
