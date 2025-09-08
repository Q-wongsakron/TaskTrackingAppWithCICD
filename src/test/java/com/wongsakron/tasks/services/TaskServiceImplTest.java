package com.wongsakron.tasks.services;

import com.wongsakron.tasks.domain.entities.Task;
import com.wongsakron.tasks.domain.entities.TaskList;
import com.wongsakron.tasks.domain.entities.TaskPriority;
import com.wongsakron.tasks.domain.entities.TaskStatus;
import com.wongsakron.tasks.repositories.TaskListRepository;
import com.wongsakron.tasks.repositories.TaskRepository;
import com.wongsakron.tasks.services.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {
    @Mock
    TaskRepository taskRepo;
    @Mock
    TaskListRepository taskListRepo;

    @InjectMocks
    TaskServiceImpl svc;

    private UUID listId;
    private UUID taskId;
    private TaskList list;
    private Task sample;

    @BeforeEach
    void setUp() {
        listId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        list = new TaskList();
        list.setId(listId);
        list.setTitle("L");
        list.setDescription("D");

        sample = new Task();
        sample.setTitle("T");
        sample.setDescription("D");
        sample.setPriority(TaskPriority.MEDIUM);
        sample.setStatus(TaskStatus.OPEN);
        sample.setDueDate(LocalDateTime.now().plusDays(1));
    }


    // listTasks
    @Test
    void listTask_shouldReturnOnlyTaskOfList() {
        when(taskRepo.findByTaskListId(listId)).thenReturn(List.of(sample));

        var out = svc.listTasks(listId);

        assertThat(out).hasSize(1)
                .extracting(Task::getTitle)
                .containsExactly("T");

        verify(taskRepo).findByTaskListId(listId);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void listTask_whenNone_shouldReturnEmpty() {
        when(taskRepo.findByTaskListId(listId)).thenReturn(Collections.emptyList());

        var out = svc.listTasks(listId);

        assertThat(out).isEmpty();

        verify(taskRepo).findByTaskListId(listId);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    // createTask
    @Test
    void createTask_whenTaskHasId_shouldThrows_andNeverSave() {
        Task withId = new Task();
        withId.setId(UUID.randomUUID());
        withId.setTitle("X");

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.createTask(listId, withId));
        assertThat(ex.getMessage()).contains("Task already has an Id!");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);

    }

    @Test
    void createTask_whenTitleNullOrBlank_shouldThrow_andNeverSave() {
        Task nullTitle = new Task();
        nullTitle.setTitle(null);
        assertThatThrownBy(() -> svc.createTask(listId, nullTitle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task title cannot be null!");

        Task blankTitle = new Task();
        blankTitle.setTitle(" ");
        assertThatThrownBy(() -> svc.createTask(listId, blankTitle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task title cannot be blank!");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void createTask_whenTaskListNotFound_shouldThrowIllegalState(){
        when(taskListRepo.findById(listId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> svc.createTask(listId, sample))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Task List ID provided!");

        verify(taskListRepo).findById(listId);
        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void createTask_happy_shouldAttachList_setDefaults_andSave() {
        Task input = new Task();
        input.setTitle("T");
        input.setDescription("D");
        input.setDueDate(LocalDateTime.now().plusDays(2));
        input.setPriority(null); // set default

        when(taskListRepo.findById(listId)).thenReturn(Optional.of(list));
        when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = svc.createTask(listId, input);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepo).save(captor.capture());
        Task toSave = captor.getValue();

        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getTitle()).isEqualTo("T");
        assertThat(toSave.getDescription()).isEqualTo("D");
        assertThat(toSave.getDueDate()).isNotNull();
        assertThat(toSave.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(toSave.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(toSave.getCreated()).isNotNull();
        assertThat(toSave.getUpdated()).isNotNull();
        assertThat(toSave.getTaskList()).isSameAs(list);

        assertThat(saved.getTaskList()).isSameAs(list);

        verify(taskListRepo).findById(listId);
        verifyNoMoreInteractions(taskListRepo, taskRepo);
    }

    @Test
    void getTask_found_shouldReturnPresent() {
        when(taskRepo.findByTaskListIdAndId(listId, taskId)).thenReturn(Optional.of(sample));

        var out = svc.getTask(listId, taskId);

        assertThat(out).isPresent();
        verify(taskRepo).findByTaskListIdAndId(listId,taskId);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void getTask_notFound_shouldReturnEmpty() {
        when(taskRepo.findByTaskListIdAndId(listId, taskId)).thenReturn(Optional.empty());
        var out = svc.getTask(listId, taskId);
        assertThat(out).isEmpty();
        verify(taskRepo).findByTaskListIdAndId(listId,taskId);
        verifyNoMoreInteractions(taskListRepo, taskRepo);

    }

    // UpdateTask

    @Test
    void updateTask_whenIdMismatch_shouldThrow() {
        Task body = new Task();
        // ID new not match
        body.setId(UUID.randomUUID());
        body.setTitle("N");
        body.setPriority(TaskPriority.LOW);
        body.setStatus(TaskStatus.OPEN);
        var ex = assertThrows(IllegalArgumentException.class,() -> svc.updateTask(listId, taskId, body));

        assertThat(ex.getMessage()).contains("Task ID does not match the provided ID!");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

//    @Test
//    void updateTask_whenIdNull_shouldThrow(){
//        Task body = new Task();
//        // setId null
//        body.setId(null);
//        body.setTitle("N");
//        body.setPriority(TaskPriority.LOW);
//        body.setStatus(TaskStatus.OPEN);
//
//        var ex = assertThrows(IllegalArgumentException.class, () -> svc.updateTask(listId, taskId, body));
//
//        assertThat(ex.getMessage()).contains("must have an ID");
//
//        verify(taskRepo, never()).save(any());
//        verifyNoMoreInteractions(taskListRepo, taskRepo);
//    }

    @Test
    void updateTask_whenPriorityNull_shouldThrow() {
        Task body = new Task();
        body.setId(taskId);
        body.setPriority(null);
        body.setStatus(TaskStatus.OPEN);
        var ex = assertThrows(IllegalArgumentException.class, () -> svc.updateTask(listId, taskId, body));

        assertThat(ex.getMessage()).contains("Task priority cannot be null!");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void updateTask_whenStatusNull_shouldThrow() {
        Task body = new Task();
        body.setId(taskId);
        body.setPriority(TaskPriority.LOW);
        body.setStatus(null);

        var ex = assertThrows(IllegalArgumentException.class, () -> svc.updateTask(listId, taskId, body));
        assertThat(ex.getMessage()).contains("Task status cannot be null!");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void updateTask_whenNotFound_shouldThrowIllegalState() {
        Task body = new Task();
        body.setId(taskId);
        body.setPriority(TaskPriority.HIGH);
        body.setStatus(TaskStatus.CLOSED);

        when(taskRepo.findByTaskListIdAndId(listId, taskId)).thenReturn(Optional.empty());
        var ex = assertThrows(IllegalArgumentException.class, () -> svc.updateTask(listId, taskId, body));
        assertThat(ex.getMessage()).contains("Task not found");

        verify(taskRepo, never()).save(any());
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void updateTask_happy_shouldMutateFields_andSave() {
        Task existing = new Task();
        existing.setId(taskId);
        existing.setTitle("Old");
        existing.setDescription("Old d");
        existing.setPriority(TaskPriority.LOW);
        existing.setStatus(TaskStatus.OPEN);

        when(taskRepo.findByTaskListIdAndId(listId,taskId)).thenReturn(Optional.of(existing));
        when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task body = new Task();
        body.setId(taskId);
        body.setTitle("New");
        body.setDescription("New d");
        body.setPriority(TaskPriority.HIGH);
        body.setStatus(TaskStatus.CLOSED);
        body.setDueDate(LocalDateTime.now().plusDays(3));

        var out = svc.updateTask(listId, taskId, body);

        assertThat(out.getId()).isEqualTo(taskId);
        assertThat(out.getTitle()).isEqualTo("New");
        assertThat(out.getDescription()).isEqualTo("New d");
        assertThat(out.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(out.getStatus()).isEqualTo(TaskStatus.CLOSED);
        assertThat(out.getUpdated()).isNotNull();

        verify(taskRepo).findByTaskListIdAndId(listId, taskId);
        verify(taskRepo).save(existing);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void deleteTask_shouldCallDeleteByListAndId() {
        svc.deleteTask(listId, taskId);
        verify(taskRepo).deleteByTaskListIdAndId(listId, taskId);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }

    @Test
    void deleteTask_whenRepositoryThrows_shouldPropagate() {
        doThrow(new IllegalStateException("DB error"))
                .when(taskRepo).deleteByTaskListIdAndId(listId, taskId);

        assertThatThrownBy(() -> svc.deleteTask(listId, taskId))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("DB error");

        verify(taskRepo).deleteByTaskListIdAndId(listId, taskId);
        verifyNoMoreInteractions(taskRepo, taskListRepo);
    }





}
