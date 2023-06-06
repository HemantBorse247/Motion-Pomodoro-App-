package com.hemant.pomoapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {

    @Insert
    suspend fun insert(pomodoroEntity: PomodoroEntity)

    @Update
    suspend fun update(pomodoroEntity: PomodoroEntity)

    @Delete
    suspend fun delete(pomodoroEntity: PomodoroEntity)

    @Query("SELECT * FROM PomodoroEntity where start_date =:date")
    fun fetchTasksByDate(date: String): Flow<List<PomodoroEntity>>
}