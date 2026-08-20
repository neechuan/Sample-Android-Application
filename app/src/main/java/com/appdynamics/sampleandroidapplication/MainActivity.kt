package com.appdynamics.sampleandroidapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdynamics.eumagent.runtime.Instrumentation
import com.appdynamics.sampleandroidapplication.data.TodoRepository
import com.appdynamics.sampleandroidapplication.model.TodoItem
import com.appdynamics.sampleandroidapplication.ui.TodoAdapter
import com.google.android.material.button.MaterialButton
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
    private lateinit var btnSimulateAnr: MaterialButton
    private lateinit var btnSimulateCrash: MaterialButton
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
        btnSimulateAnr = findViewById(R.id.btn_simulate_anr)
        btnSimulateCrash = findViewById(R.id.btn_simulate_crash)
        fabAddTodo = findViewById(R.id.fab_add_todo)
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onToggleCompleted = { item ->
                Instrumentation.leaveBreadcrumb("Toggled task '${item.title}' completed=${!item.isCompleted}")
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
        btnSimulateAnr.setOnClickListener {
            Instrumentation.leaveBreadcrumb("User initiated ANR simulation")
            Toast.makeText(this, R.string.simulating_anr_toast, Toast.LENGTH_SHORT).show()
            // Delay slightly so the Toast renders before freezing the Main UI thread
            findViewById<View>(android.R.id.content).postDelayed({
                // Block the Main UI Thread for 10 seconds (> 5s Android ANR threshold)
                // When an input/touch event is dispatched while the thread is blocked, Android OS raises an ANR
                try {
                    Thread.sleep(10000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, 200)
        }

        btnSimulateCrash.setOnClickListener {
            Instrumentation.leaveBreadcrumb("User initiated fatal Crash simulation")
            throw RuntimeException("Simulated Real App Crash: Fatal uncaught exception triggered by user tapping Crash button")
        }

        fabAddTodo.setOnClickListener {
            showAddOrEditDialog(null)
        }

        chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chip_filter_active -> FilterType.ACTIVE
                R.id.chip_filter_completed -> FilterType.COMPLETED
                else -> FilterType.ALL
            }
            Instrumentation.leaveBreadcrumb("Filter changed to ${currentFilter.name}")
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
        val actionLabel = if (isEditing) "Edit" else "Add"
        Instrumentation.leaveBreadcrumb("Opened $actionLabel Task Dialog")

        // Start Custom Timer for new task creation flow
        if (!isEditing) {
            Instrumentation.startTimer("Task Creation Flow")
        }

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
            .setNegativeButton(R.string.cancel) { _, _ ->
                Instrumentation.leaveBreadcrumb("Cancelled $actionLabel Task Dialog")
            }
            .setOnCancelListener {
                Instrumentation.leaveBreadcrumb("Dismissed $actionLabel Task Dialog without saving")
            }
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
                        Instrumentation.leaveBreadcrumb("Updated task: '$title' (ID: ${itemToEdit.id})")
                        Snackbar.make(rvTodos, R.string.task_updated, Snackbar.LENGTH_SHORT).show()
                    } else {
                        // Stop Custom Timer & Report Metric for character length
                        Instrumentation.stopTimer("Task Creation Flow")
                        Instrumentation.reportMetric("Task Title Character Length", title.length.toLong())
                        Instrumentation.leaveBreadcrumb("Created new task: '$title'")

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
        Instrumentation.leaveBreadcrumb("Opened Delete Confirmation Dialog for task: '${item.title}'")
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, item.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                val deletedItem = item
                val previousList = allTodos.toList()

                Instrumentation.leaveBreadcrumb("Confirmed deletion for task: '${deletedItem.title}'")

                lifecycleScope.launch {
                    setLoading(true)
                    allTodos = repository.deleteTodo(deletedItem.id)
                    setLoading(false)
                    render()

                    Snackbar.make(rvTodos, R.string.task_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            Instrumentation.leaveBreadcrumb("Undo deletion for task: '${deletedItem.title}'")
                            repository.saveTodos(previousList)
                            allTodos = previousList
                            render()
                        }
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                Instrumentation.leaveBreadcrumb("Cancelled task deletion dialog")
            }
            .show()
    }
}