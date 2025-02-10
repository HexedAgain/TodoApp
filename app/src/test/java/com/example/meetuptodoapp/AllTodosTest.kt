package com.example.meetuptodoapp

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.core.DataStore
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.model.todoDatastore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
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
    val rule = createAndroidComposeRule<MainActivity>()

    private lateinit var context: Context
    private lateinit var datastore: DataStore<Todos>

    @Before
    fun setup() = runTest {
        context = RuntimeEnvironment.getApplication()
        datastore = context.todoDatastore
        datastore.updateData { Todos(todos = listOf(TodoItem("some-title"))) }
        val mutStateFlow = MutableStateFlow(Todos(listOf()))
        val stateFlow: MutableStateFlow<Todos> = mutStateFlow
        val activity = rule.activity
        val field = MainActivity::class.java.getDeclaredField("todoFlow")
        field.isAccessible = true
        field.set(activity, stateFlow)
    }

    @Test
    fun testOnScreenLoadShowTodos() {

    }
    @Test
    fun testShowUpdatedTodo() {
        // this one is gonna show that for some initial todos, when they get updated then the new todos
        // respect the update
    }
    @Test
    fun testAddNewTodo() {
        // this one is gonna show that for some initial todos, if the fab is selected and a todo
        // selected then the screen shows the new todo
    }q

    @Test
    fun `it fetches todos from disk and displays them on screen`() = runTest {
        datastore.updateData { Todos(todos = listOf(TodoItem("some-title"))) }
        val mutStateFlow = MutableStateFlow(Todos(listOf()))
        val stateFlow: MutableStateFlow<Todos> = mutStateFlow
        val activity = rule.activity
        val field = MainActivity::class.java.getDeclaredField("todoFlow")
        field.isAccessible = true
        field.set(activity, stateFlow)
        rule.activity.setContent { }
//        rule.setContent {
//        }

        rule.onNodeWithText("Testing Compose").assertIsDisplayed()
    }

}