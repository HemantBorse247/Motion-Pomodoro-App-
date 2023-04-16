package com.hemant.pomoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hemant.pomoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var task_id_from_task_selection: Int? = null

    private var allTaskList: ArrayList<TaskEntity>? = null

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {

                task_id_from_task_selection = it.data?.getIntExtra("task_id", -1)
//                Toast.makeText(
//                    this@MainActivity,
//                    "${it.data?.getIntExtra("task_id", -1)} is the data",
//                    Toast.LENGTH_SHORT
//                ).show()
                val indexOfSelectedTask = task_id_from_task_selection!! - 1
                binding?.tvTaskDesc?.text = allTaskList?.get(indexOfSelectedTask)?.task_desc
                binding?.tvCompPomos?.text = allTaskList?.get(indexOfSelectedTask)?.comp_pomos
                binding?.tvTotalPomo?.text = allTaskList?.get(indexOfSelectedTask)?.total_pomos
            }
        }

    private var binding: ActivityMainBinding? = null

    private var isNextPom: Boolean = true
    private var isNextShortBreak: Boolean? = null
    private var isNextLongBreak: Boolean = false

    private var totalTimeForTimer: Long? = null
    private var totalTimeForPomo: Long = 20
    private var totalTimeForShortBreak: Long = 5
    private var totalTimeForLongBreak: Long = 15


    private var restTimer: CountDownTimer? = null
    private var timerProgress = 0


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//      instance of taskDao
        val taskdao = (application as TaskApp).db.taskDao()

//      fetching all tasks from the db and storing in allTaskList
        lifecycleScope.launch {
            taskdao.fetchAllTasks().collect {
                allTaskList = ArrayList(it)
            }
        }

        setTextForMinTimer(totalTimeForPomo)
        // invokes on start stop button
        binding?.playPauseBtn?.setOnClickListener {
            onPlayPause()
        }
        // when skip button is pressed
        binding?.skipBtn?.setOnClickListener {
            onSkip()
        }

        // navigation button
        binding?.ivAnalytics?.setOnClickListener {
            val intent = Intent(this@MainActivity, AnalyticsActivity::class.java)
            startActivity(intent)
        }
        binding?.ivTaskList?.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskListActivity::class.java)
            startActivity(intent)
        }

//      to select current task
        binding?.llCurrentTaskContainer?.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskSelectionActivity::class.java)
            launcher.launch(intent)
        }
    }


    private fun onSkip() {
        if (restTimer != null) {
            restTimer?.cancel()
            timerProgress = 0
        }
        if (binding?.playPauseBtn?.text != "Start") {
            binding?.playPauseBtn?.text = "Start"
        }
        binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_regular)
        binding?.tvTimerSecs?.typeface = resources.getFont(R.font.roboto_regular)
        setTextForMinTimer(totalTimeForPomo)
        setTextForSecTimer(0)
    }

    private fun setTextForMinTimer(time: Long) {

        if (time < 10) {
            binding?.tvMinuteTimer?.text = "0$time"

        } else {
            binding?.tvMinuteTimer?.text = "$time"
        }
    }

    private fun setTextForSecTimer(time: Long) {
        if (time < 10) {
            binding?.tvTimerSecs?.text = "0$time"

        } else {
            binding?.tvTimerSecs?.text = "$time"
        }
    }

    private fun onPlayPause() {
        if (binding?.playPauseBtn?.text == "Start") {
            binding?.playPauseBtn?.text = "Pause"
//            totalTimeForPomo = 20
            startClock()
            binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_blackk)
            binding?.tvTimerSecs?.setTypeface(resources.getFont(R.font.roboto_blackk))
        } else if (binding?.playPauseBtn?.text == "Pause") {
            binding?.playPauseBtn?.text = "Start"
            binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_regular)
            binding?.tvTimerSecs?.typeface = resources.getFont(R.font.roboto_regular)
            if (restTimer != null) {
                restTimer?.cancel()
//                restProgress = 0
            }
        }
    }


    private fun startClock() {
        if (isNextPom) {
            totalTimeForTimer = totalTimeForPomo
        } else if (isNextShortBreak!!) {
            totalTimeForTimer = totalTimeForShortBreak
        } else {
//            Toast.makeText(
//                this@MainActivity,
//                "this is from else block shortBreak",
//                Toast.LENGTH_LONG
//            ).show()
        }
        restTimer = object : CountDownTimer(totalTimeForTimer!!.times((60 * 1000)), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                timerProgress++
//                var timeInMils = (totalTimeForPomo) * ((60 * 1000))
                val totalTimeRemainingInSecs = (totalTimeForTimer!! * 60) - (timerProgress)
                val remainingMinutes = totalTimeRemainingInSecs / 60
                val remainingSecondsInCurrMinute =
                    totalTimeRemainingInSecs - (remainingMinutes * 60)

                setTextForMinTimer(remainingMinutes)
//                if (minutes < 10) {
//                    binding?.tvMinuteTimer?.text = "0$minutes"
//                }
                setTextForSecTimer(remainingSecondsInCurrMinute)
//                if (seconds < 10) {
//                    binding?.tvTimerSecs?.text = "0$seconds"
//                } else {
//                    binding?.tvTimerSecs?.text = seconds.toString()
//                }
            }

            override fun onFinish() {
                if (restTimer != null) {
                    if (binding?.playPauseBtn?.text == "Pause") binding?.playPauseBtn?.text =
                        "Start"
                    restTimer?.cancel()
                    timerProgress = 0
                }
                Toast.makeText(this@MainActivity, "hehe timer khatam", Toast.LENGTH_SHORT).show()
                if (isNextPom) {
                    isNextPom = false
                    isNextShortBreak = true
                    prepareForShort()
                } else if (isNextShortBreak!!) {
//                    Toast.makeText(this@MainActivity, "is next short false", Toast.LENGTH_SHORT).show()
                    isNextShortBreak = false
                    isNextPom = true
                    prepareForPom()
                }
            }
        }.start()
    }

    private fun prepareForPom() {
        setTextForMinTimer(totalTimeForPomo)
        setTextForSecTimer(0)
    }

    private fun prepareForShort() {

//        if (totalTimeForShortBreak < 10) {
//            binding?.tvMinuteTimer?.text = "0$totalTimeForShortBreak"
////            Toast.makeText(this@MainActivity, "after helo biches", Toast.LENGTH_SHORT).show()
//        } else {
//            binding?.tvMinuteTimer?.text = "$totalTimeForShortBreak"
//        }
        setTextForMinTimer(totalTimeForShortBreak)
        setTextForSecTimer(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) binding = null
    }
}