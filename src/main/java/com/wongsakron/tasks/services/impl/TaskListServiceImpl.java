package com.wongsakron.tasks.services.impl;

import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.repositories.TaskListRepository;
import com.wongsakron.tasks.services.TaskListService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListServiceImpl(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    } // Constructor injection for TaskListRepository

    @Override
    public List<TaskList> listTaskLists() {
        return taskListRepository.findAll(); // Retrieves all task lists
    }

    @Override
    public TaskList createTaskList(TaskList taskList) {
        if (null != taskList.getId()) {
            throw new IllegalArgumentException("Task list already has an Id!");
        } // Ensures that the task list does not already have an ID, indicating it is a new task list

        if (null == taskList.getTitle() || taskList.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task list title cannot be null or empty!");
        } // Validates that the task list has a non-empty title

        LocalDateTime now = LocalDateTime.now();
        return taskListRepository.save(new TaskList(
                null,
                taskList.getTitle(),
                taskList.getDescription(),
                null,
                now,
                now
        )); // Save the new task list with the current timestamp for created and updated fields
    }
}
