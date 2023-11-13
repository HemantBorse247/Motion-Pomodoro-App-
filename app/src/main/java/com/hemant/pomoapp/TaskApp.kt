package com.hemant.pomoapp

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Timer
import java.util.TimerTask


class TaskApp : Application(), ActivityLifecycleCallbacks {

    private var activityReferences: Int = 0
    private var isActivityChangingConfigurations: Boolean = false
    private var channelIDForReminders = "101"
    private var channelIDForTimers = "100"
    private var reminderTimer: Timer? = null

    private var isReminderRunning: Boolean = false

    private var isFocusMode: Boolean = true

    val db by lazy {
        TaskDatabase.getInstance(this)
    }


    private fun isNotificationPermissionAllowed(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }
        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun showNotification(content: String): Notification {
//        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
//            NotificationChannel(
//                channelID, "reminder notification", NotificationManager.IMPORTANCE_HIGH
//            )
//        )
        return NotificationCompat.Builder(this, channelIDForReminders)
            .setContentTitle("You may delay, but time will not.").setContentText(content)
            .setOnlyAlertOnce(false).setSmallIcon(R.drawable.ic_launcher).build()

    }


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        //create notification channel for reminders
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(
                channelIDForReminders, "reminder", NotificationManager.IMPORTANCE_HIGH
            )
        )
        //create notification channel for timers
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(
                channelIDForTimers, "timer", NotificationManager.IMPORTANCE_HIGH
            )
        )
    }


    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
//            Toast.makeText(this, "app in background", Toast.LENGTH_SHORT).show()
            showNotificationForDistraction()

        }


    }

    private fun showNotificationForDistraction() {
        if (isFocusMode == MainActivity.isNextPom && CountdownTimerService.isTimerInStartState == true) if (isNotificationPermissionAllowed()) {
//            Toast.makeText(this, "notification allowed", Toast.LENGTH_SHORT).show()
            reminderTimer = Timer()
            reminderTimer?.schedule(object : TimerTask() {
                override fun run() {
                    isReminderRunning = true
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(
                        2,
                        showNotification("Hey, you've been away for a bit too long.It's time to focus back on your task. 🥷🏻")
                    )
                }
            }, 5000, 3000)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
//            Toast.makeText(this, "app in foreground", Toast.LENGTH_SHORT).show()
            if (isReminderRunning) {
                reminderTimer?.cancel()
                reminderTimer?.purge()
            }
            isReminderRunning = false
        }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }


    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }


    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        reminderTimer?.cancel()
        reminderTimer?.purge()
        isReminderRunning = false
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }


}