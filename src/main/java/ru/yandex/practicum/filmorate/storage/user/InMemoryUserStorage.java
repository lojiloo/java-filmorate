package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public User createNewUser(User user) {
        if (user.getId() != null) {
            log.warn("Обнаружен id у незарегистрированного пользователя: {}", user.getEmail());
            throw new InvalidRequestException("id не может быть введен вручную");
        }
        checkName(user);
        isEmailTaken(user);

        user.setId(++id);
        users.put(user.getId(), user);
        log.info("Пользователь с email {} успешно добавлен", user.getEmail());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            checkName(user);
            users.put(user.getId(), user);
            log.info("Пользователь с id {} успешно обновлён", user.getId());
            return user;
        }
        log.warn("Пользователя с id {} не существует", user.getId());
        throw new NotFoundException("Пользователь с данным id не найден");
    }

    @Override
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    @Override
    public User getUser(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        log.warn("Пользователя с данным id не существует");
        throw new NotFoundException("Недействительный id");
    }

    public boolean contains(long id) {
        return users.containsKey(id);
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void isEmailTaken(User user) {
        String email = user.getEmail();
        for (User u : users.values()) {
            if (email.equals(u.getEmail())) {
                log.warn("У пользователя {} указан email, использованный в другом профиле: {}", user.getLogin(), u.getLogin());
                throw new RuntimeException("Данный email уже используется другим пользователем");
            }
        }
    }

}
