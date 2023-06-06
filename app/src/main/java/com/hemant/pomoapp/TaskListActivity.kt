package com.hemant.pomoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
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
import java.util.*

class TaskListActivity : AppCompatActivity() {

    private var mainMenu: Menu? = null
    private var binding: ActivityTaskListBinding? = null
    private var tasks: List<TaskEntity>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideStatusBar()

//        MainActivity().hideStatusBar()

        // instance of the TaskDao (this consists of all the methods)
        val taskdao = (application as TaskApp).db.taskDao()
        val date = getDate()
        lifecycleScope.launch {
            taskdao.fetchTaskByCompletionAndDate(false, date).collect {
//                Log.d("some task", "$it")
                tasks = ArrayList(it)
                val list = ArrayList(it)
                setupRecycleView(list, taskdao)
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

        binding?.ivSettings?.setOnClickListener {
            finish()
            val intent = Intent(this@TaskListActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
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
                    taskdao.insert(TaskEntity(0, td, "/$tp", "0", false, getDate()))
//                    binding?.tvTaskListNoTasksToDisplay?.visibility = View.GONE
                } else {
                    Toast.makeText(
                        this@TaskListActivity, "Task description cannot be empty", Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.setNegativeButton("Cancel", null).create().show()
    }

    private fun setupRecycleView(allTasks: ArrayList<TaskEntity>, taskdao: TaskDao) {
        if (allTasks.isNotEmpty()) {
            binding?.tvTaskListNoTasksToDisplay?.visibility = View.GONE
            val taskAdapter = TaskAdapter(allTasks)
            binding?.rvTaskListIntent?.layoutManager = LinearLayoutManager(this)
            binding?.rvTaskListIntent?.adapter = taskAdapter
            binding?.rvTaskListIntent?.visibility = View.VISIBLE

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
                                task_id, td, "/$totalp", "${cp.toInt()}", false, getDate()
                            )
                        )
                        Toast.makeText(
                            this@TaskListActivity, "Task Updated", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    taskDao.delete(TaskEntity(task_id, "", "", "", false, getDate()))
                    Toast.makeText(
                        this@TaskListActivity, "Task Deleted", Toast.LENGTH_LONG
                    ).show()
                }
            }.setNegativeButton("Cancel", null).create().show()
    }


    fun getDate(): String {
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(dateTime)
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


//    fun showDeleteMenu(show: Boolean) {
//        mainMenu?.findItem(R.id.itemDeleteIcon)?.isVisible = show
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        mainMenu = menu
//        menuInflater.inflate(R.menu.custom_delete_action_bar, mainMenu)
//        showDeleteMenu(false)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.itemDeleteIcon -> {
//                deleteItems()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun deleteItems() {
//
//    }
}