package com.hemant.pomoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hemant.pomoapp.databinding.ActivityTaskSelectionBinding
import kotlinx.coroutines.launch

class TaskSelectionActivity : AppCompatActivity() {

    private var binding: ActivityTaskSelectionBinding? = null
    private var tasks: List<TaskEntity>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskSelectionBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val taskdao = (application as TaskApp).db.taskDao()

        lifecycleScope.launch {
            taskdao.fetchAllTasks().collect {
//                Log.d("some task", "$it")
                tasks = ArrayList(it)
                val list = ArrayList(it)
                setupRecycleView(list, taskdao)
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
//                Toast.makeText(this@TaskSelectionActivity, "${it.task_id}", Toast.LENGTH_SHORT)
//                    .show()
            }

            binding?.rvTaskSelection?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskSelection?.adapter = taskAdapter
            binding?.rvTaskSelection?.visibility = View.VISIBLE
        }
    }
}