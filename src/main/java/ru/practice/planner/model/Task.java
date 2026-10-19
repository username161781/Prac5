package ru.practice.planner.model;

public record Task(
        long id,
        String title,
        String description,
        boolean completed,
        String createdAt
) {
}