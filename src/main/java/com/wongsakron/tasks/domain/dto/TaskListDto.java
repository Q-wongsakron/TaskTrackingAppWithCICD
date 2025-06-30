package com.wongsakron.tasks.domain.dto;

import java.util.List;
import java.util.UUID;

public record TaskListDto(
        UUID id,
        String title,
        String description,
        Integer count, // Number of tasks
        Double progress, // Progress percentage
        List<TaskDto> tasks
) {
}
