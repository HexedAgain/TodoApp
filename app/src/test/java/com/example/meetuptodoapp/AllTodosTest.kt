package com.example.meetuptodoapp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.core.DataStore
import com.example.meetuptodoapp.model.Todos
import com.example.meetuptodoapp.model.todoDatastore
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AllTodosTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var context: Context
    private lateinit var datastore: DataStore<Todos>

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        datastore = context.todoDatastore
    }

    @Test
    fun `it fetches todos from disk and displays them on screen`() = runTest {
        datastore.updateData { Todos(todos = listOf(com.example.meetuptodoapp.model.TodoItem("some-title"))) }
        rule.setContent {
            AllTodos()
        }

        rule.onNodeWithText("Testing Compose").assertIsDisplayed()
    }

}