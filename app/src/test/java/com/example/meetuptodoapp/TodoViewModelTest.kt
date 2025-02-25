package com.example.meetuptodoapp

import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.ui.model.UITodo
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
    private val mockTodoStore: TodoStore = mockk()
    private val mockRepository: TodoRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dummyTodoItem = TodoItem(
        id = "some-id",
        title = "some-title",
        description = "some-description",
    )
    private val dummyUITodo = UITodo(
        id = "some-id",
        title = "some-title",
        description = "some-description",
        date = "Thu, 01 January 1970 00:00",
        completedTimestamp = Long.MAX_VALUE
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockTodoStore.data }.returns(dummyFlow)
        coEvery { mockTodoStore.updateData { any() } }.answers {  answer ->
            val todoItem = answer.invocation.args.first() as Todos
            dummyFlow.value = todoItem
            todoItem
        }

        reInitialiseViewModel()
    }

    private fun reInitialiseViewModel() = runTest {
        viewModel = TodoViewModel(
            todoStore = mockTodoStore,
            repository = mockRepository,
            sharingStarted = SharingStarted.Eagerly
        )
    }

    @Test
    fun `initialise, it publishes all TODOs that are uncompleted, rendered for UI`() = runTest {
        // FIXME - need to get the completed logic in place
        val expectedTodos = listOf(dummyUITodo, dummyUITodo.copy(id = "some-other-id"))

        dummyFlow.value = Todos(todos = listOf(dummyTodoItem, dummyTodoItem.copy(id = "some-other-id")))

        assertEquals(expectedTodos.first(), viewModel.todos.value.first())
        assertEquals(expectedTodos.last(), viewModel.todos.value.last())
    }

    @Test
    fun `toggleCompleted, if toggled true, it publishes all TODOs`() {

    }

    @Test
    fun `toggleCompleted, if toggled false, it publishes all TODOs that are uncompleted`() {

    }

    @Test
    fun `onViewTodo, it publishes a ViewTodo action equipped with supplied TODO`() = runTest {

    }

    @Test
    fun `onCreateTodo, it publishes a CreateTodo action equipped with an uninitialised form`() = runTest {

    }

    @Test
    fun `onUpdateTodo, it publishes an UpdateTodo action equipped with form consistent with supplied TODO`() = runTest {

    }

    @Test
    fun `onDeleteTodo, it publishes new todo state where the supplied TODO is not present`() = runTest {

    }

    @Test
    fun `onTodoDone, if action is neither create nor update, then it publishes default action and leaves todos unchanged`() = runTest {

    }

    @Test
    fun `onTodoDone, if action is create, then it publishes new todo state appended with the created todo`() = runTest {

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
}