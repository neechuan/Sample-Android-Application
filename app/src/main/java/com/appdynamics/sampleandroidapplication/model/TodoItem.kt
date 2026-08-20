package com.appdynamics.sampleandroidapplication.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put(KEY_ID, id)
        json.put(KEY_TITLE, title)
        json.put(KEY_DESCRIPTION, description)
        json.put(KEY_IS_COMPLETED, isCompleted)
        json.put(KEY_CREATED_AT, createdAt)
        return json
    }

    fun formattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        return formatter.format(Date(createdAt))
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_IS_COMPLETED = "isCompleted"
        private const val KEY_CREATED_AT = "createdAt"

        fun fromJson(json: JSONObject): TodoItem {
            return TodoItem(
                id = json.optString(KEY_ID, UUID.randomUUID().toString()),
                title = json.optString(KEY_TITLE, ""),
                description = json.optString(KEY_DESCRIPTION, ""),
                isCompleted = json.optBoolean(KEY_IS_COMPLETED, false),
                createdAt = json.optLong(KEY_CREATED_AT, System.currentTimeMillis())
            )
        }
    }
}
