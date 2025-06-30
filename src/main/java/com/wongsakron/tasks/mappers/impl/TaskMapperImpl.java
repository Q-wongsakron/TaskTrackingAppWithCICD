package com.wongsakron.tasks.mappers.impl;

import com.wongsakron.tasks.domain.dto.TaskDto;
import com.wongsakron.tasks.domain.entities.Task;
import com.wongsakron.tasks.mappers.TaskMapper;
import org.springframework.stereotype.Component;

@Component // This annotation registers the class as a Spring bean, allowing it to be injected where needed.
public class TaskMapperImpl implements TaskMapper {
    @Override
    public Task fromDto(TaskDto taskDto) {
        return new Task(
                taskDto.id(),
                taskDto.title(),
                taskDto.description(),
                taskDto.dueDate(),
                taskDto.status(),
                taskDto.priority(),
                null,
                null,
                null
        );
    }

    @Override
    public TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.getStatus()
        );
    }
}

// This implementation of the TaskMapper interface provides methods to convert between TaskDto and Task entities.