package com.hemant.pomoapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hemant.pomoapp.databinding.TaskItemBinding

class TaskAdapter(
    val allTasks: List<TaskEntity>
//    private val onTaskClickEvent: onTaskClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    //using interface for onclick listener
//    interface onTaskClickListener {
//        fun onTaskClick(item: TaskEntity) {}
//    }

    //    for bind-view holder onclick-listener
    var onItemClick: ((TaskEntity) -> Unit)? = null
    private var isEnable = false
    private var selectedList = mutableListOf<Int>()

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


//        if (position % 2 == 0) {
//            holder.llMainContainer.setBackgroundResource(
////                ContextCompat.getColor(
////                    holder.itemView.context,
////                    R.color.colorLightGray
////                )
//                ContextCompat.getDrawable(
//                    holder.itemView.context, R.drawable.rounded_dark_grey_background
//                )
//            )
//        } else {
//            holder.llMainContainer.setBackgroundResource(
//                ContextCompat.getDrawable(
//                    holder.itemView.context, R.drawable.rounded_light_grey_background
//                )
//            )
//        }

        if (position % 2 == 0) {
            holder.llMainContainer.setBackgroundResource(R.drawable.rounded_light_grey_background)
        } else {
            holder.llMainContainer.setBackgroundResource(R.drawable.rounded_dark_grey_background)
        }


        // using bind view holder onclick-listener also for using interface
        holder.itemView.setOnClickListener {

//            if (selectedList.contains(item.task_id)) {
//
//            } else if (isEnable) {
//
//            }
            // this is for without interface
            onItemClick?.invoke(item)
            //this is for using interface
            //onTaskClickEvent.onTaskClick(item)
        }

//        holder.itemView.setOnLongClickListener {
//
//            selectItem(holder, item, position)
//            true
//        }

        holder.taskDesc.text = item.task_desc
        holder.compPomo.text = item.comp_pomos
        holder.totalPomo.text = item.total_pomos

    }

//    private fun selectItem(holder: TaskAdapter.ViewHolder, item: TaskEntity, position: Int) {
//        isEnable = true
//        selectedList.add(item.task_id)
//        showMenuDelete(true)
//    }

}
