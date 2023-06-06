package com.hemant.pomoapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PomodoroEntity(
    @PrimaryKey(autoGenerate = true) val pomo_id: Int,
    @ColumnInfo(name = "start_date") val date: String,
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean?,
    @ColumnInfo(name = "total_pomo_time") val totalTimeInMinutes: Long?

)
