package com.wongsakron.tasks.mappers.impl;

import com.wongsakron.tasks.domain.dto.TaskListDto;
import com.wongsakron.tasks.domain.entities.Task;
import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.domain.entities.TaskStatus;
import com.wongsakron.tasks.mappers.TaskListMapper;
import com.wongsakron.tasks.mappers.TaskMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TaskListMapperImpl implements TaskListMapper {

    private final TaskMapper taskMapper;

    public TaskListMapperImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskList fromDto(TaskListDto taskListDto) {
        return new TaskList(
                taskListDto.id(),
                taskListDto.title(),
                taskListDto.description(),
                Optional.ofNullable(taskListDto.tasks()
                ).map(tasks -> tasks.stream().map(taskMapper::fromDto)
                        .toList()).orElse(null), // Convert List<TaskDto> to List<Task>
                null,
                null
        );
    }

    @Override
    public TaskListDto toDto(TaskList taskList) {
        return new TaskListDto(
                taskList.getId(),
                taskList.getTitle(),
                taskList.getDescription(),
                Optional.ofNullable(taskList.getTasks())
                        .map(List::size).orElse(0),
                calculateTaskListProgress(taskList.getTasks()), // Calculate the progress of the task list
                Optional.ofNullable(taskList.getTasks()) // Convert List<Task> to List<TaskDto>
                        .map(tasks -> tasks.stream().map(taskMapper::toDto).toList())
                        .orElse(null)
        );
    }

    // This method calculates the progress of the task list based on the number of closed tasks.
    private  Double calculateTaskListProgress(List<Task> tasks) {
        if (null == tasks) {
            return null;
        } // If there are no tasks, return null

        long closedTaskCount = tasks.stream().filter(task ->
            TaskStatus.CLOSED == task.getStatus()
        ).count(); // Count the number of tasks with CLOSED status

        return (double) closedTaskCount / tasks.size(); // Calculate the progress as a percentage of closed tasks over total tasks
    }
}
// This implementation of the TaskListMapper interface provides methods to convert between TaskListDto and TaskList entities,