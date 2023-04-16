package com.hemant.pomoapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(taskEntity: TaskEntity)

    @Update
    suspend fun update(taskEntity: TaskEntity)

    @Delete
    suspend fun delete(taskEntity: TaskEntity)

    @Query("Select * from TaskEntity")
    fun fetchAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity where task_id =:task_id")
    fun fetchTasksById(task_id: Int): Flow<TaskEntity>

}