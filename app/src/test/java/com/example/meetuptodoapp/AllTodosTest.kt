package com.example.meetuptodoapp

import android.content.Context.MODE_PRIVATE
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AllTodosTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOldTodos() {
        val application = spyk(ApplicationProvider.getApplicationContext<TodoApplication>())
        val prefs = application.getSharedPreferences("todos", MODE_PRIVATE)
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
        val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
        field.isAccessible = true
        field.set(application, testDataStore)
        prefs.edit()
            .putString("TODOS", "{\"todos\":[{\"id\":\"${UUID.randomUUID()}\",\"title\":\"Donald Duck\",\"description\":\"Watch Donald Duck\",\"timestamp\":${Instant.now().toEpochMilli()},\"completionTime\":${Instant.now().toEpochMilli() + 3600000}}]}")
            .commit()
        composeTestRule.setContent {
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
                    timestamp = Instant.now().toEpochMilli(),
                    completionTime = Instant.now().toEpochMilli() + 3600000
                ),
                TodoItem(
                    id = UUID.randomUUID().toString(),
                    title = "Mickey Mouse",
                    description = "Hide and seek with Donald Duck",
                    timestamp = Instant.now().toEpochMilli() + 3600000,
                    completionTime = Instant.now().toEpochMilli() + 3600000,
                    completedTime = Instant.now().toEpochMilli()
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
    }

//    @Test
//    fun testOldTodos3() {
//        rule.setContent {  }
//    }

    private fun createTestTodoStore(application: TodoApplication): DataStore<Todos> {
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
        val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
        field.isAccessible = true
        field.set(application, testDataStore)
        return testDataStore
    }

//    @Test
//    fun testTodos() {
//        // I want to show that I'm seeing all the todos that came out of prefs
//        val prefs = rule.activity.getSharedPreferences("todos", MODE_PRIVATE)
//        prefs.edit()
//            .putString(
//                "TODOS", """
//                    {"todos":[
//                        {
//                            "id":"ID",
//                            "title":"Donald Duck",
//                            "description":"Mickey Mouse",
//                            "timestamp":1740786140000,
//                            "completionTime":1740787140000
//                        }
//                    ]
//                }"""
//            ).commit()
//        waitForUIReady()
//        rule.activity.setContent {
//            TodoScreenRoot()
//        }
//        waitForUIReady()
//        val todoDataStore = (rule.activity.application as TodoApplication).todoDataStore
//        val lifecycleScope = rule.activity.lifecycleScope
//        val todos = runBlocking { todoDataStore.data.stateIn(lifecycleScope).value }
//        println("test")
//    }
//
//    @Test
//    fun testFab() = runTest(dispatcher) {
//        val application: TodoApplication = RuntimeEnvironment.getApplication() as TodoApplication
//        val expected =
//            "{\"todos\":[{\"id\":\"ID\",\"title\":\"Donald Duck\",\"description\":\"Mickey Mouse\",\"timestamp\":TIMESTAMP,\"completionTime\":1740787140000}]}"
//
//        var field = MainActivity::class.java.getDeclaredField("repo")
//        field.isAccessible = true
//        val repo = field.get(rule.activity)
//        field = TodoRepository::class.java.getDeclaredField("todoClient")
//        field.isAccessible = true
//        val client: TodoClient = field.get(repo) as TodoClient
//        val spyClient = spyk(client)
//        coEvery { spyClient.post(any(), any()) }.answers { }
//
//        field.set(repo, spyClient)
//        field = MainActivity::class.java.getDeclaredField("todoDataStore")
//        field.isAccessible = true
//        val todoFlow = (field.get(rule.activity) as TodoStore).data
//
//        rule.onRoot().onChild().onChildAt(4).performClick()
//        val roots = rule.onAllNodes(isRoot())
//        val parent = roots[1].onChildAt(0).onChildAt(0).onChildAt(1)
//        parent.onChildAt(1).requestFocus().performTextInput("Donald Duck")
//        parent.onChildAt(2).requestFocus().performTextInput("Mickey Mouse")
//        parent.onChildAt(5).performClick()
//        testScheduler.advanceUntilIdle()
//        rule.onAllNodes(isRoot())[1].assertIsDisplayed()
//
//        parent.onChildAt(3).requestFocus()
//        roots[2].onChildAt(0).onChildAt(0).onChildAt(0)
//            .onChildAt(0).onChildAt(3).onChildAt(10)
//            .onChildAt(27).performClick()
//        rule.onNodeWithText("Done").performClick()
//        rule.activity.onBackPressedDispatcher.onBackPressed()
//        val date = Instant.now().atZone(ZoneId.of("GMT")).withDayOfMonth(28)
//        val dayStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)
//        val dateStr = "$dayStr, 28/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
//        rule.onNodeWithText(dateStr).assertIsDisplayed()
//
//        parent.onChildAt(0).performTouchInput { swipeUp() }
//        parent.onChildAt(5).performClick()
//        testScheduler.advanceUntilIdle()
//        rule.onAllNodes(isRoot())[2].assertIsNotDisplayed()
//
//        // FIXME (delete) Note: we haven't yet finished repo call so modal still visible
//        var actual: String
//        withContext(Dispatchers.Unconfined) {
//            runBlocking { delay(1000) }
//            actual = String(FileInputStream(File( application.filesDir, "datastore/TODOS")).readAllBytes())
//                .replace(Regex("\"(id)\":\"[a-z0-9\\-]*\""), "\"$1\":\"ID\"")
//                .replace(Regex("\"(timestamp)\":[0-9]*"), "\"$1\":TIMESTAMP")
//        }
//        rule.onAllNodes(isRoot())[1].assertIsNotDisplayed()
//        assertEquals(expected, actual)
//
//        val todos = todoFlow.first().todos
//        assertEquals(1, todos.size)
//        rule.onNodeWithText(todos.first().title)
//            .assertIsDisplayed()
//        rule.onNodeWithText(todos.first().description)
//            .assertIsDisplayed()
//    }
//
//    @Test
//    fun `it fetches todos from disk and displays them on screen`() = runTest(dispatcher) {
//        rule.onRoot().onChild().onChildAt(4).performClick()
//        rule.onNodeWithText("testButton").performClick()
//        withContext(dispatcher) {
//            rule.runOnIdle {
//                rule.onNodeWithTag("testing", useUnmergedTree = true).performClick()
//            }
//        }
////        datastore.updateData { Todos(todos = listOf(TodoItem("some-title"))) }
////        val mutStateFlow = MutableStateFlow(Todos(listOf()))
////        val stateFlow: MutableStateFlow<Todos> = mutStateFlow
////        val activity = rule.activity
////        val field = MainActivity::class.java.getDeclaredField("todoFlow")
////        field.isAccessible = true
////        field.set(activity, stateFlow)
////        rule.activity.setContent { }
//////        rule.setContent {
//////        }
//
//        rule.onNodeWithText("Testing Compose").assertIsDisplayed()
//    }

}