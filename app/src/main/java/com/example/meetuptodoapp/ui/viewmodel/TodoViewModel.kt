package com.example.meetuptodoapp.ui.viewmodel

//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.meetuptodoapp.domain.model.TodoItem
//import com.example.meetuptodoapp.storage.TodoSharedPrefs
//import com.example.meetuptodoapp.storage.TodoStorage
//import com.example.meetuptodoapp.ui.model.UITodo
//import com.example.meetuptodoapp.utils.formatDate
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.time.Instant
//import java.time.ZoneId
//import java.util.UUID
//
//class IdSupplier {
//    fun id(): String = UUID.randomUUID().toString()
//}
//
//class TimeSupplier {
//    fun now(): Long = Instant.now().toEpochMilli()
//}
//
//data class TodoFormState(
//    private val _title: MutableStateFlow<String>,
//    private val _description: MutableStateFlow<String>,
//    private val _timestamp: MutableStateFlow<Long>
//)
//
//class TodoForm(
//    val currentTodo: TodoItem?,
//    val timeSupplier: TimeSupplier,
//    currentId: String? = currentTodo?.id,
//) {
//    val id = currentId
//    private val _title = MutableStateFlow(currentTodo?.title ?: "")
//    val title = _title.asStateFlow()
//    private val _description = MutableStateFlow(currentTodo?.description ?: "")
//    val description = _description.asStateFlow()
//    private val _timestamp = MutableStateFlow(currentTodo?.completionTime ?: NO_TIMESTAMP)
//    val timestamp = _timestamp.asStateFlow()
//
//    private val _showDatePicker = MutableStateFlow(false)
//    val showDatePicker = _showDatePicker.asStateFlow()
//    private val _showTimePicker = MutableStateFlow(false)
//    val showTimePicker = _showTimePicker.asStateFlow()
//    fun updateTitle(newTitle: String) {
//        _title.value = newTitle
//    }
//    fun updateDescription(newDescription: String) {
//        _description.value = newDescription
//    }
//    fun updateCompletedByDate(newTimeStamp: Long?) {
//        _timestamp.value = newTimeStamp ?: NO_TIMESTAMP
//        _showDatePicker.value = false
//    }
//    fun showDatePicker() {
//        _showDatePicker.value = true
//    }
//    fun showTimePicker() {
//        _showTimePicker.value = true
//    }
//
//    fun updateHoursMins(newHours: Long, newMins: Long) {
//        val currTime = Instant.ofEpochMilli(_timestamp.value).atZone(ZoneId.of("GMT"))
//        val (oldHours, oldMins) = Pair(currTime.hour, currTime.minute)
//        val newTimeStamp = currTime
//            .plusHours(newHours - oldHours)
//            .plusMinutes(newMins - oldMins)
//            .toInstant()
//            .toEpochMilli()
//        _timestamp.value = newTimeStamp
//        _showTimePicker.value = false
//    }
//
//    fun initialTimestamp(): Long {
//        return _timestamp.value.takeIf { it > -1L } ?: timeSupplier.now()
//    }
//
//    fun isValid(): Boolean {
//        return title.value.isNotEmpty() && description.value.isNotEmpty() && timestamp.value > NO_TIMESTAMP
//    }
//
//    fun buttonText(): String {
//        return "${ if (id == null) "Add" else "Update" } TODO"
//    }
//
//    companion object {
//        const val NO_TIMESTAMP = -1L
//    }
//}
//
//class TodoViewModel(
//    private val todoStorage: TodoStorage,
//    private val ioDispatcher: CoroutineDispatcher,
//    private val todoPrefs: TodoSharedPrefs,
//    private val idSupplier: IdSupplier = IdSupplier(),
//    private val timeSupplier: TimeSupplier = TimeSupplier(),
//): ViewModel() {
//
//    sealed interface UIMode {
//        class ViewSingle(val todo: TodoItem): UIMode
//        class Update(val todoForm: TodoForm): UIMode
//        class Create(val todoForm: TodoForm): UIMode
//        data object ViewAll: UIMode
//    }
//
//    private val _todos: MutableStateFlow<List<TodoItem>> = MutableStateFlow(listOf())
//    val todos: StateFlow<List<TodoItem>> = _todos
//        .map { todos -> onlyVisibleTodos(todos) }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())
////
////    init {
////        collectTodos()
////    }
//
//
//    private val _uiMode: MutableStateFlow<UIMode> = MutableStateFlow(UIMode.ViewAll)
//    val uiMode = _uiMode.asStateFlow()
//
//    private val _showCompleted = MutableStateFlow(false)
//    val showCompleted = _showCompleted.asStateFlow()
//
//    fun onViewTodo(todo: TodoItem) {
//        _uiMode.value = UIMode.ViewSingle(todo)
//    }
//
//    fun onCreateTodo() {
//        _uiMode.value = UIMode.Create(
//            todoForm = TodoForm(currentTodo = null, timeSupplier = timeSupplier)
//        )
//    }
//
//    fun onDeleteTodo(todo: TodoItem) {
//        viewModelScope.launch {
//            val remainingTodos = _todos.value.filter { it.id != todo.id }
//            todoStorage.write(remainingTodos)
//        }
//        _uiMode.value = UIMode.ViewAll
//    }
//
//    fun onUpdateTodo(todo: TodoItem) {
//        _uiMode.value = UIMode.Update(
//            todoForm = TodoForm(currentTodo = todo, timeSupplier = timeSupplier)
//        )
//    }
//
//    fun onToggleComplete(todoItem: TodoItem) {
//        commitTodo(
//            todoItem.copy(
//                completedTime = if (isCompleted(todoItem)) Long.MAX_VALUE else timeSupplier.now()
//            )
//        )
//    }
//
//    fun onTodoDone(todoForm: TodoForm? = null) {
//        todoForm?.let {
//            val todo = TodoItem(
//                id = todoForm.id ?: idSupplier.id(),
//                title = todoForm.title.value ,
//                description = todoForm.description.value,
//                completionTime = todoForm.timestamp.value
//            )
//            commitTodo(todo)
//        }
//        _uiMode.value = UIMode.ViewAll
//    }
//
//    fun isCompleted(todo: TodoItem): Boolean {
//        return timeSupplier.now() > todo.completedTime
//    }
//
//    fun toggleShowCompleted() {
//        _showCompleted.update { !it }
//        val newTodos = _todos.value
//        // Bit dodgy but need to refresh _todos
//        refreshTodos(newTodos)
//    }
//
//    private fun refreshTodos(todoList: List<TodoItem>) {
//        _todos.update { listOf() }
//        _todos.update { todoList }
//    }
//
//    fun shouldShowTodo(todo: TodoItem): Boolean {
//        return _showCompleted.value || !isCompleted(todo)
//    }
//
//    fun collectTodos() {
//        viewModelScope.launch {
//            todoStorage.readAsFlow().collect {
//                if (!it.isMigrated) {
//                    migrateOldTodos()
//                }
//                _todos.value = it.todos
//            }
//        }
//    }
//
//    // Previous incantations of this app would have saved todos to shared prefs
//    private fun migrateOldTodos() {
//        viewModelScope.launch(ioDispatcher) {
//            val legacyTodos = todoPrefs.readTodos()
//            legacyTodos.todos.forEach {
//                commitTodo(it)
//            }
//            todoStorage.setMigrated()
//        }
//    }
//
//    private fun onlyVisibleTodos(input: List<TodoItem>): List<TodoItem> {
//        return input.filter { shouldShowTodo(it) }
//    }
//
//    private fun commitTodo(todo: TodoItem) {
//        val idx = _todos.value.indexOfFirst { it.id == todo.id }
//        val newTodos: List<TodoItem>
//        if (idx >= 0) {
//            newTodos = _todos.value.toMutableList()
//            newTodos[idx] = todo
//        } else {
//            newTodos = _todos.value + todo
//        }
//        viewModelScope.launch {
//            todoStorage.write(newTodos)
//        }
//    }
//
//    private fun toUI(todoItem: TodoItem): UITodo {
//        return UITodo(
//            id = todoItem.id,
//            title = todoItem.title.takeIf { it.isNotEmpty() } ?: "No title",
//            description = todoItem.description.takeIf { it.isNotEmpty() } ?: "No description",
//            date = formatDate(todoItem.timestamp),
//            completedTimestamp = todoItem.completedTime
//        )
//    }
//}