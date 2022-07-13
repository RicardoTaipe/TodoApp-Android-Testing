package com.example.todoapp.tasks

import android.graphics.Paint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.data.Task
import com.example.todoapp.util.convertLongToDateString

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