package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        if (user.getId() != null) {
            log.warn("Обнаружен id у незарегистрированного пользователя: {}", user.getEmail());
            throw new InvalidRequestException("id не может быть введен вручную");
        }
        checkName(user);

        user.setId(++id);
        users.put(user.getId(), user);
        log.info("Пользователь с email {} успешно добавлен", user.getEmail());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (users.containsKey(user.getId())) {
            checkName(user);
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

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
