const API_URL = "/api/tasks";

const form = document.querySelector("#task-form");
const titleInput = document.querySelector("#title");
const descriptionInput = document.querySelector("#description");
const taskList = document.querySelector("#task-list");
const message = document.querySelector("#message");
const summary = document.querySelector("#summary");
const refreshButton = document.querySelector("#refresh-button");

function showMessage(text, isError = false) {
    message.textContent = text;
    message.classList.toggle("error", isError);
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    if (!response.ok) {
        let errorText = `Ошибка HTTP ${response.status}`;

        try {
            const data = await response.json();
            errorText = data.detail || data.message || errorText;
        } catch {
            // Ответ может не содержать JSON.
        }

        throw new Error(errorText);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function createButton(text, className, handler) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = text;
    button.className = className;

    button.addEventListener("click", async () => {
        button.disabled = true;

        try {
            await handler();
        } catch (error) {
            showMessage(error.message, true);
        } finally {
            button.disabled = false;
        }
    });

    return button;
}

function renderTasks(tasks) {
    taskList.replaceChildren();

    const completedCount = tasks.filter(task => task.completed).length;
    summary.textContent =
        `Всего: ${tasks.length} · Выполнено: ${completedCount}`;

    if (tasks.length === 0) {
        const empty = document.createElement("li");
        empty.className = "empty";
        empty.textContent = "Пока нет задач. Добавьте первую!";
        taskList.append(empty);
        return;
    }

    for (const task of tasks) {
        const item = document.createElement("li");
        item.className = task.completed ? "task completed" : "task";

        const main = document.createElement("div");
        main.className = "task-main";

        const title = document.createElement("p");
        title.className = "task-title";
        title.textContent = task.title;

        const description = document.createElement("p");
        description.className = "task-description";
        description.textContent = task.description || "Без описания";

        const date = document.createElement("span");
        date.className = "task-date";
        date.textContent = `Создана: ${task.createdAt}`;

        main.append(title, description, date);

        const actions = document.createElement("div");
        actions.className = "task-actions";

        const statusButton = createButton(
            task.completed ? "Вернуть в работу" : "Выполнить",
            "secondary",
            async () => {
                await request(`${API_URL}/${task.id}`, {
                    method: "PATCH",
                    body: JSON.stringify({
                        completed: !task.completed
                    })
                });

                showMessage("Статус задачи обновлён.");
                await loadTasks();
            }
        );

        const deleteButton = createButton(
            "Удалить",
            "delete-button",
            async () => {
                await request(`${API_URL}/${task.id}`, {
                    method: "DELETE"
                });

                showMessage("Задача удалена.");
                await loadTasks();
            }
        );

        actions.append(statusButton, deleteButton);
        item.append(main, actions);
        taskList.append(item);
    }
}

async function loadTasks() {
    try {
        const tasks = await request(API_URL);
        renderTasks(tasks);
    } catch (error) {
        showMessage(
            `Не удалось загрузить задачи: ${error.message}`,
            true
        );
    }
}

form.addEventListener("submit", async event => {
    event.preventDefault();

    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();

    if (!title) {
        showMessage("Введите название задачи.", true);
        titleInput.focus();
        return;
    }

    const submitButton = form.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    try {
        await request(API_URL, {
            method: "POST",
            body: JSON.stringify({ title, description })
        });

        form.reset();
        showMessage("Задача успешно создана.");
        await loadTasks();
        titleInput.focus();
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        submitButton.disabled = false;
    }
});

refreshButton.addEventListener("click", loadTasks);

loadTasks();