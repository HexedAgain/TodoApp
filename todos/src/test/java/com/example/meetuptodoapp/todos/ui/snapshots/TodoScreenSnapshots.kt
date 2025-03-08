package com.example.meetuptodoapp.todos.ui.snapshots

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.example.meetuptodoapp.todos.domain.model.TodoItem
import com.example.meetuptodoapp.todos.domain.model.Todos
import com.example.meetuptodoapp.todos.storage.TodoSharedPrefs
import com.example.meetuptodoapp.todos.storage.TodoStorage
import com.example.meetuptodoapp.todos.ui.screen.TodoScreen
import com.example.meetuptodoapp.todos.ui.viewmodel.IdSupplier
import com.example.meetuptodoapp.todos.ui.viewmodel.TimeSupplier
import com.example.meetuptodoapp.todos.ui.viewmodel.TodoForm
import com.example.meetuptodoapp.todos.ui.viewmodel.TodoViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
class TodoScreenSnapshots {
    @get:Rule
    val paparazziRule = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    private val mockTodoStorage: TodoStorage = mockk()
    private var dummyFlow: MutableStateFlow<Todos> = MutableStateFlow(Todos.default())
    private val mockTodoPrefs: TodoSharedPrefs = mockk()
    private val mockTimeSupplier: TimeSupplier = mockk()
    private val mockIdSupplier: IdSupplier = mockk()

    private val todoItem = TodoItem("1", "Todo item 1", "One line description", completionTime = TIME_NOW)
    private val anotherTodoItem = TodoItem("2", "Todo item 2", "description that will span more than a single line", completionTime = TIME_NOW)
    private val todoForm = TodoForm(todoItem, mockTimeSupplier)
    private val anotherTodoForm = TodoForm(anotherTodoItem, mockTimeSupplier)

    private lateinit var viewModel: TodoViewModel
    //private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TodoViewModel(
            todoStorage = mockTodoStorage,
            ioDispatcher = testDispatcher,
            todoPrefs = mockTodoPrefs
        )
        every { mockTodoStorage.readAsFlow() }.returns(dummyFlow)
        coEvery { mockTodoStorage.write(any()) }.answers { answer ->
            val todosList = answer.invocation.args.first() as List<TodoItem>
            val todos = Todos(todos = todosList)
            dummyFlow.value = Todos(todos = todosList)
            todos
        }
        coEvery { mockTodoStorage.setMigrated() }.answers {}
        every { mockTimeSupplier.now() }.returns(TIME_NOW)
        every { mockIdSupplier.id() }.returns("some-id")
        coEvery { mockTodoPrefs.readTodos() }.returns(Todos.default())
    }

    @Test
    fun `todo screen with one uncompleted TODO and one completed TODO, see completed checkbox unchecked`() = runTest {
        viewModel.collectTodos()
        viewModel.onTodoDone(todoForm)
        viewModel.onTodoDone(anotherTodoForm)
        viewModel.onToggleComplete(anotherTodoItem)

        SnapshotTodoScreen()
    }

    @Test
    fun `todo screen with one uncompleted TODO and one completed TODO, see completed checkbox is checked`() = runTest {
        viewModel.collectTodos()
        viewModel.onTodoDone(todoForm)
        viewModel.onTodoDone(anotherTodoForm)
        viewModel.onToggleComplete(anotherTodoItem)
        viewModel.toggleShowCompleted()

        SnapshotTodoScreen()
    }

    @Test
    fun `todo screen where the FAB has been clicked`() = runTest {
        viewModel.collectTodos()
        viewModel.onTodoDone(todoForm)
        viewModel.onCreateTodo()

        SnapshotTodoScreen(expandedModal = true)
    }

    @Test
    fun `todo screen where a TODO has been clicked`() = runTest {
        viewModel.collectTodos()
        viewModel.onTodoDone(todoForm)
        viewModel.onViewTodo(todoItem)

        SnapshotTodoScreen(expandedModal = true)
    }

    @Test
    fun `todo screen where a TODO has been clicked then marked for edit`() = runTest {
        viewModel.collectTodos()
        viewModel.onTodoDone(todoForm)
        viewModel.onUpdateTodo(todoItem)

        SnapshotTodoScreen(expandedModal = true)
    }

    private fun SnapshotTodoScreen(expandedModal: Boolean = false) {
        paparazziRule.snapshot {
            val modalBottomSheetState = rememberModalBottomSheetState()
            if (expandedModal) {
                LaunchedEffect(Unit) { modalBottomSheetState.show() }
            }

            TodoScreen(viewModel, modalBottomSheetState)
        }
    }

//    private fun simulateAdvancingTime() {
//        var counter = 0
//        every { mockTimeSupplier.now() }.returns(TIME_NOW)
//    }

    companion object {
        const val TIME_NOW = 123L
    }
}