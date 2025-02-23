package com.example.meetuptodoapp

import android.content.Context
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeUp
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.example.meetuptodoapp.api.TodoClient
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.domain.model.TodosSerializer
import io.ktor.client.request.post
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AllTodosTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = UnconfinedTestDispatcher()
    val scope = TestScope(dispatcher)

    private lateinit var context: Context
    private lateinit var datastore: DataStore<Todos>

    companion object {
        lateinit var oStream: OutputStream
    }

    object TestSerializer: Serializer<Todos> {
        override val defaultValue: Todos
            get() = Todos(todos = listOf())

        @OptIn(ExperimentalSerializationApi::class)
        override suspend fun readFrom(input: InputStream): Todos {
            return Json.decodeFromStream(input)
        }

        @OptIn(ExperimentalSerializationApi::class)
        override suspend fun writeTo(t: Todos, output: OutputStream) {
            Log.i("COMMIT", "in writeTo")
//            oStream = FileOutputStream(file)
            oStream = output
            Json.encodeToStream(t, output)
        }
    }

    class TestStore(context: Context): DataStore<Todos> {
        val produceFile = File(context.filesDir, "datastore/TODOSTEST")
        val impl = DataStoreFactory.create(
            serializer = TestSerializer,
            produceFile = {
                produceFile
            }
        )
        override val data: Flow<Todos>
            get() = impl.data

        override suspend fun updateData(transform: suspend (t: Todos) -> Todos): Todos {
            return impl.updateData { transform(it) }
        }
    }

    @Before
    fun setup() = runTest {
//        mockkStatic(UUID::class)
//        val mockUUID = mockk<UUID>()
//        every { mockUUID.toString() } returns "935b2608-e45b-4ede-ba9f-c22d294d0307"
//        every { mockUUID.toString() } returns "11742402294532"
//        every { UUID.randomUUID() } returns mockUUID
//        val application: TodoApplication = RuntimeEnvironment.getApplication() as TodoApplication
//        datastore = application.todoDataStore
//        val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
//        field.isAccessible = true
//        field.set(application, TestStore(application))
//        println("test")
//        datastore.updateData { Todos(todos = listOf(TodoItem("some-title"))) }
//        val mutStateFlow = MutableStateFlow(Todos(listOf()))
//        val stateFlow: MutableStateFlow<Todos> = mutStateFlow
//        val activity = rule.activity
//        val field = MainActivity::class.java.getDeclaredField("todoFlow")
//        field.isAccessible = true
//        field.set(activity, stateFlow)
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
    }

    @Test
    fun testMakeTodo() = runTest(dispatcher) {
        val application: TodoApplication = RuntimeEnvironment.getApplication() as TodoApplication
        val expected =
            "{\"todos\":[{\"id\":\"ID\",\"title\":\"Donald Duck\",\"description\":\"Mickey Mouse\",\"timestamp\":TIMESTAMP,\"completionTime\":1740787140000}]}"

        var field = MainActivity::class.java.getDeclaredField("repo")
        field.isAccessible = true
        val repo = field.get(rule.activity)
        field = TodoRepository::class.java.getDeclaredField("todoClient")
        field.isAccessible = true
        val client: TodoClient = field.get(repo) as TodoClient
        val spyClient = spyk(client)
        coEvery { spyClient.post(any(), any()) }.answers { }

        field.set(repo, spyClient)
        field = MainActivity::class.java.getDeclaredField("todoDataStore")
        field.isAccessible = true
        val todoFlow = (field.get(rule.activity) as TodoStore).data

        rule.onRoot().onChild().onChildAt(4).performClick()
        val roots = rule.onAllNodes(isRoot())
        val parent = roots[1].onChildAt(0).onChildAt(0).onChildAt(1)
        parent.onChildAt(1).requestFocus().performTextInput("Donald Duck")
        parent.onChildAt(2).requestFocus().performTextInput("Mickey Mouse")
        parent.onChildAt(5).performClick()
        testScheduler.advanceUntilIdle()
        rule.onAllNodes(isRoot())[1].assertIsDisplayed()

        parent.onChildAt(3).requestFocus()
        roots[2].onChildAt(0).onChildAt(0).onChildAt(0)
            .onChildAt(0).onChildAt(3).onChildAt(10)
            .onChildAt(27).performClick()
        rule.onNodeWithText("Done").performClick()
        rule.activity.onBackPressedDispatcher.onBackPressed()
        val date = Instant.now().atZone(ZoneId.of("GMT")).withDayOfMonth(28)
        val dayStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)
        val dateStr = "$dayStr, 28/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
        rule.onNodeWithText(dateStr).assertIsDisplayed()

        parent.onChildAt(0).performTouchInput { swipeUp() }
        parent.onChildAt(5).performClick()
        testScheduler.advanceUntilIdle()
        rule.onAllNodes(isRoot())[2].assertIsNotDisplayed()

        // FIXME (delete) Note: we haven't yet finished repo call so modal still visible
        var actual: String
        withContext(Dispatchers.Unconfined) {
            runBlocking { delay(1000) }
            actual = String(FileInputStream(File( application.filesDir, "datastore/TODOS")).readAllBytes())
                .replace(Regex("\"(id)\":\"[a-z0-9\\-]*\""), "\"$1\":\"ID\"")
                .replace(Regex("\"(timestamp)\":[0-9]*"), "\"$1\":TIMESTAMP")
        }
        rule.onAllNodes(isRoot())[1].assertIsNotDisplayed()
        assertEquals(expected, actual)

        val todos = todoFlow.first().todos
        assertEquals(1, todos.size)
        rule.onNodeWithText(todos.first().title)
            .assertIsDisplayed()
        rule.onNodeWithText(todos.first().description)
            .assertIsDisplayed()
    }

    @Test
    fun `it fetches todos from disk and displays them on screen`() = runTest(dispatcher) {
        rule.onRoot().onChild().onChildAt(4).performClick()
        rule.onNodeWithText("testButton").performClick()
        withContext(dispatcher) {
            rule.runOnIdle {
                rule.onNodeWithTag("testing", useUnmergedTree = true).performClick()
            }
        }
//        datastore.updateData { Todos(todos = listOf(TodoItem("some-title"))) }
//        val mutStateFlow = MutableStateFlow(Todos(listOf()))
//        val stateFlow: MutableStateFlow<Todos> = mutStateFlow
//        val activity = rule.activity
//        val field = MainActivity::class.java.getDeclaredField("todoFlow")
//        field.isAccessible = true
//        field.set(activity, stateFlow)
//        rule.activity.setContent { }
////        rule.setContent {
////        }

        rule.onNodeWithText("Testing Compose").assertIsDisplayed()
    }

}