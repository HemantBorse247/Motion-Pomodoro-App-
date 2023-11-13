package com.hemant.pomoapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hemant.pomoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    companion object {
        var isNextPom: Boolean = true
        var isNextShortBreak: Boolean = false
        var isNextLongBreak: Boolean = false
    }


    private var taskIdFromTaskSelection: Int = -1
    private var allTaskList: ArrayList<TaskEntity>? = null
    private val dataStore = DataStoreManager(this@MainActivity)
    private var isUltraFocusEnabled: Boolean = false
    private var audioManager: AudioManager? = null
    private var isFullScreen: Boolean = false
    private var isUltraFirst: Boolean? = null

    private var ultraFocus: UltraFocus? = null


    private val CHANNEL_ID = "100"

    //media player for alarm sound
    private var mp: MediaPlayer? = null


    //service intent
    var serviceIntent: Intent? = null

    private var binding: ActivityMainBinding? = null

    private var countPomoTillLongBreak: Int = 3


    private var totalTimeForTimer: Long? = null
    private var totalTimeForPomo: Long = 25
    private var totalTimeForShortBreak: Long = 5
    private var totalTimeForLongBreak: Long = 15

//    private var taskdao: TaskDao? = null
//    private var pomodao: PomodoroDao? = null


    private var clockTimer: CountDownTimer? = null
    private var autoResumeTimer: Boolean = false
    private var enableSound: Boolean = false


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

//    private var notificationManager: NotificationManager? = null

    //creating broadcast receiver here
    private val updateTimeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getBooleanExtra("isFinished", false) == true) {
                //TODO:: implement what to do when timer is finished
                if (binding?.playPauseBtn?.text == "Pause") {
                    binding?.playPauseBtn?.text = "Play"
                } else {
                    binding?.playPauseBtn?.text = "Pause"
                }
                //imp:: play sound if sound is enabled
                if (enableSound) {
                    mp = MediaPlayer.create(this@MainActivity, R.raw.clock_alarm_8761)
                    mp!!.start()
                }
                //add pomodoro to pomodoro entity
                val taskDao = (application as TaskApp).db.taskDao()
                val pomoDao = (application as TaskApp).db.pomoDao()
                if (isNextPom) {
                    //imp:: add new pomodoro to table
                    addPomodoro(pomoDao)
                    if (taskIdFromTaskSelection != -1) {
                        val task: TaskEntity? = findTaskById(taskIdFromTaskSelection)
                        if (task == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "Current task was deleted. Please set another task to work on",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            task.comp_pomos = (task.comp_pomos.toInt() + 1).toString()
                            if (task.comp_pomos == task.total_pomos.drop(1)) {
                                task.is_completed = true
                            }
                            //imp:: if there exists a task already in current task container we will update it here :
                            updateTask(taskDao, task)
                        }
                        setupCurrentTask()
                    }
                }
                modeSelection(pomoDao, taskDao)
            } else {
                val number = intent?.getLongExtra("milliseconds_remaining", -1)
                val minutes = number!! / 1000 / 60
                val seconds = number / 1000 % 60
                setTextForSecTimer(seconds)
                setTextForMinTimer(minutes)
            }
        }
    }


    private fun addPomodoro(pomoDao: PomodoroDao) {
        lifecycleScope.launch {
            pomoDao.insert(PomodoroEntity(0, getDate(), true, totalTimeForPomo))
        }
    }

    private fun updateTask(taskDao: TaskDao, task: TaskEntity) {
        lifecycleScope.launch {
            taskDao.update(task)
        }
    }

    private fun onPlayPause(pomoDao: PomodoroDao, taskDao: TaskDao) {
        if (binding?.playPauseBtn?.text == "Play") {
            // send intent to service to start timer here:
            serviceIntent?.apply {
                action = "start"
//                putExtra("someName", (1234).toLong())
                if (isNextPom) {
                    putExtra("someName", totalTimeForPomo * 60 * 1000)
                } else if (isNextShortBreak) {
                    putExtra("someName", totalTimeForShortBreak * 60 * 1000)
                } else if (isNextLongBreak) {
                    putExtra("someName", totalTimeForLongBreak * 60 * 1000)
                }
            }
            startService(serviceIntent)
            binding?.playPauseBtn?.text = "Pause"
            binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_blackk)
            binding?.tvTimerSecs?.setTypeface(resources.getFont(R.font.roboto_blackk))
        } else if (binding?.playPauseBtn?.text == "Pause") {

            // send intent to service to stop the timer here:
            serviceIntent?.action = "stop"
            if (isNextPom) {
                serviceIntent?.putExtra("time_for_timer", (2436).toLong())
            } else {
                serviceIntent?.putExtra("time_for_timer", (2437).toLong())
            }
            startService(serviceIntent)

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
        //TODO:: work on auto resume timer
//        if (autoResumeTimer) onPlayPause(pomoDao, taskDao)
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
        serviceIntent?.action = "skip"
        startService(serviceIntent)
        if (binding?.playPauseBtn?.text != "Play") {
            binding?.playPauseBtn?.text = "Play"
        }
        binding?.tvMinuteTimer?.typeface = resources.getFont(R.font.roboto_regular)
        binding?.tvTimerSecs?.typeface = resources.getFont(R.font.roboto_regular)
        modeSelection(pomoDao, taskDao)
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


    private fun findTaskById(taskId: Int): TaskEntity? {
        return allTaskList?.find { task -> task.task_id == taskId }
    }

    private fun hideStatusBar() {
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
        val task: TaskEntity? = findTaskById(taskIdFromTaskSelection)
        if (task != null) {
            if (task.comp_pomos.toInt() == task.total_pomos.drop(1).toInt()) {
                Toast.makeText(
                    this@MainActivity,
                    "The task you selected is complete. Please select another task",
                    Toast.LENGTH_SHORT
                ).show()
                //TODO:: we shold show a congratulations dialog for completing a task completely
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

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this@MainActivity, "permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Please give permission", Toast.LENGTH_SHORT)
                    .show()

            }
        }

    private fun isNotificationPermissionAllowed(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }
        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun requestNotificationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            showRationaleDialog(
                "Allow notification permission",
                "Looks like you've denied motion the permission to display notifications, for a better user experience please turn on the notification permission by going into the app settings."
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showRationaleDialog(
        title: String, message: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showRationaleDialogForUltraFocus(
        title: String, message: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("OK") { dialog, _ ->
            startLockTask()
            isUltraFocusEnabled = true
            lifecycleScope.launch {
                dataStore.changeUltraFocusFirstTime()
            }
        }
        builder.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //      instances of DAO's
        val taskDao = (application as TaskApp).db.taskDao()
        val pomoDao = (application as TaskApp).db.pomoDao()

        val packageName = this.packageName

        //initialise serviceIntent here:
        serviceIntent = Intent(this@MainActivity, CountdownTimerService::class.java)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // register the broadcast receiver here:
        val intentFilter = IntentFilter("action_update")
        registerReceiver(updateTimeBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)

        //some debugging

        val date = Calendar.getInstance().time
        println("this is date $date")

        val date2 = Date().time.toLong()
        println("this is date2 $date2")

        //hides status bar (obviously)
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


//      fetching all tasks from the db and storing in allTaskList
        lifecycleScope.launch {
            taskDao.fetchTaskByCompletion(false).collect {
                allTaskList = ArrayList(it)
            }
        }

        lifecycleScope.launch {
            isUltraFirst = dataStore.getUltraFocusFirstTime()

        }

        // play/pause the timer
        binding?.playPauseBtn?.setOnClickListener {
            if (isNotificationPermissionAllowed() && isNotificationListenerServiceEnabled(
                    packageName, this
                )
            ) {
                onPlayPause(pomoDao, taskDao)
            } else {
                if (!isNotificationPermissionAllowed()) requestNotificationPermission()

                if (!isNotificationListenerServiceEnabled(
                        packageName, this
                    )
                ) showRationaleDialogForNotificationServiceAccess(
                    "Notification Access",
                    "To enable ultra focus mode it is necessary you enable access to notification service. Please turn on permission in the app settings by clicking on Allow"
                )
            }

        }
        // skipping timer
        binding?.skipBtn?.setOnClickListener {
            onSkip(pomoDao, taskDao)
        }
        //starting full screen mode
        binding?.llTimerTVContainer?.setOnClickListener {
            if (isFullScreen) {
                binding?.llButtonLayout?.visibility = View.VISIBLE
                binding?.llNavbar?.visibility = View.VISIBLE
                binding?.llModeContainer?.visibility = View.VISIBLE
                binding?.tvTimerSecs?.textSize = 200F
                binding?.tvMinuteTimer?.textSize = 200F
                isFullScreen = !isFullScreen
            } else {
                binding?.llButtonLayout?.visibility = View.GONE
                binding?.llNavbar?.visibility = View.INVISIBLE
                binding?.llModeContainer?.visibility = View.GONE
                binding?.tvTimerSecs?.textSize = 250F
                binding?.tvMinuteTimer?.textSize = 250F
                isFullScreen = !isFullScreen
            }
        }

        //starting ultra focus mode
        binding?.llModeContainer?.setOnClickListener {
            if (isNotificationListenerServiceEnabled(packageName, this)) {
                if (isUltraFirst != null && isUltraFirst != false) {
                    showRationaleDialogForUltraFocus(
                        "Allow screen pinning",
                        """To start ultra focus mode please select "Allow"/"Got it" in the next dialog that will be displayed"""
                    )
                } else {
                    startUltraFocusMode()
                }
            }
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


    private fun startUltraFocusMode() {
        if (isUltraFocusEnabled) {
            stopLockTask()
            audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL
            isUltraFocusEnabled = false
        } else {
            startLockTask()
            audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT
            isUltraFocusEnabled = true
        }
    }


    private fun changeModeTextToUltraFocus() {
        val modeText = binding?.tvFocusModeText
        if (modeText?.text == "Focus Mode") modeText.text = "UltraFocus Mode"
        else if (modeText?.text == "UltraFocus Mode") modeText.text = "Focus Mode"
    }

    fun isNotificationListenerServiceEnabled(packageName: String, context: Context): Boolean {
        val contentResolver = context.contentResolver
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    private fun showRationaleDialogForNotificationServiceAccess(
        title: String, message: String
    ) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Allow") { dialog, _ ->
            requestNotificationListenerPermission(this)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun requestNotificationListenerPermission(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) binding = null
        unregisterReceiver(updateTimeBroadcastReceiver)
        serviceIntent?.action = "stop"
        startService(serviceIntent)
        stopService(serviceIntent)
    }
}