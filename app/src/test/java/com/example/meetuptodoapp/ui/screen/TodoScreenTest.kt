package com.example.meetuptodoapp.ui.screen

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.ui.tags.TodoTags
import com.example.meetuptodoapp.ui.viewmodel.TodoViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.mock.MockProviderRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class TodoScreenTest {
    @get:Rule val rule = createComposeRule()

    private lateinit var spiedViewModel: TodoViewModel

    @get:Rule
    val mockProvider = MockProviderRule.create { mockkClass(it) }
//    val spiedViewModel = spyk(actualViewModel)
    private val dummyTodo = TodoItem(
        id ="some-id",
        title = "some-title",
        description = "some-description"
    )
    private val anotherDummyTodo = dummyTodo.copy(title = "some-other-title")

    @Before
    fun setup() {
        spiedViewModel = spyk(
            TodoViewModel(
                todoStorage = mockk(),
                ioDispatcher = UnconfinedTestDispatcher(),
                todoPrefs = mockk()
            )
        )
    }

    @After
    fun teardown() {
        stopKoin()
    }

    // Here we show that all interactions are correctly hooked up to the viewmodel
    @Test
    fun `when FAB is clicked, UI mode transitions to Create`() {
        rule.setContent { TodoScreen(spiedViewModel) }

        rule.onNodeWithTag(TodoTags.FAB).performClick()

        assertTrue(spiedViewModel.uiMode.value is TodoViewModel.UIMode.Create)
    }


    @Test
    fun `when add new todo form button is clicked UI asks viewmodel to create the new TODO`() {
        assertTrue(false)
    }

    @Test
    fun `when screen first loaded the modal is closed`() {
        rule.setContent {
            TodoScreen(spiedViewModel)
        }

        rule.onNodeWithTag("MODAL_BOTTOM_SHEET").assertIsNotDisplayed()
    }

    @Test
    fun `when screen loaded and no todos exist, the todo list will be rendered empty`() {
        every { spiedViewModel.todos }.returns(MutableStateFlow(listOf()))
        rule.setContent {
            TodoScreen(spiedViewModel)
        }

        rule.onNodeWithTag("TODO_LIST")
            .onChildAt(0)
            .assertIsNotDisplayed()
    }

    @Test
    fun `when screen loaded and todos exist, the todos will be rendered on screen`() {
        every { spiedViewModel.todos }.returns(MutableStateFlow(listOf(dummyTodo)))
        rule.setContent {
            TodoScreen(spiedViewModel)
        }

        rule.onNodeWithTag("TODO_LIST")
            .onChildAt(0)
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when a todo is clicked, the modal opens with immutable fields, correctly populated for the todo`() = runTest {
        val epochTodo = dummyTodo.copy(completionTime = 0L)
        every { spiedViewModel.todos }.returns(MutableStateFlow(listOf(epochTodo)))
        rule.setContent {
            TodoScreen(spiedViewModel)
        }

        rule.onNodeWithTag("TODO_LIST")
            .onChildAt(0)
            .performClick()

        verify { spiedViewModel.onViewTodo(epochTodo) }
        with (rule.onNodeWithTag("MODAL_BOTTOM_SHEET")) {
            onChildAt(1).assertTextContains("Title")
            onChildAt(2).assertTextContains("some-title").assertIsNotEditable()
            onChildAt(3).assertTextContains("Description")
            onChildAt(4).assertTextContains("some-description").assertIsNotEditable()
            onChildAt(5).assertTextContains("To Complete By")
            onChildAt(6).assertTextContains("Thu, 01 January 1970 00:00").assertIsNotEditable()
        }
    }

    @Test
    fun `when screen first loaded and FAB is pressed, the modal is opened, and TODO action is CreateTodo`() {
        rule.setContent {
            TodoScreen(spiedViewModel)
        }

        rule.onNodeWithTag("FAB").performClick()

        rule.onNodeWithTag("MODAL_BOTTOM_SHEET").assertIsDisplayed()
        verify { spiedViewModel.onCreateTodo() }
    }
}

fun SemanticsNodeInteraction.assertIsEditable(): Boolean {
    return this.fetchSemanticsNode().config.toList().map { it.key.name }.contains("IsEditable")
}
fun SemanticsNodeInteraction.assertIsNotEditable(): SemanticsNodeInteraction {
    val hasIsEditable = this.fetchSemanticsNode().config.toList().map { it.key.name }.contains("IsEditable")
    if (hasIsEditable) {
        throw AssertionError("Assert failed: The component is editable!")
    }
    return this
}
