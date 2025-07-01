package com.wongsakron.tasks.repositories;

import com.wongsakron.tasks.domain.entities.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, UUID> {

}
// This interface extends JpaRepository to provide CRUD operations for TaskList entities.
