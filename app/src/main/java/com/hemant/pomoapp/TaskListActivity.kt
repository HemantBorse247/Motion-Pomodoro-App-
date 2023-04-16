package com.hemant.pomoapp

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.hemant.pomoapp.databinding.ActivityTaskListBinding
import kotlinx.coroutines.launch

class TaskListActivity : AppCompatActivity() {
    private var binding: ActivityTaskListBinding? = null
    private var tasks: List<TaskEntity>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // instance of the TaskDao (this consists of all the methods)
        val taskdao = (application as TaskApp).db.taskDao()

        lifecycleScope.launch {
            taskdao.fetchAllTasks().collect {
//                Log.d("some task", "$it")
                tasks = ArrayList(it)
                val list = ArrayList(it)
                setupRecycleView(list, taskdao)
            }
        }

        binding?.btnAddTask?.setOnClickListener {
            showInputDialog(taskdao)
        }
    }


    private fun showInputDialog(taskdao: TaskDao) {
        val dialog = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_task_layout, null)
        val etTaskDesc: TextInputEditText = view.findViewById(R.id.etTaskDesc)
        val npTotalPomos: NumberPicker = view.findViewById(R.id.npTotalPomo)
        npTotalPomos.minValue = 0
        npTotalPomos.maxValue = 10
        npTotalPomos.wrapSelectorWheel = true


        dialog.setMessage("Add Task").setView(view)
            .setPositiveButton("Add", DialogInterface.OnClickListener { dialogInterface, i ->
                val td = etTaskDesc.text.toString()
                val tp = npTotalPomos.value
                lifecycleScope.launch {
                    if (td.isNotEmpty()) {
                        taskdao.insert(TaskEntity(0, td, tp.toString(), "0/"))
                    } else {
                        Toast.makeText(
                            this@TaskListActivity,
                            "Task description cannot be empty",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }).setNegativeButton("Cancel", null).create().show()
    }

    private fun setupRecycleView(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            val taskAdapter = TaskAdapter(allTasks)

//             code for item on click
//            taskAdapter.onItemClick = {
//                Toast.makeText(this@TaskListActivity, "${it.task_id}", Toast.LENGTH_LONG).show()
//            }

            binding?.rvTaskListIntent?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskListIntent?.adapter = taskAdapter
            binding?.rvTaskListIntent?.visibility = View.VISIBLE
        }
    }
}