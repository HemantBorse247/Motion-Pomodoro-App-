package com.hemant.pomoapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class CountdownTimerService : Service() {

    //    private var timer: CountDownTimer? = null
    private var context: Context? = null
    private val NOTIFICATION_ID = 1
    private var channelIDForReminders = "101"
    private var channelIDForTimers = "100"
    private var isDestroyed = false
    private val binder = MyBinder()
    private var timerProgress: Long = 0
    private var titleForNotification: String? = null
    private var timerMode: String? = null

    private var clockTimer: CountDownTimer? = null

    private var totalTimeForTimerFromActivity: Long? = null

    companion object {
        private var totalTimeForTimerStatic: Long? = null
        var isTimerInStartState: Boolean? = null
    }

    //todo::


    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    //for bound service but we are not using it
    inner class MyBinder : Binder() {
        fun getService(): CountdownTimerService = this@CountdownTimerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals("start")) {
//            println("hello biches1")
//            println(intent?.getLongExtra("someName", -1))
            isTimerInStartState = true
            val timeInMillisFromActivity = intent?.getLongExtra("someName", -1)
            if (totalTimeForTimerStatic != null) { // if the timer is running for the first time i.e after a onFinish() then timerStatic will be null this time will be in milli seconds
//                println("hello biches2")
                startClock(totalTimeForTimerStatic!!)
            } else if (timeInMillisFromActivity != null && timeInMillisFromActivity != (-1).toLong()) {
//                println("hello biches3")
                totalTimeForTimerStatic =
                    timeInMillisFromActivity // initializing timerStatic so that we know that the timer has started once at-least so after this if the timer is stopped and started again the updated time will be used which means the clock will not start over again from scratch. (basically
                startClock(timeInMillisFromActivity)
            }

        } else if (intent?.action.equals("stop")) {
            isTimerInStartState = false
            stopClock()
            stopForeground(true)
            stopSelf()
//            isDestroyed = true
        } else if (intent?.action.equals("skip")) {
            //Todo:: implement logic for when auto start timer is enabled for "isTimerInStartState" as well as the stopping and restarting of timer
            isTimerInStartState = false
            stopClock()
            totalTimeForTimerStatic = null
            stopForeground(true)
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //this function shows the notification
    private fun updateNotification(data: Long) {
        val minutes = data / 1000 / 60
        val seconds = data / 1000 % 60
        val notification: Notification = showNotification("${minutes}:${seconds}")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID, notification)

    }

    //this function creates notification
    private fun showNotification(content: String): Notification {
//        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
//            NotificationChannel(
//                channelIDForTimers, "timer notification", NotificationManager.IMPORTANCE_HIGH
//            )
//        )
        titleForNotification = getModeFromMainActivity()
        return NotificationCompat.Builder(this, channelIDForTimers).setOngoing(true)
            .setContentTitle(titleForNotification).setContentText(content).setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_launcher).build()

    }

    private fun getModeFromMainActivity(): String {
        return if (MainActivity.isNextPom) "Focus Mode"
        else if (MainActivity.isNextShortBreak) "Short Break"
        else "Long Break"
    }

    private fun sendBroadcastToActivity(intent: Intent?) {
        sendBroadcast(intent)
    }


    fun startClock(totalTimeForTimerFromActivityInMillis: Long) {

        clockTimer = object : CountDownTimer(totalTimeForTimerFromActivityInMillis, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                timerProgress += 1000

                totalTimeForTimerStatic = totalTimeForTimerFromActivityInMillis - timerProgress
                Log.i("BR", "BRsent")
                sendBroadcastToActivity(
                    Intent("action_update").putExtra(
                        "milliseconds_remaining",
                        totalTimeForTimerStatic!!.toLong() // sending the remaining milliseconds to activity
                    )
                )
                updateNotification(totalTimeForTimerStatic!!)
            }

            override fun onFinish() {
                if (clockTimer != null) {
                    clockTimer?.cancel()
                    totalTimeForTimerStatic = null
                    sendBroadcastToActivity(Intent("action_update").putExtra("isFinished", true))

                    stopForeground(true)
                    stopSelf()
                }
            }
        }.start()
    }

    fun stopClock() {
        if (clockTimer != null) {
            clockTimer?.cancel()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        //this starts the notification ig?
        var minutes = 25.toLong()
        if (totalTimeForTimerStatic != null) minutes = totalTimeForTimerStatic!! / 1000 / 60
        startForeground(
            NOTIFICATION_ID, showNotification("$minutes:00")
        )
//        sendDataToActivity()
    }

}