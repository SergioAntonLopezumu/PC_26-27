package com.example.taskmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskPriority;
import com.example.taskmanager.enums.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
class TaskSpecificationsTest {

  @Autowired private TaskRepository taskRepository;

  private final LocalDate reference = LocalDate.now().plusDays(10);

  @BeforeEach
  void seed() {
    taskRepository.save(task("Pasada", TaskStatus.PENDING, reference.minusDays(5)));
    taskRepository.save(task("Dentro", TaskStatus.PENDING, reference));
    taskRepository.save(task("Futura", TaskStatus.COMPLETED, reference.plusDays(5)));
  }

  private Task task(String title, TaskStatus status, LocalDate dueDate) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("descripción de " + title);
    task.setStatus(status);
    task.setPriority(TaskPriority.MEDIUM);
    task.setDueDate(dueDate);
    return task;
  }

  private List<Task> findAll(Specification<Task> specification) {
    return taskRepository.findAll(specification, PageRequest.of(0, 10)).getContent();
  }

  private Specification<Task> base() {
    return Specification.where(TaskSpecifications.hasStatus(null))
        .and(TaskSpecifications.dueDateOnOrAfter(null))
        .and(TaskSpecifications.dueDateOnOrBefore(null));
  }

  @Test
  void returnsAllTasksWhenNoFilterIsProvided() {
    assertEquals(3, findAll(base()).size());
  }

  @Test
  void filtersByDateRangeInclusive() {
    Specification<Task> specification =
        Specification.where(TaskSpecifications.dueDateOnOrAfter(reference))
            .and(TaskSpecifications.dueDateOnOrBefore(reference.plusDays(5)));

    List<Task> result = findAll(specification);

    assertEquals(2, result.size());
    assertEquals(List.of("Dentro", "Futura"), result.stream().map(Task::getTitle).toList());
  }

  @Test
  void filtersByStatusAndRange() {
    Specification<Task> specification =
        Specification.where(TaskSpecifications.hasStatus(TaskStatus.PENDING))
            .and(TaskSpecifications.dueDateOnOrAfter(reference));

    List<Task> result = findAll(specification);

    assertEquals(1, result.size());
    assertEquals("Dentro", result.get(0).getTitle());
  }
}
