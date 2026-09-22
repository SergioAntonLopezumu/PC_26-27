package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
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

  @GetMapping("/tasksCONFLICTORAMA2")
  public List<Task> getAllTasks() {
    return taskService.getAllTasks();
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
