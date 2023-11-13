package com.hemant.pomoapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val task_id: Int = 0,
    var task_desc: String,
    var total_pomos: String,
    var comp_pomos: String,
    var is_completed: Boolean = false,
    var date: Date
)