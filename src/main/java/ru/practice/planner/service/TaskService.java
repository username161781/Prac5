package ru.practice.planner.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practice.planner.dto.CreateTaskRequest;
import ru.practice.planner.model.Task;
import ru.practice.planner.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Task createTask(CreateTaskRequest request) {
        if (request == null
                || request.title() == null
                || request.title().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название задачи обязательно"
            );
        }

        String title = request.title().trim();
        String description = request.description() == null
                ? ""
                : request.description().trim();

        if (title.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название не должно превышать 200 символов"
            );
        }

        if (description.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Описание не должно превышать 2000 символов"
            );
        }

        return repository.save(title, description);
    }

    public Task updateStatus(long id, boolean completed) {
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректный идентификатор задачи"
            );
        }

        boolean updated = repository.updateStatus(id, completed);

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Задача не найдена"
            );
        }

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Задача не найдена"
                ));
    }

    public void deleteTask(long id) {
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректный идентификатор задачи"
            );
        }

        boolean deleted = repository.deleteById(id);

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Задача не найдена"
            );
        }
    }
}