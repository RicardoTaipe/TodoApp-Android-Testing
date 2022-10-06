package com.example.todoapp.addedittask

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.Event
import com.example.todoapp.R
import com.example.todoapp.TodoApplication
import com.example.todoapp.data.Result
import com.example.todoapp.data.Task
import com.example.todoapp.receiver.AlarmReceiver
import com.example.todoapp.tasks.TaskPriority
import com.example.todoapp.util.cancelNotifications
import com.example.todoapp.util.sendNotification
import kotlinx.coroutines.launch
import java.util.*

class AddEditTaskViewModel(application: Application) : AndroidViewModel(application) {
    //region timer related
    private val REQUEST_CODE = 0
    private val TRIGGER_TIME = "TRIGGER_AT"
    var minute: Int? = null
    var hour: Int? = null
    private val second: Long = 1_000L
    private var notifyPendingIntent: PendingIntent
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifyIntent = Intent(application, AlarmReceiver::class.java)

    //endregion

    private val tasksRepository = (application as TodoApplication).taskRepository

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    val priority = MutableLiveData(TaskPriority.NONE.ordinal)

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _taskUpdatedEvent = MutableLiveData<Event<Unit>>()
    val taskUpdatedEvent: LiveData<Event<Unit>> = _taskUpdatedEvent

    private var taskId: String? = null

    private var isNewTask: Boolean = false

    private var isDataLoaded = false

    private var taskCompleted = false

    init {
        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun saveTime() {
        with(NotificationManagerCompat.from(getApplication())) {
            cancelNotifications()
        }
        if (hour == null || minute == null) return
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour!!)
            set(Calendar.MINUTE, minute!!)
            set(Calendar.SECOND, 0)
        }

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            notifyPendingIntent
        )
    }


    fun start(taskId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.taskId = taskId
        if (taskId == null) {
            // No need to populate, it's a new task
            isNewTask = true
            return
        }
        if (isDataLoaded) {
            // No need to populate, already have data.
            return
        }

        isNewTask = false
        _dataLoading.value = true

        viewModelScope.launch {
            tasksRepository.getTask(taskId).let { result ->
                if (result is Result.Success) {
                    onTaskLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onTaskLoaded(task: Task) {
        title.value = task.title
        description.value = task.description
        priority.value = task.priority
        taskCompleted = task.isCompleted
        _dataLoading.value = false
        isDataLoaded = true
    }

    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    // Called when clicking on fab.
    fun saveTask() {
        val currentTitle = title.value
        val currentDescription = description.value
        val currentPriority = priority.value
        if (currentTitle == null || currentDescription == null) {
            _snackbarText.value = Event(R.string.empty_task_message)
            return
        }
        if (Task(currentTitle, currentDescription).isEmpty) {
            _snackbarText.value = Event(R.string.empty_task_message)
            return
        }

        if (currentPriority == null) {
            return
        }

        val currentTaskId = taskId
        if (isNewTask || currentTaskId == null) {
            createTask(Task(currentTitle, currentDescription, priority = currentPriority))
            saveTime()
        } else {
            val task = Task(
                currentTitle,
                currentDescription,
                taskCompleted,
                currentTaskId,
                priority = currentPriority
            )
            updateTask(task)
        }
    }

    fun setPriority(progress: Int) {
        priority.value = progress
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        tasksRepository.saveTask(newTask)
        _taskUpdatedEvent.value = Event(Unit)
    }

    private fun updateTask(task: Task) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch {
            tasksRepository.saveTask(task)
            _taskUpdatedEvent.value = Event(Unit)
        }
    }

}