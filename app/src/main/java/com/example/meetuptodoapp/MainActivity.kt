package com.example.meetuptodoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.ui.model.UITodo
import com.example.meetuptodoapp.domain.work.TodosWorker
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import com.example.meetuptodoapp.utils.toUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.UUID

class MainActivity: ComponentActivity() {
    val todoDataStore: DataStore<Todos> = TodoStore(this)
    // Things we would like to test (and be sure to make them outrageously large):
    // - that when we launch this screen we show all the todos that are available on disk
    // - that when we click the fab that we launch an activity to create a new todo
    // - once we create the new todo it is shown on the prior activity and is also saved to disk

    // To-be-done-by field, which if set will has a bell icon, if clicked then it raises
    // a calendar to set a notification time. Doing this will send device token to some server,
    // in order to issue a push notification at that date
    //
    // The test needs to be proving that the value came from disk (so will need to check what's actually
    // in the todo file
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showModal by remember { mutableStateOf(false) }
            var editIdx by remember { mutableStateOf<Int?>(null) }
            fun onModal(idx: Int?) {
                editIdx = idx
                showModal = idx != null
            }
            MeetupTODOAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { TodoFAB { showModal = true } }
                ) { innerPadding ->
                    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TodoScreen(::onModal, lifecycleScope)

                        if (showModal) {
                            ModalUpdateTodo(bottomSheetState, editIdx) { onModal(null )}
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalUpdateTodo(bottomSheetState: SheetState, todoIdx: Int?, onClose: () -> Unit) {
    LaunchedEffect(null) { bottomSheetState.expand() }
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
            onClose()
        }
    ) {
        AddTodoScreen(
            todoIdx = todoIdx,
            onUpdate = { title, description, completionDate, id ->
                commitTodo(title, description, completionDate, id) { onClose() }
            },
            onDelete = {
                deleteTodo(it) { onClose() }
            }
        )
    }
}

@Composable
fun getTodoStore(): DataStore<Todos> {
    return (LocalContext.current.applicationContext as TodoApplication).todoDataStore
}

@Composable
fun deleteTodo(id: String, onFinish: () -> Unit) {
    val todoStore = getTodoStore()
    LaunchedEffect(null) {
        todoStore.updateData { todos ->
            todos.copy(
                todos = todos.todos.filter { it.id != id }
            )
        }
        onFinish()
    }
}

@Composable
fun commitTodo(title: String, description: String, timestamp: Long, id: String?, onFinish: () -> Unit) {
    val todoStore = getTodoStore()
    LaunchedEffect(null) {
        if (id == null) {
            addTodo(todoStore, title, description, timestamp)
        } else {
            updateTodo(todoStore, title, description, timestamp, id)
        }
        onFinish()
    }
}

suspend fun addTodo(todoStore: DataStore<Todos>, title: String, description: String, timestamp: Long) {
    todoStore.updateData { todos ->
        Log.i("COMMIT", "will update data")
        val thisTodo = TodoItem(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            timestamp = Instant.now().toEpochMilli(),
            completionTime = timestamp
        )
        todos.copy(todos = todos.todos + thisTodo)
    }
}

suspend fun updateTodo(
    todoStore: DataStore<Todos>,
    title: String,
    description: String,
    completionTime: Long,
    id: String,
    completedTime: Long = Long.MAX_VALUE
) {
    todoStore.updateData { todos ->
        val todoItem = todos.todos.find { it.id == id }
        val todoIdx = todos.todos.indexOf(todoItem)
        val newTodo = TodoItem(
            title = title,
            description = description,
            timestamp = Instant.now().toEpochMilli(),
            completionTime = completionTime,
            completedTime = completedTime
        )
        val allTodos = todos.todos.map { it } as MutableList<TodoItem>
        allTodos[todoIdx] = newTodo
        todos.copy(todos = allTodos)
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun TodoScreen(onEdit: (Int) -> Unit, coroutineScope: CoroutineScope) {
    val context = LocalContext.current
    val todoFlow = getTodoStore()
    var todoList: List<UITodo> by remember { mutableStateOf(listOf()) }
    coroutineScope.launch {
        println("coroutine scope launched")
        todoFlow.data.distinctUntilChanged().map { it.toUI() }.collect { latestTodos ->
            todoList = latestTodos
        }
    }.invokeOnCompletion {
        println("completed")
    }
    var showDoneTodos by remember { mutableStateOf(false) }
    Header(showDoneTodos) {
        showDoneTodos = it
    }
    TodoList(todoList, showDoneTodos) {
        onEdit(it)
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
private fun TodoList(todos: List<UITodo>, showDoneTodos: Boolean, onClick: (Int) -> Unit) {
    val store = getTodoStore()
    val scope = rememberCoroutineScope()
    var finishedId by remember { mutableStateOf<String?>(null) }
    finishedId?.let {
        LaunchedEffect(null) {
            val todo = store.data.stateIn(scope).value.todos.first { it.id == finishedId }
            updateTodo(store, todo.title, todo.description, todo.completionTime, todo.id, Instant.now().toEpochMilli())
        }
    }
    LazyColumn {
        items(count = todos.size) { idx ->
            val item = todos[idx]
            if (!showDoneTodos && Instant.now().toEpochMilli() > item.completedTimestamp){
            } else {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth().fillMaxHeight()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .clickable { onClick(idx) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(.8f)) {
                            Text(item.title, style = TextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                            Text(text = item.description, modifier = Modifier)
                        }
                        Checkbox(
                            onCheckedChange = {
                                finishedId = item.id
                            },
                            checked = Instant.now().toEpochMilli() > item.completedTimestamp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
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
