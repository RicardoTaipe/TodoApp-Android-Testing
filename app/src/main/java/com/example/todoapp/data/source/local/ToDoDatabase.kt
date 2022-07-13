package com.example.todoapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.data.Task
//TODO search info about migration
@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao
}