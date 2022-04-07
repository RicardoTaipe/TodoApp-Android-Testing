package com.example.todoapp.statistics

import androidx.lifecycle.*
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Result.Error
import com.example.todoapp.data.Result
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {
    private val tasks: LiveData<Result<List<Task>>> = tasksRepository.observeTasks()
    private val _dataLoading = MutableLiveData<Boolean>(false)
    private val stats: LiveData<StatsResult?> = tasks.map {
        if (it is Success) {
            getActiveAndCompletedStats(it.data)
        } else {
            null
        }
    }
    val activeTasksPercent = stats.map {
        it?.activeTasksPercent ?: 0f
    }
    val completedTasksPercent: LiveData<Float> = stats.map { it?.completedTasksPercent ?: 0f }
    val dataLoading: LiveData<Boolean> = _dataLoading
    val error: LiveData<Boolean> = tasks.map { it is Error }
    val empty: LiveData<Boolean> = tasks.map { (it as? Success)?.data.isNullOrEmpty() }

    fun refresh() {
        _dataLoading.value = true
        viewModelScope.launch {
            tasksRepository.refreshTasks()
            _dataLoading.value = false
        }
    }
}