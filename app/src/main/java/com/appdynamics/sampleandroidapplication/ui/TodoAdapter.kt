package com.appdynamics.sampleandroidapplication.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appdynamics.sampleandroidapplication.R
import com.appdynamics.sampleandroidapplication.model.TodoItem
import com.google.android.material.card.MaterialCardView

class TodoAdapter(
    private val onToggleCompleted: (TodoItem) -> Unit,
    private val onEditClicked: (TodoItem) -> Unit,
    private val onDeleteClicked: (TodoItem) -> Unit
) : ListAdapter<TodoItem, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_todo)
        private val cbCompleted: CheckBox = itemView.findViewById(R.id.cb_todo_completed)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_todo_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_todo_description)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_todo_date)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_todo_edit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_todo_delete)

        fun bind(item: TodoItem) {
            val context = itemView.context

            // Title
            tvTitle.text = item.title
            if (item.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_completed))
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            // Description
            if (item.description.isNotBlank()) {
                tvDescription.visibility = View.VISIBLE
                tvDescription.text = item.description
                if (item.isCompleted) {
                    tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_completed))
                } else {
                    tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            } else {
                tvDescription.visibility = View.GONE
            }

            // Date
            tvDate.text = item.formattedDate()

            // Completion Checkbox (prevent trigger loop during binding)
            cbCompleted.setOnCheckedChangeListener(null)
            cbCompleted.isChecked = item.isCompleted
            cbCompleted.setOnClickListener {
                onToggleCompleted(item)
            }

            // Action Buttons
            btnEdit.setOnClickListener {
                onEditClicked(item)
            }

            btnDelete.setOnClickListener {
                onDeleteClicked(item)
            }

            // Card click also triggers edit
            cardView.setOnClickListener {
                onEditClicked(item)
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem == newItem
        }
    }
}
