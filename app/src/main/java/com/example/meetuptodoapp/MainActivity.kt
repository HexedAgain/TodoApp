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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meetuptodoapp.TodoViewModel.TodoAction
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.ui.model.UITodo
import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant

class MainActivity: ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TodoViewModel by viewModel()
        enableEdgeToEdge()
        setContent {
            val todoAction by viewModel.todoAction.collectAsState()
            val todos by viewModel.todos.collectAsState()
            // TODO move this to another composable so I can inject own viewmodel
            MeetupTODOAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { TodoFAB { viewModel.onCreateTodo() } }
                ) { innerPadding ->
                    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TodoScreen(todos = todos, onViewTodo = viewModel::onViewTodo)

                        when (todoAction) {
                            is TodoAction.None -> {}
                            else -> {
                                ModalUpdateTodo(
                                    bottomSheetState = bottomSheetState,
                                    todoAction = todoAction,
                                    onDelete = viewModel::onDeleteTodo,
                                    onDone = viewModel::onTodoDone,
                                    onUpdate = viewModel::onUpdateTodo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoScreen(todos: List<UITodo>, onViewTodo: (Int) -> Unit) {
    var showDoneTodos by remember { mutableStateOf(false) }
    Header(showDoneTodos) {
        showDoneTodos = it
    }
    TodoList(todos = todos, onClick = { onViewTodo(it) })
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

// TODO - viewmodel should be filtering the completed ones if necessary
@Composable
private fun TodoList(todos: List<UITodo>, onClick: (Int) -> Unit) {
    LazyColumn {
        items(count = todos.size) { idx ->
            val item = todos[idx]
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
                        onCheckedChange = { },
                        checked = Instant.now().toEpochMilli() > item.completedTimestamp
                    )
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
fun ModalUpdateTodo(
    bottomSheetState: SheetState,
    todoAction: TodoAction,
    onUpdate: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onDone: (TodoForm?) -> Unit
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
        )
    }
}
