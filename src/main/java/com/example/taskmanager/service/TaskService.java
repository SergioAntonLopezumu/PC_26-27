package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskPageResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskStatus;

public interface TaskService {
  TaskPageResponse getAllTasks(TaskStatus status, int page, int size);

  Task getTaskById(Long id);

  Task createTask(Task task);

  Task updateTask(Long id, Task task);

  void deleteTask(Long id);
}
