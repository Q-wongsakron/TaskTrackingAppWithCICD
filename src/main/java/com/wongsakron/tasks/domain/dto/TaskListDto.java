package com.wongsakron.tasks.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record TaskListDto(
        UUID id,

        @NotBlank(message = "title must not be blank")
        @Size(max = 100, message = "title must be at most 100 characters")
        String title,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,
        Integer count, // Number of tasks
        Double progress, // Progress percentage
        List<TaskDto> tasks
) {
}

// Data Transfer Object (DTO) for TaskList.
