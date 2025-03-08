package com.example.meetuptodoapp.todos.ui.widgets

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DateSelectorTest {
    @get:Rule
    val paparazziRule = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    // could probably snapshot the various different screens
    @Test
    fun `Actual datepicker invoked via DateSelector component`() = runTest {
        paparazziRule.snapshot {
            DateSelectorImpl(initialTimestamp = 0L, onClose = {})
        }
    }
}