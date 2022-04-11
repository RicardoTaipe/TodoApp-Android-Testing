package com.example.todoapp.taskdetail

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.example.todoapp.Event
import com.example.todoapp.R
import com.example.todoapp.data.Result
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Task
import com.example.todoapp.domain.ActivateTaskUseCase
import com.example.todoapp.domain.CompleteTaskUseCase
import com.example.todoapp.domain.DeleteTaskUseCase
import com.example.todoapp.domain.GetTaskUseCase
import com.example.todoapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val getTaskUseCase: GetTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val activateTaskUseCase: ActivateTaskUseCase
) : ViewModel() {

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editTaskEvent = MutableLiveData<Event<Unit>>()
    val editTaskEvent: LiveData<Event<Unit>> = _editTaskEvent

    private val _deleteTaskEvent = MutableLiveData<Event<Unit>>()
    val deleteTaskEvent: LiveData<Event<Unit>> = _deleteTaskEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val taskId: String?
        get() = _task.value?.id

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = _task.map { input: Task? ->
        input?.isCompleted ?: false
    }

    fun deleteTask() = viewModelScope.launch {
        taskId?.let {
            deleteTaskUseCase(it)
            _deleteTaskEvent.value = Event(Unit)
        }
    }

    fun editTask() {
        _editTaskEvent.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = _task.value ?: return@launch
        if (completed) {
            completeTaskUseCase(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            activateTaskUseCase(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String, forceRefresh: Boolean = false) {
        // If we're already loading or already loaded, return (might be a config change)
        if (_isDataAvailable.value == true && !forceRefresh || _dataLoading.value == true) {
            return
        }
        // Show loading indicator
        _dataLoading.value = true
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                if (taskId != null) {
                    getTaskUseCase(taskId, false).let { result ->
                        if (result is Success) {
                            onTaskLoaded(result.data)
                        } else {
                            onDataNotAvailable(result)
                        }
                    }
                }
                _dataLoading.value = false
            }
        }
    }

    private fun setTask(task: Task?) {
        this._task.value = task
        _isDataAvailable.value = task != null
    }

    private fun onTaskLoaded(task: Task) {
        setTask(task)
    }

    private fun onDataNotAvailable(result: Result<Task>) {
        _task.value = null
        _isDataAvailable.value = false
    }

    fun refresh() {
        taskId?.let { start(it, true) }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}