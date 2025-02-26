package com.example.meetuptodoapp

import com.example.meetuptodoapp.TodoViewModel.UIMode.Create
import com.example.meetuptodoapp.TodoViewModel.UIMode.ViewAll
import com.example.meetuptodoapp.TodoViewModel.UIMode.Update
import com.example.meetuptodoapp.TodoViewModel.UIMode.ViewSingle
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.storage.TodoStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    private lateinit var viewModel: TodoViewModel
    private var dummyFlow: MutableStateFlow<Todos> = MutableStateFlow(Todos.default())
    private val mockTodoStorage: TodoStorage = mockk()
    private val mockRepository: TodoRepository = mockk()
    private val mockTimeSupplier: TimeSupplier = mockk()
    private val mockIdSupplier: IdSupplier = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dummyTodo = TodoItem(
        id = "some-id",
        title = "some-title",
        description = "some-description",
    )
    private val anotherDummyTodo = dummyTodo.copy(id = "some-other-todo")
    private val completedTodo = dummyTodo.copy(completedTime = TIME_NOW - 1, id = "some-completed-id")
    private val uninitialisedForm = TodoForm(currentTodo = null, timeSupplier = mockTimeSupplier)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockTodoStorage.readAsFlow() }.returns(dummyFlow)
        coEvery { mockTodoStorage.write(any()) }.answers {  answer ->
            val todosList = answer.invocation.args.first() as List<TodoItem>
            val todos = Todos(todos = todosList)
            dummyFlow.value = Todos(todos = todosList)
            todos
        }
        coEvery { mockRepository.logTodoStats(any(), any()) }.answers { }
        every { mockTimeSupplier.now() }.returns(TIME_NOW)
        every { mockIdSupplier.id() }.returns("some-id")

        reInitialiseViewModel()
    }

    private fun reInitialiseViewModel() = runTest {
        viewModel = TodoViewModel(
            todoStorage = mockTodoStorage,
            repository = mockRepository,
            timeSupplier = mockTimeSupplier,
            idSupplier = mockIdSupplier
        )
    }

    @Test
    fun `initialise, it publishes all TODOs that are uncompleted`() = runTest {
        dummyFlow.value = Todos(todos = listOf(dummyTodo, anotherDummyTodo, completedTodo))

        assertEquals(dummyTodo, viewModel.todos.value.first())
        assertEquals(anotherDummyTodo, viewModel.todos.value.last())
    }

    @Test
    fun `toggleCompleted, if toggled true, it publishes all TODOs`() {
        dummyFlow.value = Todos(todos = listOf(dummyTodo, completedTodo))

        viewModel.toggleShowCompleted()

        assertEquals(listOf(dummyTodo, completedTodo), viewModel.todos.value)
    }

    @Test
    fun `toggleCompleted, if toggled false, it publishes all TODOs that are uncompleted`() {
        dummyFlow.value = Todos(todos = listOf(dummyTodo, completedTodo))

        assertEquals(listOf(dummyTodo), viewModel.todos.value)
    }

    @Test
    fun `onToggleComplete, when invoked on an un-completed todo it sets the completion time as now`() {
        dummyFlow.value = Todos(todos = listOf(dummyTodo))

        viewModel.onToggleComplete(dummyTodo)

        assertEquals(TIME_NOW, viewModel.todos.value.first().completedTime)
    }

    @Test
    fun `onToggleComplete, when invoked on a completed todo it sets the completion time as Long MAX_VALUE`() {
        dummyFlow.value = Todos(todos = listOf(completedTodo))

        viewModel.onToggleComplete(completedTodo)

        assertEquals(Long.MAX_VALUE, viewModel.todos.value.first().completedTime)
    }

    @Test
    fun `onViewTodo, it sets uiMode to ViewSingle, equipped with supplied TODO`() = runTest {
        viewModel.onViewTodo(dummyTodo)

        with ((viewModel.uiMode.value as ViewSingle)) {
            assertEquals(dummyTodo, this.todo)
        }
    }

    @Test
    fun `onCreateTodo, it sets uiMode to Create, equipped with an uninitialised form`() = runTest {
        viewModel.onCreateTodo()

        with ((viewModel.uiMode.value as Create).todoForm) {
            assertEquals(null, currentTodo)
            assertEquals(title.value, "")
            assertEquals(description.value, "")
            assertEquals(timestamp.value, -1L)
        }
    }

    @Test
    fun `onUpdateTodo, it sets uiMode to Update, equipped with form consistent with supplied TODO`() = runTest {
       viewModel.onUpdateTodo(dummyTodo)

        with ((viewModel.uiMode.value as Update).todoForm) {
            assertEquals(dummyTodo, currentTodo)
            assertEquals(title.value, dummyTodo.title)
            assertEquals(description.value, dummyTodo.description)
            assertEquals(timestamp.value, dummyTodo.completionTime)
        }
    }

    @Test
    fun `onDeleteTodo, it publishes new todo state where the supplied TODO is not present`() = runTest {
        dummyFlow.value = Todos(todos = listOf(dummyTodo, anotherDummyTodo))

        viewModel.onDeleteTodo(dummyTodo)

        assertEquals(listOf(anotherDummyTodo), viewModel.todos.value)
    }

    @Test
    fun `onTodoDone, if action is neither create nor update, then it sets uiMode to ViewAll and leaves todos unchanged`() = runTest {
        dummyFlow.value = Todos(todos = listOf(dummyTodo))
        viewModel.onViewTodo(dummyTodo)

        viewModel.onTodoDone()

        assertEquals(ViewAll, viewModel.uiMode.value)
        assertEquals(listOf(dummyTodo), viewModel.todos.value)
    }

    @Test
    fun `onTodoDone, if todo does not exist, then it publishes new todo state appended with the created todo`() = runTest {
        every { mockIdSupplier.id() }.returns("some-created-id")
        val newTodoForm = uninitialisedForm.apply {
            updateTitle("some-title")
            updateDescription("some-description")
            updateCompletedByDate(12345L)
        }

        viewModel.onTodoDone(newTodoForm)

        with (viewModel.todos.value.first()) {
            assertEquals("some-created-id", id)
            assertEquals("some-title", title)
            assertEquals("some-description", description)
            assertEquals(12345, completionTime)
        }
    }

    @Test
    fun `onTodoDone, if todo exists, then it publishes todo state replacing the selected todo`() = runTest {
        dummyFlow.value = Todos(todos = listOf(dummyTodo))
        val existingTodoForm = TodoForm(dummyTodo, mockTimeSupplier).apply {
            updateDescription("some-updated-description")
        }

        viewModel.onTodoDone(existingTodoForm)

        assertEquals(listOf(dummyTodo.copy(description = "some-updated-description")), viewModel.todos.value)
    }

    @Test
    fun `onTodoDone, it logs the todo`() = runTest {
        val existingTodoForm = TodoForm(dummyTodo, mockTimeSupplier)

        viewModel.onTodoDone(existingTodoForm)

        coVerify { mockRepository.logTodoStats(dummyTodo, any()) }
    }

    companion object {
        const val TIME_NOW = 123L
    }
}