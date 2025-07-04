package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<Task> listTasks(UUID taskListId); // Retrieves all tasks for a specific task list
}
