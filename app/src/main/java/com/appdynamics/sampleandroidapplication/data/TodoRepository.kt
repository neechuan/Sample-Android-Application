package com.appdynamics.sampleandroidapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.appdynamics.eumagent.runtime.DontObfuscate
import com.appdynamics.eumagent.runtime.Instrumentation
import com.appdynamics.sampleandroidapplication.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

@DontObfuscate
class TodoRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val client = OkHttpClient()

    companion object {
        private const val TAG = "TodoRepository"
        private const val PREFS_NAME = "todo_app_prefs"
        private const val KEY_TODOS = "saved_todos"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val API_BASE_URL = "https://jsonplaceholder.typicode.com/todos"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    init {
        // Populate demo items on first run
        if (prefs.getBoolean(KEY_FIRST_LAUNCH, true)) {
            val defaultTodos = listOf(
                TodoItem(
                    title = "Welcome to your To-Do App! 🚀",
                    description = "Tap on the checkbox to mark tasks complete or tap edit to modify.",
                    isCompleted = false
                ),
                TodoItem(
                    title = "Try adding a new task",
                    description = "Use the + button at the bottom right to create your own tasks.",
                    isCompleted = false
                ),
                TodoItem(
                    title = "Finished task example",
                    description = "Completed tasks show strike-through text and can be filtered.",
                    isCompleted = true
                )
            )
            saveTodos(defaultTodos)
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
    }

    fun getTodos(): MutableList<TodoItem> {
        val jsonString = prefs.getString(KEY_TODOS, null) ?: return mutableListOf()
        val list = mutableListOf<TodoItem>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(TodoItem.fromJson(obj))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stored todos", e)
        }
        return list
    }

    fun saveTodos(todos: List<TodoItem>) {
        // 1. Start Custom Timer for local persistence duration
        Instrumentation.startTimer("Local Storage Persistence")

        val jsonArray = JSONArray()
        for (item in todos) {
            jsonArray.put(item.toJson())
        }
        val jsonString = jsonArray.toString()
        prefs.edit().putString(KEY_TODOS, jsonString).apply()

        // 2. Stop Custom Timer
        Instrumentation.stopTimer("Local Storage Persistence")

        // 3. Report Custom Metric (Payload size in bytes)
        val payloadSizeBytes = jsonString.toByteArray(Charsets.UTF_8).size.toLong()
        Instrumentation.reportMetric("JSON Storage Payload Size", payloadSizeBytes)
    }

    /**
     * Adds a todo item, performs an asynchronous POST request to the mock API,
     * and persists the item locally.
     */
    suspend fun addTodo(item: TodoItem): List<TodoItem> = withContext(Dispatchers.IO) {
        // 1. Report Info Point
        val tracker = Instrumentation.beginCall(
            "com.appdynamics.sampleandroidapplication.data.TodoRepository",
            "addTodo"
        )
        try {
            val payload = JSONObject().apply {
                put("title", item.title)
                put("completed", item.isCompleted)
                put("userId", 1)
            }
            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(API_BASE_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "addTodo API response code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "addTodo network request failed", e)
        }

        val todos = getTodos()
        todos.add(0, item) // Add at top
        saveTodos(todos)

        // 1. Report Info Point
        try {
            val jsonArray = JSONArray()
            for (item in todos) {
                jsonArray.put(item.toJson())
            }
            prefs.edit().putString(KEY_TODOS, jsonArray.toString()).apply()
            // 2. End tracking on success
            Instrumentation.endCall(tracker)
        } catch (e: Exception) {
            // 3. End tracking with exception details if it fails
            Instrumentation.endCall(tracker, e)
            throw e
        }
        todos
    }

    /**
     * Updates an existing todo item, performs an asynchronous PUT request to the mock API,
     * and updates the item locally.
     */
    suspend fun updateTodo(updatedItem: TodoItem): List<TodoItem> = withContext(Dispatchers.IO) {
    
        try {
            val payload = JSONObject().apply {
                put("title", updatedItem.title)
                put("completed", updatedItem.isCompleted)
                put("userId", 1)
            }
            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$API_BASE_URL/1")
                .put(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "updateTodo API response code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateTodo network request failed", e)
        }

        val todos = getTodos()
        val index = todos.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            todos[index] = updatedItem
            saveTodos(todos)
        }
        todos
    }

    /**
     * Deletes a todo item, performs an asynchronous DELETE request to the mock API,
     * and removes the item locally.
     */
    suspend fun deleteTodo(id: String): List<TodoItem> = withContext(Dispatchers.IO) {

        try {
            val request = Request.Builder()
                .url("$API_BASE_URL/1")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "deleteTodo API response code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteTodo network request failed", e)
        }

        val todos = getTodos()
        todos.removeAll { it.id == id }
        saveTodos(todos)
        todos
    }

    /**
     * Toggles the completion status of a todo item and sends a PATCH request to the mock API.
     */
    suspend fun toggleCompletion(id: String): List<TodoItem> = withContext(Dispatchers.IO) {
        val todos = getTodos()
        val index = todos.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = todos[index]
            val updated = item.copy(isCompleted = !item.isCompleted)
            todos[index] = updated
            saveTodos(todos)

            try {
                val payload = JSONObject().apply {
                    put("completed", updated.isCompleted)
                }
                val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url("$API_BASE_URL/1")
                    .patch(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "toggleCompletion API response code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "toggleCompletion network request failed", e)
            }
        }
        todos
    }
}
