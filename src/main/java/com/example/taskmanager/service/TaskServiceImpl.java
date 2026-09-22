package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.InvalidTaskException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public Task createTask(Task task) {
        validateTask(task);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con id: " + id));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());

        validateTask(existingTask);
        return taskRepository.save(existingTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea no encontrada con id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new InvalidTaskException("La tarea no puede ser nula.");
        }

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new InvalidTaskException("El título es obligatorio.");
        }

        if (task.getStatus() == null) {
            throw new InvalidTaskException("El estado es obligatorio.");
        }

        if (task.getPriority() == null) {
            throw new InvalidTaskException("La prioridad es obligatoria.");
        }

        if (task.getDueDate() == null) {
            throw new InvalidTaskException("La fecha límite es obligatoria.");
        }

        if (task.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidTaskException("La fecha límite no puede ser anterior a hoy.");
        }
    }
}
