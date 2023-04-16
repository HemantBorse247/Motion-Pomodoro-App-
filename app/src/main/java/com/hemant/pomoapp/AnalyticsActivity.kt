package com.hemant.pomoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hemant.pomoapp.databinding.ActivityAnalyticsBinding
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {
    private var binding: ActivityAnalyticsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        //creating instance of database
//        val db = Room.databaseBuilder(
//            applicationContext,
//            TaskDatabase::class.java, "database-name"
//        ).build()

        // instance of the TaskDao (this consists of all the methods)
        val taskdao = (application as TaskApp).db.taskDao()


        binding?.ivTaskList?.setOnClickListener {
            val intent = Intent(this@AnalyticsActivity, TaskListActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            taskdao.fetchAllTasks().collect {
//                Log.d("some task", "$it")
                val list = ArrayList(it)
                setupRecycleView(list, taskdao)
            }
        }
    }

//    private fun addTask(taskdao: TaskDao) {
//        lifecycleScope.launch {
//            taskdao.insert(TaskEntity(0, "heyhey", 3, 5))
//        }
//    }

    private fun setupRecycleView(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            val taskAdapter = TaskAdapter(allTasks)
            binding?.rvTaskListContainer?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskListContainer?.adapter = taskAdapter
            binding?.rvTaskListContainer?.visibility = View.VISIBLE
        }
    }

}