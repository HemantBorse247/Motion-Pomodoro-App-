package com.hemant.pomoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.hemant.pomoapp.databinding.ActivityTaskListBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskListActivity : AppCompatActivity() {

    private var binding: ActivityTaskListBinding? = null
    private var tasksToday: List<TaskEntity>? = null
    private var tasksYesterday: List<TaskEntity>? = null
    private var TasksLast7Days: List<TaskEntity>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideStatusBar()

        // instance of the TaskDao (this consists of all the methods)
        val taskdao = (application as TaskApp).db.taskDao()


        //fetching today's incomplete tasks
        lifecycleScope.launch {
            // today's date
            val date = getDateToday()
            taskdao.fetchTaskByCompletionAndDate(false, date).collect {
                val list = ArrayList(it)
                setupRecycleViewForToday(list, taskdao)
            }
        }

        //fetching yesterday's incomplete tasks
        lifecycleScope.launch {
            //yesterday's date
            val yestDate = getDateYesterday()
            //passing false will fetch incomplete tasks and passing true will fetch completed tasks
            taskdao.fetchNotCompletedTasksAddedYesterday(false, yestDate).collect {
                val tasksYesterday = ArrayList(it)
                setupRecycleViewForYesterday(tasksYesterday, taskdao)
            }
        }

        //fetching incomplete tasks of last 7 days
        lifecycleScope.launch {
            val date2DaysAgo = getDate2DaysAgo()
            val date8DaysAgo = getDate8DaysAgo()

            taskdao.fetchNotCompletedTasksAddedLast7DaysExcludingTodayAndYesterdayNew(
                date8DaysAgo, date2DaysAgo
            ).collect {
                val tasksLast7Days = ArrayList(it)
                setupRecycleViewForLast7Days(tasksLast7Days, taskdao)
            }
        }

        binding?.btnAddTask?.setOnClickListener {
            showInputDialog(taskdao)
        }

        //imp analytics page button onclick listener
        binding?.ivAnalytics?.setOnClickListener {
            finish()
            val intent = Intent(this@TaskListActivity, AnalyticsActivity::class.java)
            startActivity(intent)
        }
        //imp timer page button onclick listener
        binding?.ivTimer?.setOnClickListener {
            finish()
        }
        //imp settings navigation
        binding?.ivSettings?.setOnClickListener {
            finish()
            val intent = Intent(this@TaskListActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecycleViewForYesterday(
        tasksYesterday: ArrayList<TaskEntity>, taskdao: TaskDao
    ) {
        if (tasksYesterday.isNotEmpty()) {
            val taskAdapter = TaskAdapter(tasksYesterday)
            binding?.rvTasksYesterday?.layoutManager = LinearLayoutManager(this)
            binding?.rvTasksYesterday?.adapter = taskAdapter
            binding?.rvTasksYesterday?.visibility = View.VISIBLE

            taskAdapter.onItemClick = {
                showUpdateDialog(
                    taskdao, it.task_id, it.task_desc, it.total_pomos, it.comp_pomos
                )
            }

        } else {

        }
    }

    private fun setupRecycleViewForLast7Days(
        tasksLast7Days: ArrayList<TaskEntity>, taskdao: TaskDao
    ) {

        if (tasksLast7Days.isNotEmpty()) {

            val taskAdapter = TaskAdapter(tasksLast7Days)
            binding?.rvTasksLast7Days?.layoutManager = LinearLayoutManager(this)
            binding?.rvTasksLast7Days?.adapter = taskAdapter
            binding?.rvTasksLast7Days?.visibility = View.VISIBLE

            taskAdapter.onItemClick = {
                showUpdateDialog(
                    taskdao, it.task_id, it.task_desc, it.total_pomos, it.comp_pomos
                )
            }

        } else {
            println("empty last 7 days")
        }

    }


    private fun setupRecycleViewForToday(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            binding?.tvTaskListNoTasksToDisplay?.visibility = View.GONE
            val taskAdapter = TaskAdapter(allTasks)
            binding?.rvTasksToday?.layoutManager = LinearLayoutManager(this)
            binding?.rvTasksToday?.adapter = taskAdapter
            binding?.rvTasksToday?.visibility = View.VISIBLE

//             code for item on click
            taskAdapter.onItemClick = {
                showUpdateDialog(
                    taskdao, it.task_id, it.task_desc, it.total_pomos, it.comp_pomos
                )
            }
        } else {
            binding?.tvTaskListNoTasksToDisplay?.visibility = View.VISIBLE
        }
    }

    private fun showUpdateDialog(
        taskDao: TaskDao, task_id: Int, task_desc: String, tp: String, cp: String
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.add_task_layout, null)
        val etTaskDesc: TextInputEditText = view.findViewById(R.id.etTaskDesc)
        val npTotalPomos: NumberPicker = view.findViewById(R.id.npTotalPomo)
        npTotalPomos.minValue = 0
        npTotalPomos.maxValue = 10
        npTotalPomos.wrapSelectorWheel = true

        AlertDialog.Builder(this, R.style.MyDialogTheme).setTitle("Update Task").setView(view)
            .setPositiveButton("Update") { _, _ ->
                val td = etTaskDesc.text.toString()
                val totalp = npTotalPomos.value

                lifecycleScope.launch {
                    if (td.isEmpty()) {
                        Toast.makeText(
                            this@TaskListActivity,
                            "Task description cannot be empty",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        taskDao.update(
                            TaskEntity(
                                task_id, td, "/$totalp", "${cp.toInt()}", false, getDateToday()
                            )
                        )
                        Toast.makeText(
                            this@TaskListActivity, "Task Updated", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    taskDao.delete(TaskEntity(task_id, "", "", "", false, getDateToday()))
                    Toast.makeText(
                        this@TaskListActivity, "Task Deleted", Toast.LENGTH_LONG
                    ).show()
                    finish();
                    overridePendingTransition(0, 0);
                    val intent = Intent(this@TaskListActivity, TaskListActivity::class.java)
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }.setNegativeButton("Cancel", null).create().show()
    }


    private fun showInputDialog(taskdao: TaskDao) {
        val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val view = LayoutInflater.from(this).inflate(R.layout.add_task_layout, null)
        val etTaskDesc: TextInputEditText = view.findViewById(R.id.etTaskDesc)
        val npTotalPomos: NumberPicker = view.findViewById(R.id.npTotalPomo)
        npTotalPomos.minValue = 0
        npTotalPomos.maxValue = 10
        npTotalPomos.wrapSelectorWheel = true

        dialog.setView(view).setPositiveButton("Add") { _, _ ->
            val td = etTaskDesc.text.toString()
            val tp = npTotalPomos.value
            lifecycleScope.launch {
                if (td.isNotEmpty()) {
                    taskdao.insert(TaskEntity(0, td, "/$tp", "0", false, getDateToday()))
//                    binding?.tvTaskListNoTasksToDisplay?.visibility = View.GONE
                } else {
                    Toast.makeText(
                        this@TaskListActivity, "Task description cannot be empty", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.setNegativeButton("Cancel", null).create().show()
    }


    fun getDateToday(): String {
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(dateTime)
    }

    fun getDateYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Move one day back for yesterday
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Your date format
        return sdf.format(calendar.time)
    }

    fun getDate2DaysAgo(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -2) // Move one day back for yesterday
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Your date format
        return sdf.format(calendar.time)
    }

    fun getDate8DaysAgo(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -8) // Move one day back for yesterday
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Your date format
        return sdf.format(calendar.time)
    }

    fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
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


}