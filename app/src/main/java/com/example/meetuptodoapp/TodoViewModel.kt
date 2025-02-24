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

class TodoEditor(
    val currentTodo: TodoItem?,
    val timeSupplier: TimeSupplier
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

    fun initialTimestamp(): Long {
        return _timestamp.value.takeIf { it > -1L } ?: timeSupplier.now()
    }

    fun isValid(): Boolean {
        return title.value.isNotEmpty() && description.value.isNotEmpty() && timestamp.value > NO_TIMESTAMP
    }

    fun buttonText(): String {
        return "${ if (id.value == null) "Add" else "Update" } TODO"
    }

    companion object {
        const val NO_TIMESTAMP = -1L
    }
}

class TodoViewModel(
    // FIXME - this one is going to be problematic to test, I will want to change a value
    // and I will then want to see that the todos got updated (which happens in collect)

    // The mock for this will need to define data as a flow, and update will cause it to emit a new
    // value
    private val todoStore: TodoStore,
    private val repository: TodoRepository,
    private val idSupplier: IdSupplier = IdSupplier(),
    val timeSupplier: TimeSupplier = TimeSupplier()
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

    private val _todoAction: MutableStateFlow<TodoAction> = MutableStateFlow(TodoAction.None)
    val todoAction = _todoAction.asStateFlow()

    fun onViewTodo(idx: Int) {
        val todo = _todos.value[idx]
        _todoAction.value = TodoAction.ViewTodo(todo)
    }

    fun onCreateTodo() {
        _todoAction.value = TodoAction.CreateTodo(
            todoEditor = TodoEditor(currentTodo = null, timeSupplier = timeSupplier)
        )
    }

    fun onTodoDone(todoEditor: TodoEditor? = null) {
        todoEditor?.let {
            commitTodo(it)
        } ?: run {
            _todoAction.value = TodoAction.None
        }
    }


    fun onUpdateTodo(todo: TodoItem) {
        _todoAction.value = TodoAction.UpdateTodo(
            todoEditor = TodoEditor(currentTodo = todo, timeSupplier = timeSupplier)
        )
    }

    fun onDeleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            val remainingTodos = _todos.value.filter { it.id != todo.id }
            todoStore.updateData { todos ->
                todos.copy(todos = remainingTodos)
            }
        }
        _todoAction.value = TodoAction.None
    }

    private fun commitTodo(todoEditor: TodoEditor) {
        val todo = TodoItem(
            id = todoEditor.id.value ?: idSupplier.id(),
            title = todoEditor.title.value ,
            description = todoEditor.description.value,
            completionTime = todoEditor.timestamp.value
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
            todoStore.updateData { it.copy(todos = newTodos) }
        }
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