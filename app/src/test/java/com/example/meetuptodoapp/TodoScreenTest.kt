package com.example.meetuptodoapp

import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import com.example.meetuptodoapp.domain.model.TodoItem
import io.mockk.MockK
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TodoScreenTest: KoinTest {
    @get:Rule val rule = createComposeRule()

//        val mockTimeSupplier = declareMock<TimeSupplier>()
//        every { mockTimeSupplier.now() }.returns(0L)

    @get:Rule
    val mockProvider = MockProviderRule.create { mockkClass(it) }
    val actualViewModel: TodoViewModel by inject(TodoViewModel::class.java)
    val spiedViewModel = spyk(actualViewModel)
    private val dummyTodo = TodoItem(
        id ="some-id",
        title = "some-title",
        description = "some-description"
    )
    private val anotherDummyTodo = dummyTodo.copy(title = "some-other-title")

    @After
    fun teardown() {
        stopKoin()
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
