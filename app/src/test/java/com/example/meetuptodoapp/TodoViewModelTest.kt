package com.example.meetuptodoapp

import com.example.meetuptodoapp.TodoViewModel.TodoAction.CreateTodo
import com.example.meetuptodoapp.TodoViewModel.TodoAction.None
import com.example.meetuptodoapp.TodoViewModel.TodoAction.UpdateTodo
import com.example.meetuptodoapp.TodoViewModel.TodoAction.ViewTodo
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.Todos
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class TodoViewModelTest {

    private lateinit var viewModel: TodoViewModel
    private var dummyFlow: MutableStateFlow<Todos> = MutableStateFlow(Todos.default())
    private val mockTodoStorage: TodoStorage = mockk()
    private val mockRepository: TodoRepository = mockk()
    private val mockTimeSupplier: TimeSupplier = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dummyTodo = TodoItem(
        id = "some-id",
        title = "some-title",
        description = "some-description",
    )
    private val anotherDummyTodo = dummyTodo.copy(id = "some-other-todo")
    private val completedTodo = dummyTodo.copy(completedTime = TIME_NOW - 1, id = "some-completed-id")

    @OptIn(ExperimentalCoroutinesApi::class)
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

        reInitialiseViewModel()
    }

    private fun reInitialiseViewModel() = runTest {
        viewModel = TodoViewModel(
            todoStorage = mockTodoStorage,
            repository = mockRepository,
            timeSupplier = mockTimeSupplier
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
    fun `onViewTodo, it publishes a ViewTodo action equipped with supplied TODO`() = runTest {
        viewModel.onViewTodo(dummyTodo)

        with ((viewModel.todoAction.value as ViewTodo)) {
            assertEquals(dummyTodo, this.todo)
        }
    }

    @Test
    fun `onCreateTodo, it publishes a CreateTodo action equipped with an uninitialised form`() = runTest {
        viewModel.onCreateTodo()

        with ((viewModel.todoAction.value as CreateTodo).todoForm) {
            assertEquals(null, currentTodo)
            assertEquals(title.value, "")
            assertEquals(description.value, "")
            assertEquals(timestamp.value, -1L)
        }
    }

    @Test
    fun `onUpdateTodo, it publishes an UpdateTodo action equipped with form consistent with supplied TODO`() = runTest {
       viewModel.onUpdateTodo(dummyTodo)

        with ((viewModel.todoAction.value as UpdateTodo).todoForm) {
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
    fun `onTodoDone, if action is neither create nor update, then it publishes default action and leaves todos unchanged`() = runTest {
        dummyFlow.value = Todos(todos = listOf(dummyTodo))
        viewModel.onViewTodo(dummyTodo)

        viewModel.onTodoDone()

        assertEquals(None, viewModel.todoAction.value)
        assertEquals(listOf(dummyTodo), viewModel.todos.value)
    }

    @Test
    fun `onTodoDone, if action is create, then it publishes new todo state appended with the created todo`() = runTest {
        val todoForm = TodoForm()
        viewModel.onTodoDone(TodoForm(dummyTodo, mockTimeSupplier))

        assertEquals(listOf(dummyTodo), viewModel.todos.value)
    }

    @Test
    fun `onTodoDone, if action is edit, then it publishes todo state replacing the selected todo`() = runTest {

    }

    @Test
    fun `onTodoDone, if action is edit, then it logs the todo`() = runTest {

    }

    @Test
    fun `onTodoDone, if action is create, then it logs the todo`() = runTest {

    }

    companion object {
        const val TIME_NOW = 123L
    }
}