package com.example.taskmanager.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanager.dto.TaskPageResponse;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.InvalidTaskException;
import com.example.taskmanager.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TaskService taskService;

  @Test
  void getAllTasks_shouldPassNullWhenNoFiltersAreProvided() throws Exception {
    when(taskService.getAllTasks(null, null, null, 0, 10))
        .thenReturn(new TaskPageResponse(List.of(), 0, 0, 0, 10));

    mockMvc
        .perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0));

    verify(taskService).getAllTasks(null, null, null, 0, 10);
  }

  @Test
  void getAllTasks_shouldPassStatusToService() throws Exception {
    when(taskService.getAllTasks(TaskStatus.COMPLETED, null, null, 0, 10))
        .thenReturn(new TaskPageResponse(List.of(), 0, 0, 0, 10));

    mockMvc
        .perform(get("/api/tasks").param("status", "COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());

    verify(taskService).getAllTasks(TaskStatus.COMPLETED, null, null, 0, 10);
  }

  @Test
  void getAllTasks_shouldPassDateRangeToService() throws Exception {
    LocalDate from = LocalDate.of(2026, 10, 1);
    LocalDate to = LocalDate.of(2026, 10, 31);
    when(taskService.getAllTasks(null, from, to, 0, 10))
        .thenReturn(new TaskPageResponse(List.of(), 0, 0, 0, 10));

    mockMvc
        .perform(get("/api/tasks").param("from", "2026-10-01").param("to", "2026-10-31"))
        .andExpect(status().isOk());

    verify(taskService).getAllTasks(null, from, to, 0, 10);
  }

  @Test
  void getAllTasks_shouldPassPageAndSizeToService() throws Exception {
    when(taskService.getAllTasks(null, null, null, 1, 5))
        .thenReturn(new TaskPageResponse(List.of(), 12, 3, 1, 5));

    mockMvc
        .perform(get("/api/tasks").param("page", "1").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(12))
        .andExpect(jsonPath("$.totalPages").value(3));

    verify(taskService).getAllTasks(null, null, null, 1, 5);
  }

  @Test
  void getAllTasks_shouldRejectUnknownStatus() throws Exception {
    mockMvc
        .perform(get("/api/tasks").param("status", "UNKNOWN"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Valor inválido para el parámetro: status."));

    verifyNoInteractions(taskService);
  }

  @Test
  void getAllTasks_shouldRejectInvalidDateParameter() throws Exception {
    mockMvc
        .perform(get("/api/tasks").param("from", "no-es-una-fecha"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Valor inválido para el parámetro: from."));

    verifyNoInteractions(taskService);
  }

  @Test
  void getAllTasks_shouldReturnBadRequestForInvalidPagination() throws Exception {
    doThrow(new InvalidTaskException("El parámetro page no puede ser negativo."))
        .when(taskService)
        .getAllTasks(null, null, null, -1, 10);

    mockMvc
        .perform(get("/api/tasks").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El parámetro page no puede ser negativo."));
  }
}
