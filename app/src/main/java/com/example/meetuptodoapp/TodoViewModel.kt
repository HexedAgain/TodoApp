package com.example.meetuptodoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
import com.example.meetuptodoapp.domain.model.TodoStore
import com.example.meetuptodoapp.ui.model.UITodo
import com.example.meetuptodoapp.utils.formatDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class IdSupplier {
    fun id(): String = UUID.randomUUID().toString()
}

class TodoEditor(
    val currentTodo: TodoItem?,
    val isAdd: Boolean, // probably don't need
) {
    private val _id = MutableStateFlow(currentTodo?.id)
    val id = _id.asStateFlow()
    private val _title = MutableStateFlow(currentTodo?.title ?: "")
    val title = _title.asStateFlow()
    private val _description = MutableStateFlow(currentTodo?.description ?: "")
    val description = _description.asStateFlow()
    private val _timestamp = MutableStateFlow(currentTodo?.completionTime ?: NO_TIMESTAMP)
    val timestamp = _timestamp.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker = _showDatePicker.asStateFlow()
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker = _showTimePicker.asStateFlow()
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }
    fun updateCompletedByDate(newTimeStamp: Long?) {
        _timestamp.value = newTimeStamp ?: NO_TIMESTAMP
        _showDatePicker.value = false
    }
    fun showDatePicker() {
        _showDatePicker.value = true
    }
    fun showTimePicker() {
        _showTimePicker.value = true
    }

    fun updateHoursMins(newHours: Long, newMins: Long) {
        val currTime = Instant.ofEpochMilli(_timestamp.value).atZone(ZoneId.of("GMT"))
        val (oldHours, oldMins) = Pair(currTime.hour, currTime.minute)
        val newTimeStamp = currTime
            .plusHours(newHours - oldHours)
            .plusMinutes(newMins - oldMins)
            .toInstant()
            .toEpochMilli()
        _timestamp.value = newTimeStamp
        _showTimePicker.value = false
    }

    fun formatDate(timestamp: Long, isVerbose: Boolean = true): String {
        val format = if (isVerbose) "EE, dd MMMM yyyy HH:mm" else "EE, dd/MM/yyyy"
        return Instant
            .ofEpochMilli(timestamp)
            .atZone(ZoneId.of("GMT"))
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern(format))
    }

    fun isValid(): Boolean {
        return title.value.isNotEmpty() && description.value.isNotEmpty() && timestamp.value > NO_TIMESTAMP
    }

    fun renderTodo() {
        // this should now communicate to viewmodel that we finished
    }

    companion object {
        const val NO_TIMESTAMP = -1L
    }
}

class TodoViewModel(
    private val todoStore: TodoStore,
    private val repository: TodoRepository = TodoRepository(),
    private val idSupplier: IdSupplier = IdSupplier()
): ViewModel() {

    sealed interface TodoAction {
        class ViewTodo(val todo: TodoItem): TodoAction
        class UpdateTodo(val todoEditor: TodoEditor): TodoAction
        class CreateTodo(val todoEditor: TodoEditor): TodoAction
        data object None: TodoAction
    }

    init {
        collectTodos()
    }

    private val _todos: MutableStateFlow<List<TodoItem>> = MutableStateFlow(listOf())
    val todos: StateFlow<List<UITodo>> = _todos
        .map { todos -> todos.map { toUI(it) } }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    private val _showModal = MutableStateFlow(false)
    val showModal = _showModal.asStateFlow()

//    private val _activeTodo = MutableStateFlow(TodoItem.default())
//    val activeTodo = _activeTodo.asStateFlow()

    private var editIdx: Int? = null

//    fun createTodoItem(): TodoItem {
//        return TodoItem.default()
//    }

    // FIXME - probably want this to be the thing that drives modal visibility
    //         if NONE we don't show modal, otherwise we show it
    private var currentTodoAction: TodoAction = TodoAction.None

    fun onTodoViewOrCreate(idx: Int?) {
        editIdx = idx
        if (idx == null) {
            currentTodoAction = TodoAction.CreateTodo(TodoEditor(currentTodo = null, isAdd = true))
        } else {
            val todo = _todos.value[idx]
            currentTodoAction = TodoAction.ViewTodo(todo)
        }
        _showModal.value = true
    }

    fun createTodo() {
        currentTodoAction = TodoAction.CreateTodo(TodoEditor(currentTodo = null, isAdd = true))
        _showModal.value = true
    }

    fun closeModal(todoEditor: TodoEditor? = null) {
        _showModal.value = false
        editIdx = null
        todoEditor?.run {
            val todo = TodoItem(
                id = id.value ?: idSupplier.id(),
                title = title.value ,
                description = description.value,
                completionTime = timestamp.value
            )
            val idx = _todos.value.indexOf(todo)
            if (idx >= 0) {
                val allTodos = _todos.value.toMutableList()
                allTodos[idx] = todo
                viewModelScope.launch {
                    todoStore.updateData { it.copy(todos = allTodos) }
                }
            } else {
                viewModelScope.launch {
                    todoStore.updateData { it.copy(todos = it.todos + todo) }
                }
            }
        }
    }

    fun requestModal() {
        _showModal.value = true
    }

    fun currentTodoItem(): TodoItem? {
        return null
    }

    fun currentTodoAction(): TodoAction {
        return currentTodoAction
    }

    fun addTodo() {

    }

    fun updateTodo(todo: TodoItem) {
        currentTodoAction = TodoAction.UpdateTodo(todoEditor = TodoEditor(currentTodo = todo, isAdd = false))
    }

//    fun getEditor(todo: TodoItem): TodoEditor {
//        return TodoEditor(todo)
//    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoStore.updateData { todos ->
                todos.copy(
                    todos = todos.todos.filter { it.id != todo.id }
                )
            }
        }
        _showModal.value = false
    }

    private fun collectTodos() {
        viewModelScope.launch {
            todoStore.data.collect {
                _todos.value = it.todos
            }
        }
    }

    private fun toUI(todoItem: TodoItem): UITodo {
        return UITodo(
            id = todoItem.id,
            title = todoItem.title.takeIf { it.isNotEmpty() } ?: "No title",
            description = todoItem.description.takeIf { it.isNotEmpty() } ?: "No description",
            date = formatDate(todoItem.timestamp),
            completedTimestamp = todoItem.completedTime
        )
    }
}