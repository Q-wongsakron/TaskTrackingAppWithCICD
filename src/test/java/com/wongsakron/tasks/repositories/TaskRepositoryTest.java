package com.wongsakron.tasks.repositories;

import com.wongsakron.tasks.domain.entities.Task;
import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.domain.entities.TaskPriority;
import com.wongsakron.tasks.domain.entities.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class TaskRepositoryTest {

    @Autowired
    TaskRepository taskRepo;

    @Autowired
    TaskListRepository listRepo;

    @Test
    void findTaskByTaskListId_and_findTaskByTaskListIdAndId_shouldWork() {
        TaskList list = new TaskList(
                null,
                "L",
                "D",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        list = listRepo.save(list);

        Task t1 = new Task(
                null,
                "A",
                "a",
                LocalDateTime.now(),
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                list,
                LocalDateTime.now(),
                LocalDateTime.now()

        );

        Task t2 = new Task(
                null,
                "B",
                "b",
                LocalDateTime.now(),
                TaskStatus.OPEN,
                TaskPriority.LOW,
                list,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        t1 = taskRepo.save(t1);
        t2 = taskRepo.save(t2);

        List<Task> onlyList = taskRepo.findByTaskListId(list.getId());
        assertThat(onlyList).hasSize(2)
                .extracting(Task::getTitle)
                .containsExactly("A", "B");

        var byBoth = taskRepo.findByTaskListIdAndId(list.getId(), t1.getId());
        assertThat(byBoth).isPresent();
        assertThat(byBoth.get().getTitle()).isEqualTo("A");
    }

    @Test
    void deleteTaskByTaskListIdAndId_shouldDeleteOnlyOne() {
        TaskList list = new TaskList(
                null,
                "L",
                "D",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        list = listRepo.save(list);

        Task t1 = new Task(
                null,
                "A",
                "a",
                LocalDateTime.now(),
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                list,
                LocalDateTime.now(),
                LocalDateTime.now()

        );

        Task t2 = new Task(
                null,
                "B",
                "b",
                LocalDateTime.now(),
                TaskStatus.OPEN,
                TaskPriority.LOW,
                list,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        t1 = taskRepo.save(t1);
        t2 = taskRepo.save(t2);

        taskRepo.deleteByTaskListIdAndId(list.getId(), t1.getId());

        assertThat(taskRepo.findByTaskListId(list.getId())).hasSize(1)
                .extracting(Task::getTitle)
                .containsExactly("B");
    }
}
