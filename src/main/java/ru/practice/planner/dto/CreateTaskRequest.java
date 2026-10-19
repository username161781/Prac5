package ru.practice.planner.dto;

public record CreateTaskRequest(
        String title,
        String description
) {
}