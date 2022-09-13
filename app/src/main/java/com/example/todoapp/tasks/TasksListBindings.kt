package com.example.todoapp.tasks

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.Task
import com.example.todoapp.util.convertLongToDateString
import com.google.android.material.progressindicator.LinearProgressIndicator

@BindingAdapter("items")
fun setItems(recyclerView: RecyclerView, items: List<Task>?) {
    items?.let {
        (recyclerView.adapter as TasksAdapter).submitList(it)
    }
}

@BindingAdapter("completedTask")
fun setStyle(textView: TextView, enabled: Boolean) {
    if (enabled) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

@BindingAdapter("formattedDate")
fun setDate(textView: TextView, date: Long) {
    textView.text = convertLongToDateString(date)
}

@BindingAdapter("progress")
fun setProgress(progressIndicator: LinearProgressIndicator, progress: Float) {
    progressIndicator.progress = progress.toInt()
}

@BindingAdapter("priorityColor")
fun setPriorityColor(view: View, priority: Int) {
    val context = view.context
    val colors = context.resources.getStringArray(R.array.note_color_array)
    view.setBackgroundColor(Color.parseColor(colors[priority]))
}
