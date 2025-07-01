package com.wongsakron.tasks.controllers;

import com.wongsakron.tasks.domain.dto.TaskListDto;
import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.mappers.TaskListMapper;
import com.wongsakron.tasks.services.TaskListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/task-lists")
public class TaskListController {

    private final TaskListService taskListService;
    private final TaskListMapper taskListMapper;

    public TaskListController(TaskListService taskListService, TaskListMapper taskListMapper) {
        this.taskListService = taskListService;
        this.taskListMapper = taskListMapper;
    }

    @GetMapping
    public List<TaskListDto> listTaskLists() {
        // get task lists and convert to DTOs
        return taskListService.listTaskLists()
                .stream() // Stream the list of TaskList entities
                .map(taskListMapper::toDto) // Convert TaskList entities to TaskListDto
                .toList(); // Collect the converted DTOs into a list
    }

    @PostMapping
    public TaskListDto createTaskList(@RequestBody TaskListDto taskListDto) {
        TaskList createdTaskList = taskListService.createTaskList(
                taskListMapper.fromDto(taskListDto)
        ); // Convert TaskListDto to TaskList entity and create a new task list
        return taskListMapper.toDto(createdTaskList); // Convert the created TaskList entity back to TaskListDto for the response
    }

    // dto is a Data Transfer Object, which is used to transfer data between layers of the application.
    // between the controller and the service layer, we use DTOs to avoid exposing the internal entity structure directly.
}
