package com.hemant.pomoapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskEntity: TaskEntity)

    @Update
    suspend fun update(taskEntity: TaskEntity)

    @Delete
    suspend fun delete(taskEntity: TaskEntity)

    @Query("Select * from TaskEntity")
    fun fetchAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity where task_id =:task_id")
    fun fetchTasksById(task_id: Int): Flow<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE is_completed = :is_completed AND date = :date")
    fun fetchTaskByCompletionAndDate(is_completed: Boolean, date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity WHERE is_completed = :is_completed")
    fun fetchTaskByCompletion(is_completed: Boolean): Flow<List<TaskEntity>>


    @Query("DELETE FROM TaskEntity WHERE task_id = :id")
    fun deleteTask(id: Int)


}