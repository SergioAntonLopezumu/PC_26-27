package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

  private TaskSpecifications() {}

  public static Specification<Task> hasStatus(TaskStatus status) {
    return (root, query, builder) ->
        status == null ? null : builder.equal(root.get("status"), status);
  }

  public static Specification<Task> dueDateOnOrAfter(LocalDate from) {
    return (root, query, builder) ->
        from == null ? null : builder.greaterThanOrEqualTo(root.get("dueDate"), from);
  }

  public static Specification<Task> dueDateOnOrBefore(LocalDate to) {
    return (root, query, builder) ->
        to == null ? null : builder.lessThanOrEqualTo(root.get("dueDate"), to);
  }
}
