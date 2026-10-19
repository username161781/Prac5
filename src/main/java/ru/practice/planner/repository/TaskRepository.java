package ru.practice.planner.repository;

import org.springframework.stereotype.Repository;
import ru.practice.planner.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private static final String URL = "jdbc:sqlite:planner.db";

    public TaskRepository() {
        initializeDatabase();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void initializeDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    completed INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL
                )
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось создать базу данных", e
            );
        }
    }

    public List<Task> findAll() {
        String sql = """
                SELECT id, title, description, completed, created_at
                FROM tasks
                ORDER BY id DESC
                """;

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                tasks.add(mapTask(result));
            }

            return tasks;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить список задач", e
            );
        }
    }

    public Optional<Task> findById(long id) {
        String sql = """
                SELECT id, title, description, completed, created_at
                FROM tasks
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapTask(result));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось найти задачу", e
            );
        }
    }

    public Task save(String title, String description) {
        String sql = """
                INSERT INTO tasks
                    (title, description, completed, created_at)
                VALUES (?, ?, 0, ?)
                """;

        String createdAt = java.time.LocalDateTime.now().toString();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, createdAt);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Task(
                            keys.getLong(1),
                            title,
                            description,
                            false,
                            createdAt
                    );
                }
            }

            try (Statement idStatement = connection.createStatement();
                 ResultSet result = idStatement.executeQuery(
                         "SELECT last_insert_rowid()")) {

                if (result.next()) {
                    return new Task(
                            result.getLong(1),
                            title,
                            description,
                            false,
                            createdAt
                    );
                }
            }

            throw new SQLException("Не удалось получить ID новой задачи");

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось создать задачу", e
            );
        }
    }

    public boolean updateStatus(long id, boolean completed) {
        String sql = """
                UPDATE tasks
                SET completed = ?
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, completed ? 1 : 0);
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось изменить статус задачи", e
            );
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось удалить задачу", e
            );
        }
    }

    private Task mapTask(ResultSet result) throws SQLException {
        return new Task(
                result.getLong("id"),
                result.getString("title"),
                result.getString("description"),
                result.getInt("completed") == 1,
                result.getString("created_at")
        );
    }
}