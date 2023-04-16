package com.hemant.pomoapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hemant.pomoapp.databinding.TaskItemBinding

class TaskAdapter(
    val allTasks: List<TaskEntity>,
//    private val onTaskClickEvent: onTaskClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {


    //using interface for onclick listener
//    interface onTaskClickListener {
//        fun onTaskClick(item: TaskEntity) {}
//    }


    //    for bind-view holder onclick-listener
    var onItemClick: ((TaskEntity) -> Unit)? = null

    class ViewHolder(binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMainContainer = binding.llMainContainer
        val taskDesc = binding.tvTaskDesc
        val totalPomo = binding.tvTotalPomo
        val compPomo = binding.tvCompPomos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return allTasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = allTasks[position]


        // using bind view holder onclick-listener also for using interface
        holder.itemView.setOnClickListener {

            // this is for without interface
            onItemClick?.invoke(item)
            //this is for using interface
            //onTaskClickEvent.onTaskClick(item)
        }

        holder.taskDesc.text = item.task_desc
        holder.compPomo.text = item.comp_pomos
        holder.totalPomo.text = item.total_pomos

    }

}
