package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createNewUser(@Valid @RequestBody final User user) {
        validateUser(user);

        if (user.getId() != null) {
            log.warn("Обнаружен id у незарегистрированного пользователя: {}", user.getEmail());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с email {} успешно добавлен", user.getEmail());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody final User user) {
        validateUser(user);

        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с id {} успешно обновлён", user.getId());
            return user;
        }
        log.warn("Пользователя с id {} не существует", user.getId());
        throw new NotFoundException("Пользователь с данным id не найден");
    }

    @GetMapping
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    private void validateUser(final User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Передан пустой email");
            throw new InvalidRequestException("Email не может быть пустым");
        } else if (!user.getEmail().contains("@")) {
            log.warn("Передан некорректный email");
            throw new InvalidRequestException("Некорректный email");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Передан пустой логин");
            throw new InvalidRequestException("Логин не может быть пустым");
        } else if (user.getLogin().contains(" ")) {
            log.warn("Передан логин, содержащий пробелы");
            throw new InvalidRequestException("Логин не может содержать пробелы");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Передана дата рождения из будущего");
            throw new InvalidRequestException("Дата рождения не может быть позднее " + LocalDate.now());
        }
    }

    private long getNextId() {
        long maxCurrentId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxCurrentId;
    }
}
