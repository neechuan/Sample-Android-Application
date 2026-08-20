package com.appdynamics.sampleandroidapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdynamics.sampleandroidapplication.data.TodoRepository
import com.appdynamics.sampleandroidapplication.model.TodoItem
import com.appdynamics.sampleandroidapplication.ui.TodoAdapter
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: TodoRepository
    private lateinit var adapter: TodoAdapter

    private lateinit var rvTodos: RecyclerView
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var tvStats: TextView
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var fabAddTodo: FloatingActionButton

    private enum class FilterType {
        ALL, ACTIVE, COMPLETED
    }

    private var currentFilter: FilterType = FilterType.ALL
    private var allTodos: List<TodoItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = TodoRepository(this)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadTodos()
    }

    private fun initViews() {
        rvTodos = findViewById(R.id.rv_todos)
        progressBarLoading = findViewById(R.id.progress_bar_loading)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle)
        tvStats = findViewById(R.id.tv_stats)
        chipGroupFilter = findViewById(R.id.chip_group_filter)
        fabAddTodo = findViewById(R.id.fab_add_todo)
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onToggleCompleted = { item ->
                lifecycleScope.launch {
                    setLoading(true)
                    allTodos = repository.toggleCompletion(item.id)
                    setLoading(false)
                    render()
                }
            },
            onEditClicked = { item ->
                showAddOrEditDialog(item)
            },
            onDeleteClicked = { item ->
                showDeleteConfirmationDialog(item)
            }
        )
        rvTodos.layoutManager = LinearLayoutManager(this)
        rvTodos.adapter = adapter
    }

    private fun setupListeners() {
        fabAddTodo.setOnClickListener {
            showAddOrEditDialog(null)
        }

        chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chip_filter_active -> FilterType.ACTIVE
                R.id.chip_filter_completed -> FilterType.COMPLETED
                else -> FilterType.ALL
            }
            render()
        }
    }

    private fun loadTodos() {
        allTodos = repository.getTodos()
        render()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun render() {
        val totalCount = allTodos.size
        val completedCount = allTodos.count { it.isCompleted }

        // Update stats
        tvStats.text = getString(R.string.tasks_stats_format, completedCount, totalCount)

        // Filter list
        val filteredList = when (currentFilter) {
            FilterType.ALL -> allTodos
            FilterType.ACTIVE -> allTodos.filter { !it.isCompleted }
            FilterType.COMPLETED -> allTodos.filter { it.isCompleted }
        }

        adapter.submitList(filteredList)

        // Empty state handling
        if (filteredList.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvTodos.visibility = View.GONE

            if (allTodos.isEmpty()) {
                tvEmptyTitle.text = getString(R.string.empty_tasks_title)
                tvEmptySubtitle.text = getString(R.string.empty_tasks_subtitle)
            } else {
                tvEmptyTitle.text = getString(R.string.empty_tasks_title)
                tvEmptySubtitle.text = getString(R.string.empty_tasks_filtered_subtitle)
            }
        } else {
            layoutEmptyState.visibility = View.GONE
            rvTodos.visibility = View.VISIBLE
        }
    }

    private fun showAddOrEditDialog(itemToEdit: TodoItem?) {
        val isEditing = itemToEdit != null
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_todo, null)

        val tilTitle = dialogView.findViewById<TextInputLayout>(R.id.til_task_title)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_task_title)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.et_task_desc)

        if (itemToEdit != null) {
            etTitle.setText(itemToEdit.title)
            etDesc.setText(itemToEdit.description)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (isEditing) R.string.edit_task else R.string.add_task)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null) // Override later to prevent auto-dismiss on validation failure
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = etTitle.text?.toString()?.trim().orEmpty()
                val desc = etDesc.text?.toString()?.trim().orEmpty()

                if (title.isEmpty()) {
                    tilTitle.error = getString(R.string.title_required)
                    return@setOnClickListener
                }

                tilTitle.error = null
                dialog.dismiss()

                lifecycleScope.launch {
                    setLoading(true)
                    if (itemToEdit != null) {
                        val updated = itemToEdit.copy(title = title, description = desc)
                        allTodos = repository.updateTodo(updated)
                        Snackbar.make(rvTodos, R.string.task_updated, Snackbar.LENGTH_SHORT).show()
                    } else {
                        val newTodo = TodoItem(title = title, description = desc)
                        allTodos = repository.addTodo(newTodo)
                        Snackbar.make(rvTodos, R.string.task_added, Snackbar.LENGTH_SHORT).show()
                    }
                    setLoading(false)
                    render()
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(item: TodoItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, item.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                val deletedItem = item
                val previousList = allTodos.toList()

                lifecycleScope.launch {
                    setLoading(true)
                    allTodos = repository.deleteTodo(deletedItem.id)
                    setLoading(false)
                    render()

                    Snackbar.make(rvTodos, R.string.task_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            repository.saveTodos(previousList)
                            allTodos = previousList
                            render()
                        }
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}