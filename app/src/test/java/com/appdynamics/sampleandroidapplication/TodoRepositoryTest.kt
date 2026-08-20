package com.appdynamics.sampleandroidapplication

import com.appdynamics.sampleandroidapplication.model.TodoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoRepositoryTest {

    @Test
    fun testTodoListOperations() {
        val list = mutableListOf<TodoItem>()

        // 1. Add
        val item1 = TodoItem(id = "1", title = "Task 1", isCompleted = false)
        val item2 = TodoItem(id = "2", title = "Task 2", isCompleted = false)
        list.add(0, item1)
        list.add(0, item2)

        assertEquals(2, list.size)
        assertEquals("Task 2", list[0].title)
        assertEquals("Task 1", list[1].title)

        // 2. Toggle completion
        val index = list.indexOfFirst { it.id == "1" }
        list[index] = list[index].copy(isCompleted = !list[index].isCompleted)

        assertTrue(list.first { it.id == "1" }.isCompleted)
        assertFalse(list.first { it.id == "2" }.isCompleted)

        // 3. Update title/description
        val updateIndex = list.indexOfFirst { it.id == "2" }
        list[updateIndex] = list[updateIndex].copy(title = "Updated Task 2", description = "New Desc")

        assertEquals("Updated Task 2", list.first { it.id == "2" }.title)
        assertEquals("New Desc", list.first { it.id == "2" }.description)

        // 4. Delete
        list.removeAll { it.id == "1" }
        assertEquals(1, list.size)
        assertEquals("2", list[0].id)
    }

    @Test
    fun testTodoFilterLogic() {
        val todos = listOf(
            TodoItem(id = "1", title = "Task 1", isCompleted = false),
            TodoItem(id = "2", title = "Task 2", isCompleted = true),
            TodoItem(id = "3", title = "Task 3", isCompleted = false),
            TodoItem(id = "4", title = "Task 4", isCompleted = true)
        )

        val activeList = todos.filter { !it.isCompleted }
        val completedList = todos.filter { it.isCompleted }

        assertEquals(2, activeList.size)
        assertEquals(2, completedList.size)
        assertEquals("Task 1", activeList[0].title)
        assertEquals("Task 3", activeList[1].title)
        assertEquals("Task 2", completedList[0].title)
        assertEquals("Task 4", completedList[1].title)
    }
}
