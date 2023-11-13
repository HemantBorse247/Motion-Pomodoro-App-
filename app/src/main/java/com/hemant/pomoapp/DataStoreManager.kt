package com.hemant.pomoapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(val context: Context) {

    companion object {
        val FOCUS_LENGTH = stringPreferencesKey("focusLength")
        val SHORT_BREAK_LENGTH = stringPreferencesKey("shortBreakLength")
        val LONG_BREAK_LENGTH = stringPreferencesKey("longBreakLength")
        val POMO_INTERVAL = stringPreferencesKey("pomoInterval")
        val AUTO_TIMER = booleanPreferencesKey("autoResumeTimer")
        val SOUND = booleanPreferencesKey("sound")
        val CURRENT_TASK = intPreferencesKey("currentTask")
        val IS_ULTRA_FOCUS_FIRST_TIME = booleanPreferencesKey("isUltraFocusFirstTime")

    }


    suspend fun saveToDataStore(settings: Settings) {
        context.dataStore.edit {
            it[FOCUS_LENGTH] =
                if (settings.focusLengthValue.isNotEmpty()) settings.focusLengthValue else "1"
            it[SHORT_BREAK_LENGTH] =
                if (settings.shortBreakLengthValue.isNotEmpty()) settings.shortBreakLengthValue else "1"
            it[LONG_BREAK_LENGTH] =
                if (settings.longBreakLengthValue.isNotEmpty()) settings.longBreakLengthValue else "1"
            it[POMO_INTERVAL] =
                if (settings.pomodoroInterval.isNotEmpty()) settings.pomodoroInterval else "1"
            it[AUTO_TIMER] = settings.autoResumeTimer
            it[SOUND] = settings.sound
        }
    }

    suspend fun saveCurrentTaskToDataStore(taskId: Int) {
        context.dataStore.edit {
            it[CURRENT_TASK] = taskId
        }
    }

    suspend fun getCurrentTaskFromDataStore(): Int? {
        return context.dataStore.data.first()[CURRENT_TASK]
    }

    suspend fun changeUltraFocusFirstTime() {
        context.dataStore.edit {
            it[IS_ULTRA_FOCUS_FIRST_TIME] = false
        }
    }

    suspend fun getUltraFocusFirstTime(): Boolean? {
        return context.dataStore.data.first()[IS_ULTRA_FOCUS_FIRST_TIME]
    }


    suspend fun getFocusLengthFromDataStore(): String? {
        val values = context.dataStore.data.first()
        return values[FOCUS_LENGTH]
    }

    suspend fun getShortBreakLengthFromDataStore(): String? {
        val values = context.dataStore.data.first()
        return values[SHORT_BREAK_LENGTH]
    }

    suspend fun getLongBreakLengthFromDataStore(): String? {
        val values = context.dataStore.data.first()
        return values[LONG_BREAK_LENGTH]
    }

    suspend fun getPomoInterval(): String? {
        val values = context.dataStore.data.first()
        return values[POMO_INTERVAL]
    }

    suspend fun getAutoResumeTimer(): Boolean? {
        val values = context.dataStore.data.first()
        return values[AUTO_TIMER]
    }

    suspend fun getSound(): Boolean? {
        val values = context.dataStore.data.first()
        return values[SOUND]
    }

}



