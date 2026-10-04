package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Task;
import java.util.List;

public record TaskPageResponse(
    List<Task> content, long totalElements, int totalPages, int page, int size) {}
