package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskService {
    List<Task> listTasks(UUID taskListId); // Retrieves all tasks for a specific task list
    Task createTask(UUID taskListId, Task task); // Creates a new task in a specific task list
    Optional<Task> getTask(UUID taskListId, UUID taskId); // Retrieves a specific task by its ID
    Task updateTask(UUID taskListId, UUID taskId, Task task); // Updates an existing task in a specific task list
}
