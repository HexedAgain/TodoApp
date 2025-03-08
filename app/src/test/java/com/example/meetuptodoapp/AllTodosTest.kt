package com.example.meetuptodoapp

import android.content.Context.MODE_PRIVATE
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ApplicationProvider
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AllTodosTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val time1 = Instant.now().toEpochMilli() + 3600000
    val time2 = Instant.now().toEpochMilli()

    val dateStr  = Instant.now().atZone(ZoneId.of("GMT")).withDayOfMonth(28)
        .run { "${dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)}, 28/${monthValue.toString().padStart(2, '0')}/${year}" }

    @Test
    fun testOldTodos() {
        val flow = MutableStateFlow(Todos.default())
        val testDataStore: TodoStore = mockk()
        coEvery { testDataStore.data }.returns(flow)
        coEvery { testDataStore.updateData(any()) }.answers { callContext ->
            val firstArg = callContext.invocation.args.first()
            val field = firstArg!!::class.java.declaredFields.find { it.name == "\$legacyTodos" }
            val todos: Todos = field!!.get(firstArg) as Todos
            flow.value = todos
            todos
        }
        composeTestRule.setContent {
            val application = LocalContext.current.applicationContext
            val prefs = application.getSharedPreferences("todos", MODE_PRIVATE)
            prefs.edit()
                .putString("TODOS", "{\"todos\":[{\"id\":\"${UUID.randomUUID()}\",\"title\":\"Donald Duck\",\"description\":\"Watch Donald Duck\",\"timestamp\":${Instant.now().toEpochMilli()},\"completionTime\":$time1}]}")
                .commit()
            val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
            field.isAccessible = true
            field.set(application, testDataStore)
            TodoScreenRoot()
        }
        composeTestRule.onNodeWithText("Donald Duck").assertIsDisplayed()
        composeTestRule.onNodeWithText("Watch Donald Duck").assertIsDisplayed()
    }

    @Test
    fun testNewTodos() {
        val flow = MutableStateFlow(Todos.default())
        val testDataStore: TodoStore = mockk()
        coEvery { testDataStore.data }.returns(flow)
        coEvery { testDataStore.updateData(any()) }.answers { callContext ->
            val firstArg = callContext.invocation.args.first()
            val field = firstArg!!::class.java.declaredFields.find { it.name == "\$legacyTodos" }
            val todos: Todos = field!!.get(firstArg) as Todos
            flow.value = todos
            todos
        }
        val todos = Todos(
            isMigrated = true,
            todos = listOf(
                TodoItem(
                    id = UUID.randomUUID().toString(),
                    title = "Donald Duck",
                    description = "Watch Donald Duck",
                    timestamp = time1,
                    completionTime = time1
                ),
                TodoItem(
                    id = UUID.randomUUID().toString(),
                    title = "Mickey Mouse",
                    description = "Hide and seek with Donald Duck",
                    timestamp = time1,
                    completionTime = time1,
                    completedTime = time2
                )
            )
        )
        flow.value = todos
        composeTestRule.setContent {
            val application = LocalContext.current.applicationContext
            val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
            field.isAccessible = true
            field.set(application, testDataStore)
            TodoScreenRoot()
        }
        composeTestRule.onNodeWithText("Donald Duck").assertIsDisplayed()
        composeTestRule.onNodeWithText("Watch Donald Duck").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mickey Mouse").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Hide and seek with Donald Duck").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("checkBox").performClick()
        composeTestRule.onNodeWithText("Mickey Mouse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide and seek with Donald Duck").assertIsDisplayed()
    }

    @Test
    fun testFab() = runTest {
        lateinit var activity: ComponentActivity
        val flow = MutableStateFlow(Todos(isMigrated = true, todos = listOf()))
        val testDataStore: TodoStore = mockk()
        coEvery { testDataStore.data }.returns(flow)
        coEvery { testDataStore.updateData(any()) }.answers { callContext ->
            val firstArg = callContext.invocation.args.first()
            val field = firstArg!!::class.java.declaredFields.find { it.name == "\$thisTodo"}
            val todos = Todos( todos = listOf(field!!.get(firstArg) as TodoItem))
            flow.value = todos
            todos
        }
        composeTestRule.setContent {
            activity = LocalActivity.current as ComponentActivity
            val application = activity.applicationContext
            val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
            field.isAccessible = true
            field.set(application, testDataStore)
            TodoScreenRoot()
        }
        composeTestRule.onNodeWithTag("FAB").performClick()
        val roots = composeTestRule.onAllNodes(isRoot())
        val parent = roots[1].onChildAt(0).onChildAt(0).onChildAt(1)
        parent.onChildAt(1).requestFocus().performTextInput("Donald Duck")
        parent.onChildAt(2).requestFocus().performTextInput("Watch Donald Duck")
        parent.onChildAt(5).performClick()
        parent.onChildAt(3).requestFocus()
        roots[2].onChildAt(0).onChildAt(0).onChildAt(0)
            .onChildAt(0).onChildAt(3).onChildAt(10)
            .onChildAt(27).performClick()
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.onNodeWithText(dateStr).assertIsDisplayed()
        parent.onChildAt(5).performClick()
        composeTestRule.onNodeWithTag("DatePicker").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("Modal").performTouchInput { swipeDown() }
        composeTestRule.onNodeWithTag("Modal").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Donald Duck").assertIsDisplayed()
        composeTestRule.onNodeWithText("Watch Donald Duck").assertIsDisplayed()
    }
}