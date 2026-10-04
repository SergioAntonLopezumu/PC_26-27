package com.example.taskmanager.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.TaskService;
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
  void getAllTasks_shouldPassNullWhenStatusIsOmitted() throws Exception {
    when(taskService.getAllTasks(null)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());

    verify(taskService).getAllTasks(null);
  }

  @Test
  void getAllTasks_shouldPassStatusToService() throws Exception {
    when(taskService.getAllTasks(TaskStatus.COMPLETED)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/tasks").param("status", "COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());

    verify(taskService).getAllTasks(TaskStatus.COMPLETED);
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
}
