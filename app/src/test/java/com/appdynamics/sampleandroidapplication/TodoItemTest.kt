package com.appdynamics.sampleandroidapplication

import com.appdynamics.sampleandroidapplication.model.TodoItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoItemTest {

    @Test
    fun testTodoItemCreation() {
        val item = TodoItem(
            title = "Buy groceries",
            description = "Milk, Eggs, Bread",
            isCompleted = false
        )

        assertNotNull(item.id)
        assertEquals("Buy groceries", item.title)
        assertEquals("Milk, Eggs, Bread", item.description)
        assertFalse(item.isCompleted)
        assertTrue(item.createdAt > 0)
    }

    @Test
    fun testTodoItemJsonSerialization() {
        val item = TodoItem(
            id = "test-123",
            title = "Code Review",
            description = "Review PR #42",
            isCompleted = true,
            createdAt = 1700000000000L
        )

        val json = item.toJson()

        assertEquals("test-123", json.getString("id"))
        assertEquals("Code Review", json.getString("title"))
        assertEquals("Review PR #42", json.getString("description"))
        assertTrue(json.getBoolean("isCompleted"))
        assertEquals(1700000000000L, json.getLong("createdAt"))
    }

    @Test
    fun testTodoItemJsonDeserialization() {
        val json = JSONObject()
        json.put("id", "custom-id-999")
        json.put("title", "Fix layout bug")
        json.put("description", "RecyclerView padding")
        json.put("isCompleted", true)
        json.put("createdAt", 1700000000000L)

        val item = TodoItem.fromJson(json)

        assertEquals("custom-id-999", item.id)
        assertEquals("Fix layout bug", item.title)
        assertEquals("RecyclerView padding", item.description)
        assertTrue(item.isCompleted)
        assertEquals(1700000000000L, item.createdAt)
    }

    @Test
    fun testTodoItemFormatting() {
        val item = TodoItem(
            title = "Check format",
            createdAt = 1700000000000L
        )

        val formattedDate = item.formattedDate()
        assertTrue(formattedDate.isNotEmpty())
    }
}
