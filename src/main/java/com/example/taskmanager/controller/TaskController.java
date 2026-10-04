package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskPageResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping("/tasks")
  public TaskPageResponse getAllTasks(
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return taskService.getAllTasks(status, page, size);
  }

  @GetMapping("/tasks/{id}")
  public Task getTaskById(@PathVariable Long id) {
    return taskService.getTaskById(id);
  }

  @PostMapping("/tasks")
  public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
    Task savedTask = taskService.createTask(task);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
  }

  @PutMapping("/tasks/{id}")
  public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
    return taskService.updateTask(id, task);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }
}
