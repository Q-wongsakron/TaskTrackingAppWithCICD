package com.wongsakron.tasks.services.impl;


import com.wongsakron.tasks.domain.entities.Task;
import com.wongsakron.tasks.repositories.TaskRepository;
import com.wongsakron.tasks.services.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository; // Assuming a TaskRepository exists for data access

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> listTasks(UUID taskListId) {
        return taskRepository.findByTaskListId(taskListId); // Retrieves all tasks for a specific task list
    }
}
