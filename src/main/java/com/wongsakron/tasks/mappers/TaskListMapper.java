package com.wongsakron.tasks.mappers;

import com.wongsakron.tasks.domain.dto.TaskListDto;
import com.wongsakron.tasks.domain.entities.TaskList;

public interface TaskListMapper {

    TaskList fromDto(TaskListDto taskListDto);

    TaskListDto toDto(TaskList taskList);
}

// This interface defines methods for mapping between TaskListDto and TaskList entities.
