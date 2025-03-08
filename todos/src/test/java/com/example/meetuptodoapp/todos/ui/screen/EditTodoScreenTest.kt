package com.example.meetuptodoapp.todos.ui.screen

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.meetuptodoapp.todos.ui.tags.TodoTags
import com.example.meetuptodoapp.todos.ui.viewmodel.TimeSupplier
import com.example.meetuptodoapp.todos.ui.viewmodel.TodoForm
import com.example.meetuptodoapp.todos.ui.viewmodel.TodoFormState
import com.example.meetuptodoapp.todos.ui.widgets.MockDateSelector
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EditTodoScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val mockTimeSupplier: TimeSupplier = mockk()
    private val populatedTodoState = TodoFormState(
        title = MutableStateFlow("some-title"),
        description = MutableStateFlow("some-description"),
        timestamp = MutableStateFlow(EPOCH_TIMESTAMP),
    )

    private val createTodoForm = TodoForm(currentTodo = null, timeSupplier = mockTimeSupplier)
    private val populatedTodoForm = TodoForm(currentTodo = null, timeSupplier = mockTimeSupplier, todoFormState = populatedTodoState)
    private val mockDateSelector = MockDateSelector()

    @Before
    fun setup() {
        every { mockTimeSupplier.now() } returns EPOCH_TIMESTAMP
    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `when title text is updated, new text is rendered in the title field`() {
        renderTodoForm(todoForm = createTodoForm)

        createTodoForm.updateTitle("some-title")

        rule.onNodeWithTag(TodoTags.TODO_FORM_TITLE).assertTextContains("some-title")
    }

    @Test
    fun `when description text is updated, new text is rendered in the description field`() {
        renderTodoForm(todoForm = createTodoForm)

        createTodoForm.updateDescription("some-description")

        rule.onNodeWithTag(TodoTags.TODO_FORM_DESCRIPTION).assertTextContains("some-description")
    }

    @Test
    fun `when date field is clicked, the date picker dialog is rendered with the correct timestamp`() {
        every { mockTimeSupplier.now() } returns(12345L)
        renderTodoForm(todoForm = createTodoForm)

        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).performClick()

        assertEquals(12345L, mockDateSelector.initialTimestampSupplied)
    }

    @Test
    fun `when a date is selected the date field is populated with a formatted date`() {
        renderTodoForm(todoForm = createTodoForm)
        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).performClick()

        mockDateSelector.onCloseFn?.invoke(EPOCH_TIMESTAMP + ONE_DAY)

        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).assertTextContains("Fri, 02/01/1970")
    }

    @Test
    fun `when not all of title, description, and date are populated then the add TODO button is disabled`() {
        var didInvokeOnDone = false
        renderTodoForm(todoForm = createTodoForm, onDone = { didInvokeOnDone = true })
        createTodoForm.updateTitle("some-title")
        createTodoForm.updateTitle("some-description")

        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).performClick()

        assertEquals(false, didInvokeOnDone)
    }

    @Test
    fun `when title, description, and date are populated then the add TODO button is enabled and will invoke supplied onDone`() {
        var didInvokeOnDone = false
        renderTodoForm(todoForm = populatedTodoForm, onDone = { didInvokeOnDone = true })

        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).performClick()

        assertEquals(true, didInvokeOnDone)
    }

    private fun renderTodoForm(
        todoForm: TodoForm,
        onDone: (TodoForm) -> Unit = {},
    ) {
        rule.setContent {
            ModalTodoForm(
                todoForm = todoForm,
                onDone = onDone,
                dateSelector = mockDateSelector
            )
        }
    }

    companion object {
        const val EPOCH_TIMESTAMP = 0L
        const val ONE_DAY = (24 * 60 * 60 * 1000).toLong()
    }
}