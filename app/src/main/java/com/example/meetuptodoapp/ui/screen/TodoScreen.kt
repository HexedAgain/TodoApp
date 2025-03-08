package com.example.meetuptodoapp.ui.screen

//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Checkbox
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SheetState
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.meetuptodoapp.R
//import com.example.meetuptodoapp.ui.viewmodel.TodoForm
//import com.example.meetuptodoapp.ui.viewmodel.TodoViewModel
//import com.example.meetuptodoapp.ui.viewmodel.TodoViewModel.UIMode
//import com.example.meetuptodoapp.domain.model.TodoItem
//import com.example.meetuptodoapp.ui.tags.TodoTags
//import com.example.meetuptodoapp.ui.theme.MeetupTODOAppTheme
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TodoScreen(viewModel: TodoViewModel) {
//    val uiMode by viewModel.uiMode.collectAsState()
//    val todos by viewModel.todos.collectAsState()
//    MeetupTODOAppTheme {
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//            floatingActionButton = {
//                TodoFAB {
//                    viewModel.onCreateTodo()
//                }
//            }
//        ) { innerPadding ->
//            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
//            val showCompleted by viewModel.showCompleted.collectAsState()
//            Column(modifier = Modifier.padding(innerPadding)) {
//                Header(showCompleted) {
//                    viewModel.toggleShowCompleted()
//                }
//                TodoList(
//                    todos = todos,
//                    isCompleted = viewModel::isCompleted,
//                    onViewTodo = viewModel::onViewTodo,
//                    onToggleComplete = viewModel::onToggleComplete
//                )
//
//                when (uiMode) {
//                    is TodoViewModel.UIMode.ViewAll -> {}
//                    else -> {
//                        ModalTodo(
//                            bottomSheetState = bottomSheetState,
//                            todoAction = uiMode,
//                            onDelete = viewModel::onDeleteTodo,
//                            onDone = viewModel::onTodoDone,
//                            onUpdate = viewModel::onUpdateTodo
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AddTodoScreen(
//    todoAction: UIMode,
//    onUpdate: (TodoItem) -> Unit,
//    onDelete: (TodoItem) -> Unit,
//    onDone: (TodoForm) -> Unit
//) {
//    Box(
//        modifier = Modifier.padding(16.dp),
//    ) {
//        when (todoAction) {
//            is UIMode.ViewSingle -> {
//                ViewTodo(todoAction.todo, onDelete = onDelete, onUpdate = onUpdate)
//            }
//            is UIMode.Update -> {
//                ModalTodoForm(todoForm = todoAction.todoForm, onDone = onDone)
//            }
//            is UIMode.Create -> {
//                ModalTodoForm(todoForm = todoAction.todoForm, onDone = onDone)
//            }
//
//            else -> {}
//        }
//    }
//}
//
//@Composable
//private fun Header(currChecked: Boolean, onChecked: (Boolean) -> Unit) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Todo List",
//                color = Color.Black,
//                fontSize = 24.sp
//            )
//            Row(
//                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("Show done todos?")
//                Checkbox(checked = currChecked, onCheckedChange = onChecked)
//            }
//        }
//    }
//}
//
//@Composable
//private fun TodoList(
//    todos: List<TodoItem>,
//    isCompleted: (TodoItem) -> Boolean,
//    onViewTodo: (TodoItem) -> Unit,
//    onToggleComplete: (TodoItem) -> Unit
//) {
//    LazyColumn(modifier = Modifier.testTag(TodoTags.TODO_LIST)) {
//        items(count = todos.size) { idx ->
//            val todo = todos[idx]
//            Card(
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                modifier = Modifier
//                    .fillMaxWidth().fillMaxHeight()
//                    .padding(vertical = 8.dp, horizontal = 16.dp)
//                    .clickable { onViewTodo(todo) }
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(.8f)) {
//                        Text(todo.title, style = TextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
//                        Text(text = todo.description, modifier = Modifier)
//                    }
//                    IconButton(onClick = { onToggleComplete(todo) }) {
//                        Icon(
//                            painter = painterResource(R.drawable.baseline_done_24),
//                            contentDescription = null,
//                            tint = if (isCompleted(todo)) Color.DarkGray else Color.LightGray
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun TodoFAB(onClick: () -> Unit) {
//    FloatingActionButton(
//        onClick = onClick,
//        shape = CircleShape,
//        modifier = Modifier.testTag(TodoTags.FAB).size(72.dp)
//    ) {
//        Icon(
//            painter = painterResource(R.drawable.baseline_add_24),
//            modifier = Modifier.size(36.dp),
//            contentDescription = null,
//            tint = Color.Black,
//        )
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ModalTodo(
//    bottomSheetState: SheetState,
//    todoAction: UIMode,
//    onUpdate: (TodoItem) -> Unit,
//    onDelete: (TodoItem) -> Unit,
//    onDone: (TodoForm?) -> Unit
//) {
//    LaunchedEffect(null) { bottomSheetState.expand() }
//    ModalBottomSheet(
//        modifier = Modifier.testTag(TodoTags.MODAL_BOTTOM_SHEET),
//        sheetState = bottomSheetState,
//        onDismissRequest = {
//            onDone(null)
//        }
//    ) {
//        AddTodoScreen(
//            todoAction = todoAction,
//            onDelete = onDelete,
//            onDone = { todoEditor ->
//                onDone(todoEditor)
//            },
//            onUpdate = { todoItem ->
//                onUpdate(todoItem)
//            }
//        )
//    }
//}
