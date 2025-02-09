package com.example.meetuptodoapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.Locale

@Composable
fun AddTodoScreen(todoIdx: Int?, onDone: @Composable (String, String, Long, String?) -> Unit) {
    var todo by remember { mutableStateOf<TodoItem?>(null) }
    Box(
        modifier = Modifier.padding(16.dp),
    ) {
        when {
            todo != null -> {
                todo?.let {
                    EditTodo(it.title, it.description, it.completionTime) { title, description, timestamp ->
                        onDone(title, description, timestamp, it.id)
                    }
                }
            }
            todoIdx == null -> {
                EditTodo { title, description, timestamp ->
                    onDone(title, description, timestamp, null)
                }
            }
            else -> ViewTodo(todoIdx) {
                todo = it
            }
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
    var isDone by remember { mutableStateOf(false) }
    if (isDone) {
        onDone(title.value, description.value, timestamp.longValue)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        TitleSection(title.value) {
            title.value = it
        }
        TodoDescription(description.value) {
            description.value = it
        }
        ToBeDoneBy(timestamp.longValue) {
            showDatePicker = true
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
fun ViewTodo(todoIdx: Int, onUpdate: (TodoItem) -> Unit) {
    val todoFlow = (LocalContext.current.applicationContext as TodoApplication).todoDataStore.data
    val todo = runBlocking { todoFlow.first().todos[todoIdx] }
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        TextBox("Title", todo.title)
        TextBox("Description", todo.description)
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
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
fun ToBeDoneBy(date: Long, onSelected: () -> Unit) {
    val interactionScope = remember {
        object: MutableInteractionSource {
            override val interactions = MutableSharedFlow<Interaction>(
                extraBufferCapacity = 16,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )

            override suspend fun emit(interaction: Interaction) {
                if (interaction is PressInteraction.Press) {
                    onSelected()
                }
            }

            override fun tryEmit(interaction: Interaction): Boolean {
                return interactions.tryEmit(interaction)
            }

        }
    }
    val initialDate = date.takeIf { it > -1 } ?: Instant.now().toEpochMilli()
    OutlinedTextField(
        label = { Text(text = "To Be Done By") },
        value = formatDate(initialDate, includeHoursMinsSeconds = false),
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.baseline_calendar_month_24),
                contentDescription = null
            )
        },
        interactionSource = interactionScope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(initialTimestamp: Long, onClose: (Long?) -> Unit) {
    val datePickerState = DatePickerState(locale = Locale.UK).apply {
        selectedDateMillis = initialTimestamp.takeIf { it > -1 } ?: Instant.now().toEpochMilli()
    }
    val enabled = remember { mutableStateOf(false) }
    LaunchedEffect(null) {
        enabled.value = true
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
                println(datePickerState)
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