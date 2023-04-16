package com.hemant.pomoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val task_id: Int = 0,
    val task_desc: String,
    val total_pomos: String,
    val comp_pomos: String
)