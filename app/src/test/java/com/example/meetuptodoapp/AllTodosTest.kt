package com.example.meetuptodoapp

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import junit.framework.TestCase.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AllTodosTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun textIsDisplayed() {
        rule.setContent {
            Text("Testing Compose")
        }

        rule.onNodeWithText("Testing Compose").assertIsDisplayed()
    }

}