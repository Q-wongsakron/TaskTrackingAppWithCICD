package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.TaskList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskListService {
    List<TaskList> listTaskLists(); // Retrieves all task lists
    TaskList createTaskList(TaskList taskList);
    Optional<TaskList> getTaskList(UUID id);
    TaskList updateTaskList(UUID id, TaskList taskList); // Updates an existing task list by its ID
    void deleteTaskList(UUID id); // Deletes a task list by its ID
}
