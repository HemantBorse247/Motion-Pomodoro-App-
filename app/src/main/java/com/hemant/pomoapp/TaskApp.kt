package com.hemant.pomoapp

import android.app.Application

class TaskApp : Application() {
    val db by lazy {
        TaskDatabase.getInstance(this)
    }
}