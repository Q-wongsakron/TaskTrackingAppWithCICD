package com.wongsakron.tasks.controllers;

import com.wongsakron.tasks.domain.dto.TaskDto;
import com.wongsakron.tasks.mappers.TaskMapper;
import com.wongsakron.tasks.services.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/task-lists/{task_list_id}/tasks")
public class TasksController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TasksController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public List<TaskDto> listTasks(@PathVariable("task_list_id") UUID taskListId) {
        return taskService.listTasks(taskListId)
                .stream() // Streams the list of Task entities
                .map(taskMapper::toDto)  // Converts Task entities to TaskDto
                .toList(); // Retrieves all tasks for a specific task list and converts them to DTOs
    }
}
