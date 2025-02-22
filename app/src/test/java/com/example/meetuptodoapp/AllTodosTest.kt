package com.example.meetuptodoapp

import android.content.Context
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.domain.model.TodosSerializer
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AllTodosTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

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
    fun testSaveTodo() = runTest {
        val application: TodoApplication = RuntimeEnvironment.getApplication() as TodoApplication
        val file = File(application.filesDir, "datastore/TODOSTEST")
        val field = TodoApplication::class.java.getDeclaredField("todoDataStore")
        field.isAccessible = true
        field.set(application, object: DataStore<Todos> {
            val impl = DataStoreFactory.create(
                serializer = TestSerializer,
                produceFile = { file }
            )
            override val data: Flow<Todos>
                get() = impl.data

            override suspend fun updateData(transform: suspend (t: Todos) -> Todos): Todos {
                return impl.updateData { transform(it) }
            }
        })
        datastore = application.todoDataStore
        // assert here that the file exists maybe... perhaps show that the file is initially empty?
        rule.onRoot().onChild().onChildAt(4).performClick()
        runBlocking { delay(1000) }
        val roots = rule.onAllNodes(isRoot())
        var parent = roots[1].onChildAt(0).onChildAt(0).onChildAt(1)
        parent.onChildAt(1).requestFocus().performTextInput("Donald Duck")
        parent.onChildAt(2).requestFocus().performTextInput("Mickey Mouse")
        parent.onChildAt(3).requestFocus()
        runBlocking { delay(1000) }
        parent = roots[2]
        rule.mainClock.advanceTimeBy(1000)
        rule.activity
//        File(application.filesDir, "datastore/TODOSTEST").createNewFile()
//        oStream = FileOutputStream(File(application.filesDir, "datastore/TODOSTEST"))
//        oStream = FileOutputStream(file)
//        val json = "{\"todos\":[{\"id\":\"4d7a3376-b6ab-4b24-b8c4-b7b4ea3aaa9b\",\"title\":\"Get a quote for car insurance\",\"description\":\"Find the cheapest quote for car insurance\",\"timestamp\":1740240154864},{\"id\":\"d57eaac9-dbdc-4d66-b5fd-56e53b6d55a7\",\"title\":\"Weekly shopping\",\"description\":\"Starting to run low on food, make a quick trip to TESCOs\",\"timestamp\":1740240154864}]}"
//        oStream.write(json.toByteArray())
//        val thing = SingleProcessDataStore::class.java
//        runBlocking {
//            datastore.updateData {
//                Todos(
//                    todos = listOf(
//                        TodoItem(
//                            id = UUID.randomUUID().toString(),
//                            title = "Get a quote for car insurance",
//                            description = "Find the cheapest quote for car insurance",
//                            timestamp = Instant.now().toEpochMilli() + 10000000
//                        ),
//                        TodoItem(
//                            id = UUID.randomUUID().toString(),
//                            title = "Weekly shopping",
//                            description = "Starting to run low on food, make a quick trip to TESCOs",
//                            timestamp = Instant.now().toEpochMilli() + 10000000
//                        )
//                    )
//                )
//            }
//        }
//        val thing = ""
    }

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