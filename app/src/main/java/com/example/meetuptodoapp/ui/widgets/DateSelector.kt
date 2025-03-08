package com.example.meetuptodoapp.ui.widgets

//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.material3.Button
//import androidx.compose.material3.DatePicker
//import androidx.compose.material3.DatePickerDialog
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberDatePickerState
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.testTag
//import com.example.meetuptodoapp.ui.tags.TodoTags
//
//interface DateSelector {
//    @Composable
//    operator fun invoke(initialTimestamp: Long, onClose: (Long?) -> Unit)
//}
//
//object DateSelectorImpl: DateSelector {
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    override fun invoke(initialTimestamp: Long, onClose: (Long?) -> Unit) {
//        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTimestamp)
//        Column(
//            modifier = Modifier
//                .fillMaxHeight()
//                .background(color = Color.White.copy(alpha = 0.5f)),
//            verticalArrangement = Arrangement.Center,
//        ) {
//            DatePickerDialog(
//                confirmButton = {
//                    Button(
//                        onClick = { onClose(datePickerState.selectedDateMillis) }
//                    ) {
//                        Text("Done")
//                    }
//                },
//                onDismissRequest = { onClose(datePickerState.selectedDateMillis) }
//            ) {
//                DatePicker(
//                    state = datePickerState,
//                    modifier = Modifier.testTag(TodoTags.DATE_PICKER)
//                )
//            }
//        }
//    }
//}