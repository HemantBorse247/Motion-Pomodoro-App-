package com.hemant.pomoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hemant.pomoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var taskIdFromTaskSelection: Int = -1
    private var allTaskList: ArrayList<TaskEntity>? = null
    private val dataStore = DataStoreManager(this@MainActivity)

    //media player for alarm sound
    private var mp: MediaPlayer? = null


    // this is the taskSelection activity launcher for getting a result back
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //this runs when the activity sends a result back
            if (it.resultCode == Activity.RESULT_OK) {
                taskIdFromTaskSelection = it.data?.getIntExtra("task_id", -1)!!
                lifecycleScope.launch {
                    dataStore.saveCurrentTaskToDataStore(taskIdFromTaskSelection)
                }
                setupCurrentTask()
            }
        }

    // runs when settings activity is closed
    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    changeSettings()
                }
            }
        }

    private var binding: ActivityMainBinding? = null

    private var countPomoTillLongBreak: Int = 3
    private var isNextPom: Boolean = true
    private var isNextShortBreak: Boolean = false
    private var isNextLongBreak: Boolean = false

    private var totalTimeForTimer: Long? = null
    private var totalTimeForPomo: Long = 25
    private var totalTimeForShortBreak: Long = 5
    private var totalTimeForLongBreak: Long = 15


    private var clockTimer: CountDownTimer? = null

    //    private var alarmTimer: CountDownTimer? = null
    private var timerProgress = 0

    private var autoResumeTimer: Boolean = false
    private var enableSound: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lifecycleScope.launch {
            changeSettings()
            totalTimeForTimer = if (dataStore.getFocusLengthFromDataStore() != null) {
                totalTimeForPomo * 60 * 1000
            } else {
                25 * 60 * 1000
            }

            val taskid: Int? = dataStore.getCurrentTaskFromDataStore()
            if (taskid != null && taskid != -1) {
                taskIdFromTaskSelection = taskid
                setupCurrentTask()
            }
        }


//      instances of DAO's
        val taskDao = (application as TaskApp).db.taskDao()
        val pomoDao = (application as TaskApp).db.pomoDao()


//      fetching all tasks from the db and storing in allTaskList
        lifecycleScope.launch {
            taskDao.fetchTaskByCompletion(false).collect {
                allTaskList = ArrayList(it)
            }
        }

        // setting time on timer
//        setTextForMinTimer(totalTimeForPomo)

        // play/pause the timer
        binding?.playPauseBtn?.setOnClickListener {
            onPlayPause(pomoDao, taskDao)
        }
        // skipping timer
        binding?.skipBtn?.setOnClickListener {
            onSkip(pomoDao, taskDao)
        }

        //imp navigation bar onclick listeners
        //imp analytics page button onclick listener
        binding?.ivAnalytics?.setOnClickListener {
            val intent = Intent(this@MainActivity, AnalyticsActivity::class.java)
            startActivity(intent)
        }
        //imp taskList page button onclick listener
        binding?.ivTaskList?.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskListActivity::class.java)
            startActivity(intent)
        }
        //imp setting current task onclick listener
        binding?.llCurrentTaskContainer?.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskSelectionActivity::class.java)
            launcher.launch(intent)
        }
        binding?.ivSettings?.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }
    }


    private fun startClock(
        pomoDao: PomodoroDao, taskDao: TaskDao, totalTimeForTimerInMilliSecs: Long
    ) {

        clockTimer = object : CountDownTimer(totalTimeForTimerInMilliSecs, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                timerProgress += 1
//                var timeInMils = (totalTimeForPomo) * ((60 * 1000))
                totalTimeForTimer = totalTimeForTimer!! - 1000
                val totalTimeRemainingInSecs =
                    (totalTimeForTimerInMilliSecs / 1000) - (timerProgress)
                val remainingMinutes = totalTimeRemainingInSecs / 60
                val remainingSecondsInCurrMinute =
                    totalTimeRemainingInSecs - (remainingMinutes * 60)
                setTextForMinTimer(remainingMinutes)
                setTextForSecTimer(remainingSecondsInCurrMinute)
            }

            override fun onFinish() {

                if (enableSound) {
                    mp = MediaPlayer.create(this@MainActivity, R.raw.clock_alarm_8761)
                    mp!!.start()

                }

                if (clockTimer != null) {
                    if (binding?.playPauseBtn?.text == "Pause") binding?.playPauseBtn?.text = "Play"
                    clockTimer?.cancel()
                    timerProgress = 0
                }
                if (isNextPom) {
                    lifecycleScope.launch {
                        pomoDao.insert(PomodoroEntity(0, getDate(), true, totalTimeForPomo))
                    }
                    if (taskIdFromTaskSelection != -1) {
                        val task: TaskEntity? = findTaskById(taskIdFromTaskSelection)
                        if (task == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "Current task was deleted. Please set another task to work on",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            task.comp_pomos = (task.comp_pomos?.toInt()!! + 1).toString()
                            if (task.comp_pomos == task.total_pomos.drop(1)) {
                                task.is_completed = true
                            }
                            lifecycleScope.launch {
                                taskDao.update(task)
                            }
                        }

                        setupCurrentTask()

                    }
                }

                modeSelection(pomoDao, taskDao)
            }
        }.start()
    }


    private fun onPlayPause(pomoDao: PomodoroDao, taskDao: TaskDao) {
        if (binding?.playPauseBtn?.text == "Play") {
            var time = (totalTimeForTimer!!) /*- (timerProgress * 1000)*/
            timerProgress = 0
            startClock(pomoDao, taskDao, time)
            binding?.playPauseBtn?.text = "Pause"
            binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_blackk)
            binding?.tvTimerSecs?.setTypeface(resources.getFont(R.font.roboto_blackk))
        } else if (binding?.playPauseBtn?.text == "Pause") {
            if (clockTimer != null) {
                clockTimer?.cancel()
            }
            binding?.playPauseBtn?.text = "Play"
            binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_regular)
            binding?.tvTimerSecs?.typeface = resources.getFont(R.font.roboto_regular)
        }
    }

    private fun modeSelection(pomoDao: PomodoroDao, taskDao: TaskDao) {
        if (isNextPom) {
            countPomoTillLongBreak -= 1
            isNextPom = false
            if (countPomoTillLongBreak == 0) {
                isNextLongBreak = true
                countPomoTillLongBreak = 3
                prepareForLong()
            } else {
                isNextShortBreak = true
                prepareForShort()
            }

        } else {
            if (isNextShortBreak) {
                isNextShortBreak = false
                isNextPom = true
            } else {
                isNextLongBreak = false
                isNextPom = true
            }
            prepareForPom()
        }
        if (autoResumeTimer) onPlayPause(pomoDao, taskDao)
    }

    private fun prepareForPom() {
        setTextForMinTimer(totalTimeForPomo)
        setTextForSecTimer(0)
        setTextForFocusMode("Focus Mode")
        totalTimeForTimer = totalTimeForPomo * 60 * 1000
    }


    private fun prepareForShort() {
        setTextForMinTimer(totalTimeForShortBreak)
        setTextForSecTimer(0)
        setTextForFocusMode("Short Break")
        totalTimeForTimer = totalTimeForShortBreak * 60 * 1000
    }

    private fun prepareForLong() {
        setTextForMinTimer(totalTimeForLongBreak)
        setTextForSecTimer(0)
        setTextForFocusMode("Long Break")
        totalTimeForTimer = totalTimeForLongBreak * 60 * 1000
    }


    private fun onSkip(pomoDao: PomodoroDao, taskDao: TaskDao) {
        if (clockTimer != null) {
            clockTimer?.cancel()
            clockTimer = null
            timerProgress = 0
        }
        if (binding?.playPauseBtn?.text != "Play") {
            binding?.playPauseBtn?.text = "Play"
        }
        binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_regular)
        binding?.tvTimerSecs?.typeface = resources.getFont(R.font.roboto_regular)
        modeSelection(pomoDao, taskDao)
//        setTextForMinTimer(totalTimeForPomo)
//        setTextForSecTimer(0)
    }

    private fun setTextForMinTimer(time: Long) {
        if (time < 10) binding?.tvMinuteTimer?.text = "0$time"
        else binding?.tvMinuteTimer?.text = "$time"
    }

    private fun setTextForSecTimer(time: Long) {
        if (time < 10) binding?.tvTimerSecs?.text = "0$time"
        else binding?.tvTimerSecs?.text = "$time"
    }

    private fun setTextForFocusMode(s: String) {
        binding?.tvFocusModeText?.text = s
    }

    private fun setBgForFocusMode(draw: Int) {
        binding?.llModeContainer?.setBackgroundResource(draw)
    }

    private fun getDate(): String {
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(dateTime)
    }

//    fun getDateInDate(): Date {
//        val c = Calendar.getInstance()
//        return c.time
//    }

    private fun findTaskById(taskId: Int): TaskEntity? {
        return allTaskList?.find { task -> task.task_id == taskId }
    }

    fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()
        supportActionBar?.hide()

        // For devices with API level 30 and above, use the following code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            // For devices with API level below 30, use the following code
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun setupCurrentTask() {
//        val indexOfSelectedTask = taskIdFromTaskSelection - 1
        val task: TaskEntity? = findTaskById(taskIdFromTaskSelection)
//        val compPomo = task?.comp_pomos
//        val totalPomo = task?.total_pomos!!.drop(1)
        if (task != null) {
            if (task.comp_pomos.toInt() == task.total_pomos.drop(1).toInt()) {
                Toast.makeText(
                    this@MainActivity,
                    "The task you selected is complete. Please select another task",
                    Toast.LENGTH_SHORT
                ).show()
                taskIdFromTaskSelection = -1
                lifecycleScope.launch {
                    dataStore.saveCurrentTaskToDataStore(-1)
                }
                binding?.tvTaskDesc?.gravity = Gravity.CENTER
                binding?.tvTaskDesc?.text = "SET CURRENT TASK"
                binding?.tvCompPomos?.text = ""
                binding?.tvTotalPomo?.text = ""
            } else {
                binding?.tvTaskDesc?.gravity = Gravity.START
                binding?.tvTaskDesc?.text = task.task_desc
                binding?.tvCompPomos?.text = task.comp_pomos
                binding?.tvTotalPomo?.text = task.total_pomos
            }
        } else {
            taskIdFromTaskSelection = -1
            binding?.tvTaskDesc?.gravity = Gravity.CENTER
            binding?.tvTaskDesc?.text = "SET CURRENT TASK"
            binding?.tvCompPomos?.text = ""
            binding?.tvTotalPomo?.text = ""
        }

    }

    suspend fun changeSettings() {
        if (dataStore.getFocusLengthFromDataStore() != null) {
            totalTimeForPomo = dataStore.getFocusLengthFromDataStore()!!.toLong()
        }
        if (dataStore.getShortBreakLengthFromDataStore() != null) {
            totalTimeForShortBreak = dataStore.getShortBreakLengthFromDataStore()!!.toLong()
        }
        if (dataStore.getLongBreakLengthFromDataStore() != null) {
            totalTimeForLongBreak = dataStore.getLongBreakLengthFromDataStore()!!.toLong()
        }
        if (dataStore.getPomoInterval() != null) {
            countPomoTillLongBreak = dataStore.getPomoInterval()!!.toInt()
        }
        if (dataStore.getAutoResumeTimer() != null) {
            autoResumeTimer = dataStore.getAutoResumeTimer()!!
        }
        if (dataStore.getSound() != null) {
            enableSound = dataStore.getSound()!!
        }
        if (clockTimer == null) {
            if (isNextPom) {
                totalTimeForTimer = totalTimeForPomo * 60 * 1000
                setTextForMinTimer(totalTimeForPomo)
            } else if (isNextShortBreak) {
                totalTimeForTimer = totalTimeForShortBreak * 60 * 1000
                setTextForMinTimer(totalTimeForShortBreak)
            } else {
                totalTimeForTimer = totalTimeForLongBreak * 60 * 1000
                setTextForMinTimer(totalTimeForLongBreak)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) binding = null
    }
}