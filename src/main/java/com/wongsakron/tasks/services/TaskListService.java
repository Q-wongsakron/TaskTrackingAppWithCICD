package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.TaskList;

import java.util.List;

public interface TaskListService {
    List<TaskList> listTaskLists(); // Retrieves all task lists
    TaskList createTaskList(TaskList taskList);
}
