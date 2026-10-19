package ru.practice.planner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practice.planner.dto.CreateTaskRequest;
import ru.practice.planner.dto.UpdateTaskStatusRequest;
import ru.practice.planner.model.Task;
import ru.practice.planner.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")

public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return service.getAllTasks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody CreateTaskRequest request) {
        return service.createTask(request);
    }

    @PatchMapping("/{id}")
    public Task updateTaskStatus(
            @PathVariable long id,
            @RequestBody UpdateTaskStatusRequest request) {

        return service.updateStatus(id, request.completed());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable long id) {
        service.deleteTask(id);
    }
}