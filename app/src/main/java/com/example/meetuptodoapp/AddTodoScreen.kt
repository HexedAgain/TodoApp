package com.example.meetuptodoapp

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Locale

@Composable
fun AddTodoScreen(onDone: () -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val description = remember { mutableStateOf("")}
    val title = remember { mutableStateOf("")}
    val date = remember { mutableStateOf("")}
    fun isValid(): Boolean {
        return title.value.isNotEmpty() && description.value.isNotEmpty() && date.value.isNotEmpty()
    }
    Box(
        modifier = Modifier.padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
//            Spacer(modifier = Modifier.weight(.20f))
            TitleSection(title)
            TodoDescription(description)
            ToBeDoneBy(date) { showDatePicker = true }
            Box(
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    enabled = isValid(),
                    onClick = {
                        if (title.value.isNotEmpty() && description.value.isNotEmpty()) {
                            // Add the todo
                            onDone()
                        }
                    }
                ) {
                    Text(text = "Add TODO")
                }
            }
            Spacer(modifier = Modifier.weight(.5f))
            Spacer(modifier = Modifier.weight(.20f))
        }
        if (showDatePicker) {
            Calendar {
                showDatePicker = false
            }
        }
    }
}

@Composable
fun TitleSection(title: MutableState<String>) {
    OutlinedTextField(
        label = { Text(text = "Title") },
        placeholder = {},
        value = title.value,
        onValueChange = { newValue: String ->
            title.value = newValue
        },
        singleLine = true,
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
    )
}

@Composable
fun ColumnScope.TodoDescription(description: MutableState<String>) {
    OutlinedTextField(
        label = { Text(text = "Description") },
        value = description.value,
        onValueChange = { newDescription ->
            description.value = newDescription
        },
        singleLine = false,
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().weight(.25f)
    )
}

@Composable
fun ToBeDoneBy(date: MutableState<String>, onSelected: () -> Unit) {
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
    OutlinedTextField(
        label = { Text(text = "To Be Done By") },
        value = "",
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.baseline_calendar_month_24),
                contentDescription = null
            )
        },
        interactionSource = interactionScope
//        modifier = Modifier.clickable {
//            showDatePicker = true
//        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(onClose: () -> Unit) {
    val datePickerState = DatePickerState(locale = Locale.UK)
    val enabled = remember { mutableStateOf(false) }
    val size = animateFloatAsState(
        targetValue =  if (enabled.value) .75f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
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
            onDismissRequest = { onClose() }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
//                .fillMaxHeight(size.value)
//                    .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
//                    .shadow(elevation = 4.dp)
//                .clickable { }
            )
        }
    }
}