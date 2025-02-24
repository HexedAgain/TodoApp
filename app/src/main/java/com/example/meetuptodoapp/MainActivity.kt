package com.example.meetuptodoapp

import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.ui.model.UITodo
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import kotlinx.coroutines.flow.stateIn
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.util.UUID

class MainActivity: ComponentActivity() {
    private val todoDataStore: DataStore<Todos> = TodoStore(this)
    private val repo = TodoRepository()
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
        val viewModel: TodoViewModel by viewModel()
        enableEdgeToEdge()
        setContent {
            val showModal by viewModel.showModal.collectAsState()
            val todos by viewModel.todos.collectAsState()
            MeetupTODOAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { TodoFAB { viewModel.createTodo() } }
                ) { innerPadding ->
                    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TodoScreen(todos = todos, onCreateOrEdit = viewModel::onTodoViewOrCreate)

                        if (showModal) {
                            ModalUpdateTodo(
                                bottomSheetState = bottomSheetState,
                                todoAction = viewModel.currentTodoAction(),
                                onDelete = viewModel::deleteTodo,
                                onDone = viewModel::closeModal,
                                onUpdate = viewModel::updateTodo
                            )
                        }
                    }
                }
            }
        }
    }

//    @Composable
////    fun commitTodo(title: String, description: String, timestamp: Long, id: String?, onFinish: () -> Unit) {
//    fun commitTodo(title: String, description: String, timestamp: Long, currentTodoItem: TodoItem?, onFinish: () -> Unit) {
//        LaunchedEffect(null) {
//            val todo = if (currentTodoItem == null) {
//                addTodo(todoDataStore, title, description, timestamp)
//            } else {
//                updateTodo(todoDataStore, title, description, timestamp, currentTodoItem)
//            }
//            repo.logTodoStats(todo) {
//                onFinish()
//            }
//        }
//    }

//    suspend fun addTodo(todoStore: DataStore<Todos>, title: String, description: String, timestamp: Long): TodoItem {
//        val thisTodo = TodoItem(
//            id = UUID.randomUUID().toString(),
//            title = title,
//            description = description,
//            timestamp = Instant.now().toEpochMilli(),
//            completionTime = timestamp
//        )
//        todoStore.updateData { todos ->
//            todos.copy(todos = todos.todos + thisTodo)
//        }
//        return thisTodo
//    }

//    suspend fun updateTodo(
//        todoStore: DataStore<Todos>,
//        title: String,
//        description: String,
//        completionTime: Long,
//        currentTodoItem: TodoItem,
////        id: String,
//        completedTime: Long = Long.MAX_VALUE
//    ): TodoItem {
//        var newTodo: TodoItem = TodoItem.default()
//        todoStore.updateData { todos ->
////            val todoItem = todos.todos.find { it.id == id }
//            val todoIdx = todos.todos.indexOf(currentTodoItem)
//            newTodo = TodoItem(
//                title = title,
//                description = description,
//                timestamp = Instant.now().toEpochMilli(),
//                completionTime = completionTime,
//                completedTime = completedTime
//            )
//            val allTodos = todos.todos.map { it } as MutableList<TodoItem>
//            allTodos[todoIdx] = newTodo
//            todos.copy(todos = allTodos)
//        }
//        return newTodo
//    }

    @Composable
    fun TodoScreen(todos: List<UITodo>, onCreateOrEdit: (Int) -> Unit) {
        var showDoneTodos by remember { mutableStateOf(false) }
        Header(showDoneTodos) {
            showDoneTodos = it
        }
        // So far, I only know that I'm "viewing" a todo
        TodoList(todos = todos, showDoneTodos = showDoneTodos, onClick = { onCreateOrEdit(it) })
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
        val scope = rememberCoroutineScope()
        var finishedId by remember { mutableStateOf<String?>(null) }
        finishedId?.let {
            LaunchedEffect(null) {
                val todo = todoDataStore.data.stateIn(scope).value.todos.first { it.id == finishedId }
//                updateTodo(
//                    todoDataStore,
//                    todo.title,
//                    todo.description,
//                    todo.completionTime,
//                    todo,
//                    Instant.now().toEpochMilli()
//                )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
//    fun ModalUpdateTodo(bottomSheetState: SheetState, todoIdx: Int?, onClose: () -> Unit) {
    fun ModalUpdateTodo(
        bottomSheetState: SheetState,
        todoAction: TodoViewModel.TodoAction,
        onUpdate: (TodoItem) -> Unit,
        onDelete: (TodoItem) -> Unit,
        onDone: (TodoEditor?) -> Unit
    ) {
        LaunchedEffect(null) { bottomSheetState.expand() }
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = {
                onDone(null)
            }
        ) {
            AddTodoScreen(
                todoAction = todoAction,
                onDelete = onDelete,
                onDone = { todoEditor ->
                    onDone(todoEditor)
                },
                onUpdate = { todoItem ->
                    onUpdate(todoItem)
                }
//                todoIdx = todoIdx,
//                currentTodoItem = activeTodoItem,
//                onUpdate = { title, description, completionDate, id ->
////                    commitTodo(title, description, completionDate, activeTodoItem) { onClose() }
//                },
//                onDelete = {
////                    deleteTodo(it) { onClose() }
//                },
//                todoFlow = todoDataStore.data
            )
        }
    }
}