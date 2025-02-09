package com.example.meetuptodoapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.utils.formatDate
import com.example.meetuptodoapp.utils.formatTime
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@Composable
fun AddTodoScreen(
    todoIdx: Int?,
    onUpdate: @Composable (String, String, Long, String?) -> Unit,
    onDelete: @Composable (String) -> Unit
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
                onDelete = { deleteId = it.id }
            )
        }
    }
}

fun getHoursMinsFromTimestamp(timestamp: Long): Pair<Int, Int> {
    if (timestamp == -1L) return Pair(0, 0)
    val actualTime = Instant.ofEpochMilli(timestamp)

    return with (actualTime.atZone(ZoneId.of("GMT"))) {
        Pair(hour, minute)
    }
}

fun addHoursMinsToDate(timestamp: Long, hours: Int, mins: Int): Long {
    if (timestamp == -1L) return timestamp
    val actualTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("GMT"))
        .plusHours(hours.toLong())
        .plusMinutes(mins.toLong())

    return actualTime.toInstant().toEpochMilli()
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
    val hoursMins = remember { getHoursMinsFromTimestamp(timestamp.longValue) }
    var hours by remember { mutableStateOf(hoursMins.first) }
    var mins by remember { mutableStateOf(hoursMins.second) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    if (isDone) {
        //onDone(title.value, description.value, addHoursMinsToDate(timestamp.longValue, hours, mins))
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
            ToBeDoneByTime(timestamp.longValue, hours, mins) {
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
        }
    }
    if (showTimePicker) {
        Clock(timestamp.longValue) { newHours, newMins ->
            val (oldHours, oldMins) = getHoursMinsFromTimestamp(timestamp.longValue)
            val newTimestamp = Instant.ofEpochMilli(timestamp.longValue)
                .atZone(ZoneId.of("GMT"))
                .plusHours((newHours - oldHours).toLong())
                .plusMinutes((newMins - oldMins).toLong())
                .toInstant()
                .toEpochMilli()
            timestamp.longValue = newTimestamp
            showTimePicker = false
        }
    }
}

fun newTimestamp(timestamp: Long, hours: Int, mins: Int): Long {
    return timestamp
}

@Composable
fun CTAButton(title: String, description: String, timestamp: Long, isAdd: Boolean, onDone: () -> Unit) {
    Box(
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            enabled = title.isNotEmpty() && description.isNotEmpty() && timestamp > -1,
            onClick = { onDone() }
        ) {
            Text(text = "${ if (isAdd) "Add" else "Update" } TODO")
        }
    }
}

@Composable
fun ViewTodo(
    todoIdx: Int,
    onUpdate: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
    val todoFlow = (LocalContext.current.applicationContext as TodoApplication).todoDataStore.data
    val todo = runBlocking { todoFlow.first().todos[todoIdx] }
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        TextBox("Title", todo.title)
        TextBox("Description", todo.description)
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
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().weight(.25f)
    )
}

@Composable
fun ToBeDoneByDate(date: Long, onSelected: () -> Unit) {
    val interactionScope = remember {
        getInteractionSource { onSelected() }
    }
    Row {
        OutlinedTextField(
            label = { Text(text = "To Be Done By") },
            value = if (date > -1) formatDate(date, isVerbose = false) else "",
            onValueChange = {},
            readOnly = true,
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
fun ToBeDoneByTime(timestamp: Long, hours: Int = -1, mins: Int = -1, onSelected: () -> Unit) {
    val interactionScope = remember {
        getInteractionSource { onSelected() }
    }
    Row {
        OutlinedTextField(
            label = { Text(text = "At Time") },
            enabled = timestamp > -1,
            //value = if (hours > -1 && mins > -1 && timestamp > -1) formatTime(timestamp = timestamp, hours, mins) else "",
            value = formatTime(timestamp = timestamp, hours, mins),
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
fun Clock(hours: Int, mins: Int, onClose: (Int, Int) -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = .25f)),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        TimePicker(timePickerState)
//    }
    // Might have a timestamp that already has hours / minutes in it
    val timePickerState = TimePickerState(hours, mins, true)
    BasicAlertDialog(onDismissRequest = {
        onClose(timePickerState.hour, timePickerState.minute)
    }) {
        TimePicker(timePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Clock(timestamp: Long, onClose: (Int, Int) -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = .25f)),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        TimePicker(timePickerState)
//    }
    // Might have a timestamp that already has hours / minutes in it
    val (hours, mins) = getHoursMinsFromTimestamp(timestamp)
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
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = Color.White.copy(alpha = 0.5f)),
        verticalArrangement = Arrangement.Center,
    ) {
        DatePickerDialog(
            confirmButton = {},
            onDismissRequest = {
                onClose(datePickerState.selectedDateMillis)
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
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
            if (interaction is PressInteraction.Press) {
                onClick()
            }
        }

        override fun tryEmit(interaction: Interaction): Boolean {
            return interactions.tryEmit(interaction)
        }

    }
}