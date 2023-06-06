package com.hemant.pomoapp

data class Settings(
    val focusLengthValue: String,
    val shortBreakLengthValue: String,
    val longBreakLengthValue: String,
    val pomodoroInterval: String,
    val autoResumeTimer: Boolean,
    val sound: Boolean
)
