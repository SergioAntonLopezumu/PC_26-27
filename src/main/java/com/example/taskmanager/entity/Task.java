package com.example.taskmanager.entity;

import com.example.taskmanager.enums.TaskPriority;
import com.example.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "El título es obligatorio.")
  @Size(max = 120, message = "El título no puede exceder 120 caracteres.")
  @Column(nullable = false)
  private String title;

  @Size(max = 500, message = "La descripción no puede exceder 500 caracteres.")
  @Column(length = 500)
  private String description;

  @NotNull(message = "El estado es obligatorio.")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status;

  @NotNull(message = "La prioridad es obligatoria.")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskPriority priority;

  @NotNull(message = "La fecha límite es obligatoria.")
  @FutureOrPresent(message = "La fecha límite debe ser hoy o una fecha futura.")
  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  public Task() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public void setPriority(TaskPriority priority) {
    this.priority = priority;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }
}
