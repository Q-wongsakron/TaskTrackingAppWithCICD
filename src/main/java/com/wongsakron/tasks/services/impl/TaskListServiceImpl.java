package com.wongsakron.tasks.services.impl;

import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.repositories.TaskListRepository;
import com.wongsakron.tasks.services.TaskListService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<TaskList> getTaskList(UUID id) {
        return taskListRepository.findById(id); // Retrieves a task list by its ID, returning an Optional
    }

    @Override
    public TaskList updateTaskList(UUID taskListId, TaskList taskList) {

        if(null == taskList.getId()) {
            throw new IllegalArgumentException("Task list must have an Id to be updated!");
        } // Ensures that the task list being updated has an ID

        if (!Objects.equals(taskList.getId(), taskListId)) {
            throw new IllegalArgumentException("Attempt to change task list ID, this is not permitted!");
        } // Validates that the ID of the task list being updated matches the provided taskListId

        TaskList existingTaskList = taskListRepository.findById(taskListId).orElseThrow(() ->
                new IllegalArgumentException("Task list not found!"));
        // Ensures the task list exists before updating

        existingTaskList.setTitle(taskList.getTitle());
        existingTaskList.setDescription(taskList.getDescription());
        existingTaskList.setUpdated(LocalDateTime.now());
        return taskListRepository.save(existingTaskList); // Updates the existing task list with new values and saves it
    }

    @Override
    public void deleteTaskList(UUID id) {
        Optional<TaskList> existingTaskList = taskListRepository.findById(id);
        // JPA จัดการให้เองได้ เเต่เขียนเองก็ดี
        if(existingTaskList.isEmpty()){
            throw new IllegalArgumentException("Task list not found!"); // Ensures the task list exists before attempting to delete
        } else {
            taskListRepository.deleteById(id); // Deletes the task list by its ID
        }
    }
}
