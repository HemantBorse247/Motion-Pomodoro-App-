package com.hemant.pomoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hemant.pomoapp.databinding.ActivityTaskSelectionBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskSelectionActivity : AppCompatActivity() {

    private var binding: ActivityTaskSelectionBinding? = null
    private var tasks: List<TaskEntity>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskSelectionBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideStatusBar()
        val date = getDate()

        val taskdao = (application as TaskApp).db.taskDao()

        lifecycleScope.launch {
            taskdao.fetchTaskByCompletionAndDate(false, date).collect {
                tasks = ArrayList(it)
                val list = ArrayList(it)

                if (list.isNotEmpty()) {
                    setupRecycleView(list, taskdao)
                } else {
                    finish()
                    Toast.makeText(
                        this@TaskSelectionActivity,
                        "Task list is empty. Add tasks to start working on a task.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

    private fun setupRecycleView(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            val taskAdapter = TaskAdapter(allTasks)

//             code for item on click
            taskAdapter.onItemClick = {
                val taskid = it.task_id
                setResult(RESULT_OK, Intent().putExtra("task_id", taskid))
                finish()
            }

            binding?.rvTaskSelection?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskSelection?.adapter = taskAdapter
            binding?.rvTaskSelection?.visibility = View.VISIBLE
        }
    }

    private fun hideStatusBar() {
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

    private fun getDate(): String {
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(dateTime)
    }
}