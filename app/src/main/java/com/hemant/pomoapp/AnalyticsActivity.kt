package com.hemant.pomoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hemant.pomoapp.databinding.ActivityAnalyticsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity() {
    private var binding: ActivityAnalyticsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        MainActivity().hideStatusBar()


        // instance of the TaskDao (this consists of all the methods)
        val taskDao = (application as TaskApp).db.taskDao()
        val pomoDao = (application as TaskApp).db.pomoDao()

        val date = getDate()
        lifecycleScope.launch {
            pomoDao.fetchTasksByDate(date).collect {
                val pomoList = ArrayList(it)
                setupFocusAndPomo(pomoList)
            }
        }

        lifecycleScope.launch {
            taskDao.fetchTaskByCompletionAndDate(false, date).collect {
                val list = ArrayList(it)
                setupRecycleView(list, taskDao)
            }
        }

        lifecycleScope.launch {
            taskDao.fetchTaskByCompletionAndDate(true, date).collect {
                val completedList = ArrayList(it)
                setupCompletedRecycleView(completedList, taskDao)
            }
        }

        //imp navigation bar onclick listeners

        //imp taskList page button onclick listener
        binding?.ivTaskList?.setOnClickListener {
            finish()
            val intent = Intent(this@AnalyticsActivity, TaskListActivity::class.java)
            startActivity(intent)
        }
        binding?.ivTimer?.setOnClickListener {
            finish()
        }

        binding?.ivSettings?.setOnClickListener {
            finish()
            val intent = Intent(this@AnalyticsActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCompletedRecycleView(completedList: ArrayList<TaskEntity>, taskDao: TaskDao) {
        if (completedList.isNotEmpty()) {
            binding?.tvCompletedTasksNoTasksToDisplay?.visibility = View.GONE

            val taskaAdapter = TaskAdapter(completedList)
            binding?.rvCompletedTaskLContainer?.layoutManager = LinearLayoutManager(this)
            binding?.rvCompletedTaskLContainer?.adapter = taskaAdapter
            binding?.rvCompletedTaskLContainer?.visibility = View.VISIBLE
        } else {
            binding?.tvCompletedTasksNoTasksToDisplay?.visibility = View.VISIBLE
        }
    }

    private fun setupRecycleView(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            binding?.tvTodaysTasksNoTasksToDisplay?.visibility = View.GONE
            val taskAdapter = TaskAdapter(allTasks)
            binding?.rvTaskListContainer?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskListContainer?.adapter = taskAdapter
            binding?.rvTaskListContainer?.visibility = View.VISIBLE
        } else {
            binding?.tvTodaysTasksNoTasksToDisplay?.visibility = View.VISIBLE
        }
    }

    private fun setupFocusAndPomo(pomoList: ArrayList<PomodoroEntity>) {
        if (pomoList.isNotEmpty()) {
            binding?.tvTotalPomosNumber?.text = pomoList.size.toString()
            var focusMinutes: Long = 0
            for (i in pomoList) {
                focusMinutes += i.totalTimeInMinutes!!
            }
            var hours: Long = if (focusMinutes / 60 > 0) focusMinutes / 60 else 0
            var minutes: Long = if (focusMinutes - hours * 60 > 0) focusMinutes - hours * 60 else 0

            binding?.tvHours?.text = hours.toString()
            binding?.tvMinutes?.text = "${minutes.toString()} Minutes"
            binding?.tvTotalPomodoroMinutes?.text = "${focusMinutes.toString()} Minutes"
        }

    }

    fun getDate(): String {
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(dateTime)
    }
}