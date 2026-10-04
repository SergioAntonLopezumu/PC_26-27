package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskStatus;
import java.util.List;

public interface TaskService {
  List<Task> getAllTasks(TaskStatus status);

  Task getTaskById(Long id);

  Task createTask(Task task);

  Task updateTask(Long id, Task task);

  void deleteTask(Long id);
}
