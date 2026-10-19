package ru.practice.planner.dto;

public record UpdateTaskStatusRequest(
        boolean completed
) {
}