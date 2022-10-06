package com.example.todoapp.addedittask

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.EventObserver
import com.example.todoapp.R
import com.example.todoapp.databinding.AddEditTaskFragmentBinding
import com.example.todoapp.tasks.ADD_EDIT_RESULT_OK
import com.example.todoapp.util.setupRefreshLayout
import com.example.todoapp.util.setupSnackbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class AddEditTaskFragment : Fragment() {

    private lateinit var binding: AddEditTaskFragmentBinding

    private val args: AddEditTaskFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditTaskViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddEditTaskFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(binding.refreshLayout)
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
        binding.reminderButton.setOnClickListener {
            showTimePicker()
        }
        viewModel.start(args.taskId)
    }

    private fun showTimePicker() {
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Appointment time")
                .build()
        picker.addOnPositiveButtonClickListener {
            viewModel.hour = picker.hour
            viewModel.minute = picker.minute
            binding.reminderButton.text = "${picker.hour} : ${picker.minute}"
        }
        picker.show(parentFragmentManager, "tag")

    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.taskUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            val action = AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}