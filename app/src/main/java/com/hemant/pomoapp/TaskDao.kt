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

    // get all tasks
    @Query("Select * from TaskEntity")
    fun fetchAllTasks(): Flow<List<TaskEntity>>


    //get all tasks matching given id
    @Query("SELECT * FROM TaskEntity where task_id =:task_id")
    fun fetchTasksById(task_id: Int): Flow<TaskEntity>


    //get all incomplete/complete tasks from yesterday
    @Query("SELECT * FROM TaskEntity WHERE date = :yesterdayDate AND is_completed = :is_completed")
    fun fetchNotCompletedTasksAddedYesterday(
        is_completed: Boolean, yesterdayDate: String
    ): Flow<List<TaskEntity>>


    @Query("SELECT * FROM TaskEntity WHERE date BETWEEN :eightDaysAgo AND :twoDaysAgo")
    fun fetchNotCompletedTasksAddedLast7DaysExcludingTodayAndYesterday(
        eightDaysAgo: String, twoDaysAgo: String
    ): Flow<List<TaskEntity>>


    @Query("SELECT * FROM TaskEntity WHERE date >= :eightDaysAgo AND date <= :twoDaysAgo AND is_completed = 0")
    fun fetchNotCompletedTasksAddedLast7DaysExcludingTodayAndYesterdayNew(
        eightDaysAgo: String,
        twoDaysAgo: String
    ): Flow<List<TaskEntity>>


    //get incomplete/complete tasks with particular date
    @Query("SELECT * FROM TaskEntity WHERE is_completed = :is_completed AND date = :date")
    fun fetchTaskByCompletionAndDate(is_completed: Boolean, date: String): Flow<List<TaskEntity>>


    //get all completed tasks
    @Query("SELECT * FROM TaskEntity WHERE is_completed = :is_completed")
    fun fetchTaskByCompletion(is_completed: Boolean): Flow<List<TaskEntity>>


    //delete a task matching given id
    @Query("DELETE FROM TaskEntity WHERE task_id = :id")
    fun deleteTask(id: Int)
}