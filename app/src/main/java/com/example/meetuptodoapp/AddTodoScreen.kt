package com.example.meetuptodoapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.utils.formatDate
import com.example.meetuptodoapp.utils.formatTime
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun AddTodoScreen(
    todoIdx: Int?,
    onUpdate: @Composable (String, String, Long, String?) -> Unit,
    onDelete: @Composable (String) -> Unit,
    todoFlow: Flow<Todos>
) {
    var todo by remember { mutableStateOf<TodoItem?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.padding(16.dp),
    ) {
        when {
            deleteId != null -> {
                deleteId?.let {
                    onDelete(it)
                }
            }
            todo != null -> {
                todo?.let {
                    EditTodo(it.title, it.description, it.completionTime) { title, description, timestamp ->
                        onUpdate(title, description, timestamp, it.id)
                    }
                }
            }
            todoIdx == null -> {
                EditTodo { title, description, timestamp ->
                    onUpdate(title, description, timestamp, null)
                }
            }
            else -> ViewTodo(
                todoIdx = todoIdx,
                onUpdate = { todo = it },
                onDelete = { deleteId = it.id },
                todoFlow = todoFlow
            )
        }
    }
}

@Composable
fun EditTodo(
    initialTitle: String = "",
    initialDescription: String = "",
    initialTimestamp: Long = -1,
    onDone: @Composable (String, String, Long) -> Unit
) {
    // Interesting bug here I had mixed up title / description
    val description = remember { mutableStateOf(initialDescription)}
    val title = remember { mutableStateOf(initialTitle)}
    val timestamp = remember { mutableLongStateOf(initialTimestamp) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    if (isDone) {
        println("isDone")
        onDone(title.value, description.value, timestamp.longValue)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        TitleSection(title.value) {
            title.value = it
        }
        TodoDescription(description.value) {
            description.value = it
        }
        Row {
            ToBeDoneByDate(timestamp.longValue) {
                showDatePicker = true
            }
            ToBeDoneByTime(timestamp.longValue) {
                showTimePicker = true
            }
        }
        CTAButton(title.value, description.value, timestamp.longValue, initialTitle == "") {
            isDone = true
        }
        Spacer(modifier = Modifier.weight(.75f))
    }
    if (showDatePicker) {
        Calendar(timestamp.longValue) { timeStamp ->
            timeStamp?.let {
                timestamp.longValue = it
            }
            showDatePicker = false
            // could set isDone true here if in test (total hack)
        }
    }
    if (showTimePicker) {
        Clock(timestamp.longValue) { newHours, newMins ->
            val currTime = currTime(timestamp.longValue)
            val (oldHours, oldMins) = Pair(currTime.hour, currTime.minute)
            timestamp.longValue = currTime
                .plusHours((newHours - oldHours).toLong())
                .plusMinutes((newMins - oldMins).toLong())
                .toInstant()
                .toEpochMilli()
            showTimePicker = false
        }
    }
}

fun currTime(timestamp: Long): ZonedDateTime {
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("GMT"))
}

@Composable
fun CTAButton(title: String, description: String, timestamp: Long, isAdd: Boolean, onDone: () -> Unit) {
    println("CTA button rendered")
//    val interactionScope = remember {
//        getInteractionSource {
//            onDone()
//        }
//    }
    Box(
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
//            interactionSource = interactionScope,
            enabled = title.isNotEmpty() && description.isNotEmpty() && timestamp > -1,
            onClick = {
                println("CTA button clicked")
                onDone()
            },
        ) {
            Text(text = "${ if (isAdd) "Add" else "Update" } TODO")
        }
    }
}

@Composable
fun ViewTodo(
    todoIdx: Int,
    onUpdate: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    todoFlow: Flow<Todos>
) {
//    val todoFlow = (LocalContext.current.applicationContext as TodoApplication).todoDataStore.data
    val todo = runBlocking { todoFlow.first().todos[todoIdx] }
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        TextBox("Title", todo.title)
        TextBox("Description", todo.description)
        TextBox("To Complete By", formatDate(todo.completionTime))
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row {
                IconButton(onClick = {
                    onDelete(todo)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_delete_24),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
                IconButton(onClick = {
                    onUpdate(todo)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_edit_24),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

@Composable
fun TextBox(title: String, text: String?) {
    Text(
        text = title,
        style = TextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Box(
        modifier = Modifier
            .heightIn(min = if (title == "Description") 60.dp else 24.dp)
            .border(border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
    ) {
        Text(
            text = text ?: "",
            modifier = Modifier.padding(8.dp)
        )
    }
    Spacer(modifier = Modifier.padding(8.dp))
}

@Composable
fun TitleSection(title: String, onUpdateTitle: (String) -> Unit) {
    OutlinedTextField(
        label = { Text(text = "Title") },
        placeholder = {},
        value = title,
        onValueChange = onUpdateTitle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
    )
}

@Composable
fun ColumnScope.TodoDescription(description: String, onUpdateDescription: (String) -> Unit) {
    OutlinedTextField(
        label = { Text(text = "Description") },
        value = description,
        onValueChange = onUpdateDescription,
        singleLine = false,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().weight(.25f)
    )
}

@Composable
fun ToBeDoneByDate(timestamp: Long, onSelected: () -> Unit) {
    val interactionScope = remember {
        getInteractionSource {
            onSelected()
        }
    }
    Row {
        OutlinedTextField(
            label = { Text(text = "To Be Done By") },
            value = if (timestamp > -1) formatDate(timestamp, isVerbose = false) else "",
            onValueChange = {},
            readOnly = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_calendar_month_24),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(.6f).padding(end = 8.dp),
            interactionSource = interactionScope
        )
    }
}

@Composable
fun ToBeDoneByTime(timestamp: Long, onSelected: () -> Unit) {
    val interactionScope = remember {
        getInteractionSource { onSelected() }
    }
    Row {
        OutlinedTextField(
            label = { Text(text = "At Time") },
            enabled = timestamp > -1,
            value = formatTime(timestamp = timestamp),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_access_time_24),
                    contentDescription = null
                )
            },
            interactionSource = interactionScope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Clock(timestamp: Long, onClose: (Int, Int) -> Unit) {
    val (hours, mins) = with (Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("GMT"))) {
        Pair(hour, minute)
    }
    val timePickerState = TimePickerState(hours, mins, true)
    BasicAlertDialog(onDismissRequest = {
        onClose(timePickerState.hour, timePickerState.minute)
    }) {
        TimePicker(timePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(initialTimestamp: Long, onClose: (Long?) -> Unit) {
    val datePickerState = DatePickerState(locale = Locale.UK).apply {
        selectedDateMillis = initialTimestamp.takeIf { it > -1 } ?: Instant.now().toEpochMilli()
    }
    val currTime = currTime(initialTimestamp)
    fun onDismiss() {
        val hoursMinsOffset = (currTime.hour * 3600 + currTime.minute * 60) * 1000
        onClose(datePickerState.selectedDateMillis?.plus(hoursMinsOffset.toLong()))
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = Color.White.copy(alpha = 0.5f)),
        verticalArrangement = Arrangement.Center,
    ) {
        DatePickerDialog(
            confirmButton = {
                // Inject this from test
//                Box(modifier = Modifier.testTag("datePickerConfirm").clickable { onDismiss() })
                Button(onClick = {
                    onDismiss()
//                    val hoursMinsOffset = (currTime.hour * 3600 + currTime.minute * 60) * 1000
//                    onClose(datePickerState.selectedDateMillis?.plus(hoursMinsOffset.toLong()))
                }) {
                    Text("Done")
                }
            },
            modifier = Modifier.testTag("DatePicker"),
            onDismissRequest = { onDismiss() }
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

fun getInteractionSource(onClick: () -> Unit): MutableInteractionSource {
    return object: MutableInteractionSource {
        override val interactions = MutableSharedFlow<Interaction>(
            extraBufferCapacity = 16,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override suspend fun emit(interaction: Interaction) {
            if (interaction is PressInteraction.Press || interaction is FocusInteraction.Focus) {
                onClick()
            }
        }

        override fun tryEmit(interaction: Interaction): Boolean {
            return interactions.tryEmit(interaction)
        }

    }
}