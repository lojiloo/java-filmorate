package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    @Qualifier("DbUsers")
    private final UserStorage userStorage;

    public User createNewUser(User user) {
        if (user.getId() != null) {
            log.warn("Обнаружен id у незарегистрированного пользователя: {}", user.getEmail());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        return userStorage.createNewUser(user);
    }

    public User updateUser(User user) {
        Long id = user.getId();

        if (id == null) {
            log.warn("Не указан id, невозможно обновить информацию о пользователе");
            throw new InvalidRequestException("id не указан");
        } else if (!userStorage.contains(id)) {
            log.warn("Пользователя с id {} не существует", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(long id) {
        if (!userStorage.contains(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return userStorage.getUser(id);
    }

    public User addNewFriend(long id, long friendId) {
        if (!userStorage.contains(id)) {
            log.warn("При добавлении пользователя в друзья произошла ошибка поиска по id: {}", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        if (!userStorage.contains(friendId)) {
            log.warn("При добавлении пользователя в друзья произошла ошибка поиска по id: {}", friendId);
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return userStorage.addNewFriend(id, friendId);
    }

    public User deleteFromFriends(long id, long friendId) {
        if (!userStorage.contains(id)) {
            log.warn("При удалении пользователя из друзей произошла ошибка поиска по id: {}", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        if (!userStorage.contains(friendId)) {
            log.warn("При удалении пользователя из друзей произошла ошибка поиска по id: {}", friendId);
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return userStorage.deleteFromFriends(id, friendId);
    }

    public List<User> getUserFriends(long id) {
        if (!userStorage.contains(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return userStorage.getUserFriends(id);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        if (!userStorage.contains(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userStorage.contains(otherId)) {
            log.warn("Пользователь с id {} не найден", otherId);
            throw new NotFoundException("Недействительный id");
        }

        return userStorage.getCommonFriends(id, otherId);
    }
}
