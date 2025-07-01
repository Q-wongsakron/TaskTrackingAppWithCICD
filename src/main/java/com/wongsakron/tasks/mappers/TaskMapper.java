package com.wongsakron.tasks.mappers;

import com.wongsakron.tasks.domain.dto.TaskDto;
import com.wongsakron.tasks.domain.entities.Task;

public interface TaskMapper {

    Task fromDto(TaskDto taskDto);
    TaskDto toDto(Task task);
}

// This interface defines methods for mapping between TaskDto and Task entities.
