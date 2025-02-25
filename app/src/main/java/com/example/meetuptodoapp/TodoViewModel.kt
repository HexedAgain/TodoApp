package com.example.meetuptodoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetuptodoapp.api.TodoRepository
import com.example.meetuptodoapp.domain.model.TodoItem
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
import java.util.UUID

class IdSupplier {
    fun id(): String = UUID.randomUUID().toString()
}

class TimeSupplier {
    fun now(): Long = Instant.now().toEpochMilli()
}

class TodoForm(
    val currentTodo: TodoItem?,
    val timeSupplier: TimeSupplier,
    currentId: String? = currentTodo?.id,
) {
    val id = currentId
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

    fun initialTimestamp(): Long {
        return _timestamp.value.takeIf { it > -1L } ?: timeSupplier.now()
    }

    fun isValid(): Boolean {
        return title.value.isNotEmpty() && description.value.isNotEmpty() && timestamp.value > NO_TIMESTAMP
    }

    fun buttonText(): String {
        return "${ if (id == null) "Add" else "Update" } TODO"
    }

    companion object {
        const val NO_TIMESTAMP = -1L
    }
}

class TodoViewModel(
    private val todoStorage: TodoStorage,
    private val repository: TodoRepository,
    private val idSupplier: IdSupplier = IdSupplier(),
    val timeSupplier: TimeSupplier = TimeSupplier(),
): ViewModel() {

    sealed interface TodoAction {
        class ViewTodo(val todo: TodoItem): TodoAction
        class UpdateTodo(val todoForm: TodoForm): TodoAction
        class CreateTodo(val todoForm: TodoForm): TodoAction
        data object None: TodoAction
    }

    private val _todos: MutableStateFlow<List<TodoItem>> = MutableStateFlow(listOf())
    // Is there any way to defer this mapping till last minute?
    // Do I want the viewmodel to be doing this?
    // possibly want toUI to return null if crap data, and then filter them out
//    val todos: StateFlow<List<UITodo>> = _todos
//        .map { todos -> todos.map { toUI(it) } }
//        .stateIn(viewModelScope, sharingStarted, listOf())
    val todos: StateFlow<List<TodoItem>> = _todos
        .map { todos -> onlyVisibleTodos(todos) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    init {
        collectTodos()
    }


    private val _todoAction: MutableStateFlow<TodoAction> = MutableStateFlow(TodoAction.None)
    val todoAction = _todoAction.asStateFlow()
    private var showCompleted = false

    fun onViewTodo(todo: TodoItem) {
        _todoAction.value = TodoAction.ViewTodo(todo)
    }

    fun onCreateTodo() {
        _todoAction.value = TodoAction.CreateTodo(
            todoForm = TodoForm(currentTodo = null, timeSupplier = timeSupplier)
        )
    }

    fun onDeleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            val remainingTodos = _todos.value.filter { it.id != todo.id }
            todoStorage.write(remainingTodos)
        }
        _todoAction.value = TodoAction.None
    }

    fun onUpdateTodo(todo: TodoItem) {
        _todoAction.value = TodoAction.UpdateTodo(
            todoForm = TodoForm(currentTodo = todo, timeSupplier = timeSupplier)
        )
    }

    fun onTodoDone(todoForm: TodoForm? = null) {
        todoForm?.let {
            commitTodo(it)
        } ?: run {
            _todoAction.value = TodoAction.None
        }
    }

    fun isCompleted(todo: TodoItem): Boolean {
        return timeSupplier.now() > todo.completedTime
    }

    fun toggleShowCompleted() {
        showCompleted = !showCompleted
        val newTodos = _todos.value
        // Bit dodgy but need to refresh _todos
        _todos.update { listOf() }
        _todos.update { newTodos }
    }

    fun shouldShowTodo(todo: TodoItem): Boolean {
        return showCompleted || !isCompleted(todo)
    }

    private fun onlyVisibleTodos(input: List<TodoItem>): List<TodoItem> {
        return input.filter { shouldShowTodo(it) }
    }

    private fun commitTodo(todoForm: TodoForm) {
        val todo = TodoItem(
            id = todoForm.id ?: idSupplier.id(),
            title = todoForm.title.value ,
            description = todoForm.description.value,
            completionTime = todoForm.timestamp.value
        )
        val idx = _todos.value.indexOfFirst { it.id == todo.id }
        val newTodos: List<TodoItem>
        if (idx >= 0) {
            newTodos = _todos.value.toMutableList()
            newTodos[idx] = todo
        } else {
            newTodos = _todos.value + todo
        }
        viewModelScope.launch {
            repository.logTodoStats(todo) {
                _todoAction.value = TodoAction.None
            }
            todoStorage.write(newTodos)
        }
    }

    private fun collectTodos() {
        viewModelScope.launch {
            todoStorage.readAsFlow().collect {
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