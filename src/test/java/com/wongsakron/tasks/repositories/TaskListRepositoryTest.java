package com.wongsakron.tasks.repositories;

import com.wongsakron.tasks.domain.entities.TaskList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class TaskListRepositoryTest {

    @Autowired
    TaskListRepository listRepo;

    @Test
    void save_find_delete_shouldWork() {
        TaskList list = new TaskList(null, "L1", "Desc", null, LocalDateTime.now(), LocalDateTime.now());
        TaskList saved = listRepo.save(list);

        assertThat(saved.getId()).isNotNull();

        var found = listRepo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("L1");

        listRepo.deleteById(saved.getId());
        assertThat(listRepo.findById(saved.getId())).isNotPresent();
    }
}
