package com.wongsakron.tasks.repositories;

import com.wongsakron.tasks.domain.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByTaskListId(UUID taskListId); // Retrieves all tasks associated with a specific task list by its ID.
    Optional<Task> findByTaskListIdAndId(UUID taskListId, UUID id); // Retrieves a specific task by its ID within a given task list.
    void deleteByTaskListIdAndId(UUID taskListId, UUID id); // Deletes a specific task by its ID within a given task list.
}
// This interface extends JpaRepository to provide CRUD operations for Task entities.
