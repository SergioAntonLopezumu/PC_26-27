package com.example.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskPriority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.InvalidTaskException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;

  @InjectMocks private TaskServiceImpl taskService;

  private Task validTask;

  @BeforeEach
  void setUp() {
    validTask = new Task();
    validTask.setId(1L);
    validTask.setTitle("Implementar API");
    validTask.setDescription("Crear endpoints CRUD");
    validTask.setStatus(TaskStatus.PENDING);
    validTask.setPriority(TaskPriority.HIGH);
    validTask.setDueDate(LocalDate.now().plusDays(3));
  }

  @Test
  void getAllTasks_shouldReturnAllTasksWhenStatusIsNull() {
    when(taskRepository.findAll()).thenReturn(List.of(validTask));

    List<Task> tasks = taskService.getAllTasks(null);

    assertEquals(List.of(validTask), tasks);
    verify(taskRepository).findAll();
    verify(taskRepository, never()).findByStatus(any());
  }

  @Test
  void getAllTasks_shouldFilterByStatusWhenProvided() {
    when(taskRepository.findByStatus(TaskStatus.COMPLETED)).thenReturn(List.of(validTask));

    List<Task> tasks = taskService.getAllTasks(TaskStatus.COMPLETED);

    assertEquals(List.of(validTask), tasks);
    verify(taskRepository).findByStatus(TaskStatus.COMPLETED);
    verify(taskRepository, never()).findAll();
  }

  @Test
  void createTask_shouldRejectPastDueDate() {
    Task task = new Task();
    task.setTitle("Tarea vencida");
    task.setDescription("Fecha ya pasada");
    task.setStatus(TaskStatus.PENDING);
    task.setPriority(TaskPriority.MEDIUM);
    task.setDueDate(LocalDate.now().minusDays(1));

    InvalidTaskException exception =
        assertThrows(InvalidTaskException.class, () -> taskService.createTask(task));

    assertEquals("La fecha límite no puede ser anterior a hoy.", exception.getMessage());
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void createTask_shouldRejectBlankTitle() {
    Task task = new Task();
    task.setTitle("    ");
    task.setDescription("Sin título válido");
    task.setStatus(TaskStatus.PENDING);
    task.setPriority(TaskPriority.LOW);
    task.setDueDate(LocalDate.now().plusDays(2));

    InvalidTaskException exception =
        assertThrows(InvalidTaskException.class, () -> taskService.createTask(task));

    assertTrue(exception.getMessage().contains("título"));
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void createTask_shouldSaveValidTask() {
    when(taskRepository.save(any(Task.class))).thenReturn(validTask);

    Task saved = taskService.createTask(validTask);

    assertNotNull(saved);
    assertEquals("Implementar API", saved.getTitle());
    assertEquals(TaskStatus.PENDING, saved.getStatus());
    verify(taskRepository).save(validTask);
  }

  @Test
  void updateTask_shouldRejectMissingTask() {
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(99L, validTask));

    assertEquals("Tarea no encontrada con id: 99", exception.getMessage());
  }

  @Test
  void deleteTask_shouldDeleteExistingTask() {
    when(taskRepository.existsById(1L)).thenReturn(true);

    taskService.deleteTask(1L);

    verify(taskRepository).deleteById(1L);
  }
}
