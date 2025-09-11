package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.repositories.TaskListRepository;
import com.wongsakron.tasks.services.impl.TaskListServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskListServiceImplTest {

    @Mock
    TaskListRepository listRepo;

    @InjectMocks
    TaskListServiceImpl svc;

    private TaskList sample;

    @BeforeEach
    void setup() {
        sample = new TaskList(
                null,
                "My list",
                "Desc",
                new ArrayList<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // listTaskList()
    @Test
    void listTaskList_shouldReturnAll_andCallRepoOnce() {
        // Arrange
        when(listRepo.findAll()).thenReturn(List.of(sample));

        // Act
        var result = svc.listTaskLists();

        // Assert
        assertThat(result).hasSize(1)
                .extracting(TaskList::getTitle)
                .containsExactly("My list");

        verify(listRepo, times(1)).findAll();
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void listTaskList_whenEmpty_shouldReturnEmpty() {
        when(listRepo.findAll()).thenReturn(Collections.emptyList());

        var result = svc.listTaskLists();

        assertThat(result).isEmpty();

        verify(listRepo).findAll();
        verifyNoMoreInteractions(listRepo);
    }

    // createTaskList
    @Test
    void createTaskList_whenAlreadyHasId_shouldThrow_andNeverSave() {
        TaskList withId = new TaskList(
                UUID.randomUUID(),
                "T",
                "D",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.createTaskList(withId));
        assertThat(ex.getMessage()).contains("Task list already has an Id!");

        verify(listRepo, never()).save(any());
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void createTaskList_whenTitleNull_shouldThrow_andNeverSave() {
        sample.setTitle(null);

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.createTaskList(sample));
        assertThat(ex.getMessage()).contains("Task list title cannot be null or empty!");

        verify(listRepo, never()).save(any());
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void createTaskList_shouldCopyFieldsSetTimestamps_andSaveNew() {
        when(listRepo.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = svc.createTaskList(sample);

        ArgumentCaptor<TaskList> captor = ArgumentCaptor.forClass(TaskList.class);
        verify(listRepo).save(captor.capture());
        TaskList toSave = captor.getValue();

        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getTitle()).isEqualTo("My list");
        assertThat(toSave.getDescription()).isEqualTo("Desc");
        assertThat(toSave.getCreated()).isNotNull();
        assertThat(toSave.getUpdated()).isNotNull();

        assertThat(saved.getTitle()).isEqualTo("My list");

        verifyNoMoreInteractions(listRepo);
    }

    // getTaskList
    @Test
    void getTaskList_found_shouldReturnOptionalPresent() {
        UUID id = UUID.randomUUID();

        when(listRepo.findById(id)).thenReturn(Optional.of(sample));

        var opt = svc.getTaskList(id);

        assertThat(opt).isPresent();

        verify(listRepo).findById(id);
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void getTaskList_notFound_shouldReturnEmpty() {
        UUID id = UUID.randomUUID();

        when(listRepo.findById(id)).thenReturn(Optional.empty());

        var opt = svc.getTaskList(id);

        assertThat(opt).isEmpty();

        verify(listRepo).findById(id);
        verifyNoMoreInteractions(listRepo);
    }

    // updateTaskList
    @Test
    void updateTaskList_whenBodyIdNull_shouldThrow() {
        UUID pathId = UUID.randomUUID();

        TaskList body = new TaskList();
        body.setId(null);
        body.setTitle("X");

        assertThatThrownBy(() -> svc.updateTaskList(pathId, body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have an Id");

        verify(listRepo, never()).findById(any());
        verify(listRepo, never()).save(any());
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void updateTaskList_whenBodyIdNotEqualsPathId_shouldThrow() {
        UUID pathId = UUID.randomUUID();

        // ทำให้ path id != body id
        sample.setId(UUID.randomUUID());

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.updateTaskList(pathId, sample));
        assertThat(ex.getMessage()).contains("Attempt to change task list ID");

        verify(listRepo, never()).findById(any());
        verify(listRepo, never()).save(any());
        verifyNoMoreInteractions(listRepo);

    }

    @Test
    void updateTaskList_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();

        // path id = body id but not found when findTaskList
        sample.setId(id);
        when(listRepo.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalStateException.class, () -> svc.updateTaskList(id, sample));
        assertThat(ex.getMessage()).contains("not found");

        verify(listRepo).findById(id);
        verify(listRepo, never()).save(any());
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void updateTaskList_shouldMutateExisting_andSave() {
        UUID id = UUID.randomUUID();

        TaskList existing = new TaskList(
                id,
                "Old",
                "Old d",
                new ArrayList<>(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );


        sample.setId(id);
        sample.setTitle("New");
        sample.setDescription("New d");
        //  mock repo พบ existing and save then return object input
        when(listRepo.findById(id)).thenReturn(Optional.of(existing));
        when(listRepo.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));

        var update = svc.updateTaskList(id, sample);

        // assert
        assertThat(update.getId()).isEqualTo(id);
        assertThat(update.getTitle()).isEqualTo("New");
        assertThat(update.getDescription()).isEqualTo("New d");
        assertThat(update.getUpdated()).isNotNull();

        // verify
        verify(listRepo).findById(id);
        verify(listRepo).save(existing);
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void deleteTaskList_whenRepositoryThrows_shouldPropagateException() {
        UUID id = UUID.randomUUID();

    // กรณี mock db error
//        // Arrange: จำลองกรณี DB/infra มีปัญหาให้ deleteById(...) โยน exception
//        doThrow(new IllegalStateException("DB error")).when(listRepo).deleteById(id);
//
//        // Act + Assert: service ควรส่งต่อ (propagate) exception เดียวกันออกมา
//        assertThatThrownBy(() -> svc.deleteTaskList(id))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("DB error");

        when(listRepo.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.deleteTaskList(id));

        assertThat(ex.getMessage()).contains("Task list not found");

        verify(listRepo,never()).deleteById(id);
        verifyNoMoreInteractions(listRepo);
    }

    @Test
    void deleteTaskList_shouldCallDeleteByIdOnce() {
        UUID id = UUID.randomUUID();

        when(listRepo.findById(id)).thenReturn(Optional.of(sample));

        // act
        svc.deleteTaskList(id);

        verify(listRepo).deleteById(id);
        verifyNoMoreInteractions(listRepo);
    }


}
