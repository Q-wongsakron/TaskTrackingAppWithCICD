package com.wongsakron.tasks.domain.dto;

import com.wongsakron.tasks.domain.entities.TaskPriority;
import com.wongsakron.tasks.domain.entities.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(
        UUID id,

        @NotBlank(message = "title must not be blank")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        String description,

        @FutureOrPresent(message = "dueDate must be in the present or future")
        LocalDateTime dueDate,

        TaskPriority priority,
        TaskStatus status
) {
}
// Data Transfer Object (DTO) for Task.

