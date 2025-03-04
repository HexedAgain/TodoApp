package com.example.meetuptodoapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meetuptodoapp.R
import com.example.meetuptodoapp.ui.viewmodel.TodoForm
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.ui.tags.TodoTags
import com.example.meetuptodoapp.ui.widgets.DateSelector
import com.example.meetuptodoapp.ui.widgets.DateSelectorImpl
import com.example.meetuptodoapp.utils.formatDate
import com.example.meetuptodoapp.utils.formatTime
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import java.time.ZoneId

@Composable
fun ModalTodoForm(
    todoForm: TodoForm,
    onDone: (TodoForm) -> Unit,
    dateSelector: DateSelector = DateSelectorImpl
) {
    // Interesting bug here I had mixed up title / description
    val title by todoForm.title.collectAsState()
    val description by todoForm.description.collectAsState()
    val timestamp by todoForm.timestamp.collectAsState()
    val showDatePicker by todoForm.showDatePicker.collectAsState()
    val showTimePicker by todoForm.showTimePicker.collectAsState()

//    var isDone by remember { mutableStateOf(false) }
//    if (isDone) {
//        onDone(todoForm)
//    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TitleSection(title, todoForm::updateTitle)
        TodoDescription(description, todoForm::updateDescription)
        Row {
            ToBeDoneByDate(
                timestamp = timestamp,
                onSelected = todoForm::showDatePicker
            )
            ToBeDoneByTime(
                timestamp = timestamp,
                onSelected = todoForm::showTimePicker
            )
        }
        CTAButton(isEnabled = todoForm.isValid(), buttonText = todoForm.buttonText()) {
            onDone(todoForm)
        }
        Spacer(modifier = Modifier.weight(.75f))
    }

    if (showDatePicker) {
        dateSelector(
            initialTimestamp = todoForm.initialTimestamp(),
            onClose = todoForm::updateCompletedByDate
        )
//        Calendar(
//            initialTimestamp = todoForm.initialTimestamp(),
//            onClose = todoForm::updateCompletedByDate
//        )
    }
    if (showTimePicker) {
        Clock(
            timestamp = timestamp,
            onClose = todoForm::updateHoursMins
        )
    }
}

@Composable
fun CTAButton(isEnabled: Boolean, buttonText: String, onDone: () -> Unit) {
    Box(
        modifier = Modifier.testTag(TodoTags.ADD_EDIT_TODO_BUTTON).padding(vertical = 16.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            enabled = isEnabled,
            onClick = {
                println("CTA button clicked")
                onDone()
            },
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
fun ViewTodo(
    currentTodoItem: TodoItem,
    onUpdate: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        TextBox(label = "Title", text = currentTodoItem.title)
        TextBox(label = "Description", text = currentTodoItem.description)
        TextBox(label = "To Complete By", text = formatDate(currentTodoItem.completionTime))
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row {
                IconButton(onClick = { onDelete(currentTodoItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_delete_24),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
                IconButton(onClick = { onUpdate(currentTodoItem)
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
fun TextBox(label: String, text: String?) {
    Text(
        text = label,
        style = TextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Box(
        modifier = Modifier
            .heightIn(min = if (label == "Description") 60.dp else 24.dp)
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
        modifier = Modifier.testTag(TodoTags.TODO_FORM_TITLE).padding(vertical = 8.dp).fillMaxWidth()
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
        modifier = Modifier.testTag(TodoTags.TODO_FORM_DESCRIPTION).padding(vertical = 8.dp).fillMaxWidth()
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
            modifier = Modifier.testTag(TodoTags.TODO_FORM_DATE).fillMaxWidth(.6f).padding(end = 8.dp),
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
            modifier = Modifier.testTag(TodoTags.TODO_FORM_TIME),
            interactionSource = interactionScope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Clock(timestamp: Long, onClose: (Long, Long) -> Unit) {
    val (hours, mins) = with (Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("GMT"))) {
        Pair(hour, minute)
    }
    val timePickerState = TimePickerState(hours, mins, true)
    BasicAlertDialog(onDismissRequest = {
        onClose(timePickerState.hour.toLong(), timePickerState.minute.toLong())
    }) {
        TimePicker(timePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(initialTimestamp: Long, onClose: (Long?) -> Unit) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTimestamp)
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = Color.White.copy(alpha = 0.5f)),
        verticalArrangement = Arrangement.Center,
    ) {
        DatePickerDialog(
            confirmButton = {
                Button(
                    onClick = { onClose(datePickerState.selectedDateMillis) }
                ) {
                    Text("Done")
                }
            },
            onDismissRequest = { onClose(datePickerState.selectedDateMillis) }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.testTag(TodoTags.DATE_PICKER)
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