package com.example.todoapp.statistics

import androidx.lifecycle.*
import com.example.todoapp.data.Result.Error
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Task
import com.example.todoapp.domain.GetTasksUseCase
import com.example.todoapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val getTasksUseCase: GetTasksUseCase
) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading
    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    private val _empty = MutableLiveData<Boolean>()
    val empty: LiveData<Boolean> = _empty

    private val _activeTasksPercent = MutableLiveData<Float>()
    val activeTasksPercent: LiveData<Float> = _activeTasksPercent

    private val _completedTasksPercent = MutableLiveData<Float>()
    val completedTasksPercent: LiveData<Float> = _completedTasksPercent


    private var activeTasks = 0

    private var completedTasks = 0

    init {
        start()
    }

    fun start() {
        if (_dataLoading.value == true) {
            return
        }
        _dataLoading.value = true

        wrapEspressoIdlingResource {
            viewModelScope.launch {
                getTasksUseCase().let { result ->
                    if (result is Success) {
                        _error.value = false
                        computeStats(result.data)
                    } else {
                        _error.value = true
                        activeTasks = 0
                        completedTasks = 0
                        computeStats(null)
                    }
                }
            }
        }
    }

    fun refresh() {
        start()
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>?) {
        getActiveAndCompletedStats(tasks).let {
            _activeTasksPercent.value = it.activeTasksPercent
            _completedTasksPercent.value = it.completedTasksPercent
        }
        _empty.value = tasks.isNullOrEmpty()
        _dataLoading.value = false
    }
}