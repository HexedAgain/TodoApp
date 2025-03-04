package com.example.meetuptodoapp.ui.screen

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.meetuptodoapp.ui.tags.TodoTags
import com.example.meetuptodoapp.ui.viewmodel.TimeSupplier
import com.example.meetuptodoapp.ui.viewmodel.TodoForm
import com.example.meetuptodoapp.ui.widgets.MockDateSelector
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    val mockTimeSupplier: TimeSupplier = mockk()

    val createTodoForm = TodoForm(currentTodo = null, timeSupplier = mockTimeSupplier)
    val mockDateSelector = MockDateSelector()

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
        rule.setContent { ModalTodoForm(todoForm = createTodoForm, onDone = {}) }

        createTodoForm.updateTitle("some-title")

        rule.onNodeWithTag(TodoTags.TODO_FORM_TITLE).assertTextContains("some-title")
    }

    @Test
    fun `when description text is updated, new text is rendered in the description field`() {
        rule.setContent { ModalTodoForm(todoForm = createTodoForm, onDone = {}) }

        createTodoForm.updateDescription("some-description")

        rule.onNodeWithTag(TodoTags.TODO_FORM_DESCRIPTION).assertTextContains("some-description")
    }

    @Test
    fun `when date field is clicked, the date picker dialog is rendered with the correct timestamp`() {
        every { mockTimeSupplier.now() } returns(12345L)
        rule.setContent { ModalTodoForm(todoForm = createTodoForm, onDone = {}, dateSelector = mockDateSelector) }

        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).performClick()

        assertEquals(12345L, mockDateSelector.initialTimestampSupplied)
    }

    @Test
    fun `when a date is selected the date field is populated with a formatted date`() {
        rule.setContent { ModalTodoForm(todoForm = createTodoForm, onDone = {}, dateSelector = mockDateSelector) }
        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).performClick()

        mockDateSelector.onCloseFn?.invoke(EPOCH_TIMESTAMP + ONE_DAY)

        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).assertTextContains("Fri, 02/01/1970")
    }

    @Test
    fun `when not all of title, description, and date are populated then the add TODO button is disabled`() {
        rule.setContent { ModalTodoForm(todoForm = createTodoForm, onDone = {}, dateSelector = mockDateSelector) }
        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).assertIsDeactivated()

        createTodoForm.updateTitle("some-title")
        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).assertIsDeactivated()

        createTodoForm.updateTitle("some-description")
        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).assertIsDeactivated()
    }

    @Test
    fun `when title, description, and date are populated then the add TODO button is enabled and will invoke supplied onDone`() {
        var didInvokeOnDone = false
        rule.setContent {
            ModalTodoForm(
                todoForm = createTodoForm,
                onDone = { didInvokeOnDone = true },
                dateSelector = mockDateSelector
            )
        }
        createTodoForm.updateTitle("some-title")
        createTodoForm.updateTitle("some-description")
        rule.onNodeWithTag(TodoTags.TODO_FORM_DATE).performClick()
        mockDateSelector.onCloseFn?.invoke(EPOCH_TIMESTAMP + ONE_DAY)

        rule.onNodeWithTag(TodoTags.ADD_EDIT_TODO_BUTTON).performClick()

        assertEquals(true, didInvokeOnDone)
    }

    companion object {
        const val EPOCH_TIMESTAMP = 0L
        const val ONE_DAY = (24 * 60 * 60 * 1000).toLong()
    }
}