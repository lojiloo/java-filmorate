package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    @Qualifier("dbUsers")
    private final UserStorage userStorage;

    public User createNewUser(User user) {
        if (user.getId() != null) {
            log.warn("Обнаружен id у незарегистрированного пользователя: {}", user.getEmail());
            throw new InvalidRequestException("id не может быть введен вручную");
        }
        if (userStorage.isEmailTaken(user)) {
            log.warn("У пользователя {} указан email, использованный в другом профиле", user.getLogin());
            throw new RuntimeException("Данный email уже используется другим пользователем");
        }

        checkName(user);
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
        userStorage.updateUser(user);
        user.getFriends().addAll(userStorage.getUserFriendsIds(id));

        return user;
    }

    public List<User> getUsers() {
        List<User> users = userStorage.getUsers();
        Map<Long, List<Long>> usersFriendsIds = userStorage.getUsersFriendsIds();
        for (int i = 0; i < users.size(); i++) {
            if (usersFriendsIds.containsKey(users.get(i).getId())) {
                users.get(i).getFriends().addAll(usersFriendsIds.get(users.get(i).getId()));
            }
        }

        return users;
    }

    public User getUser(long id) {
        if (!userStorage.contains(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        User user = userStorage.getUser(id);
        user.getFriends().addAll(userStorage.getUserFriendsIds(id));

        return user;
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

        if (!userStorage.friendIsAdded(id, friendId)) {
            userStorage.addNewFriend(id, friendId);
        }
        User user = getUser(id);
        user.getFriends().addAll(userStorage.getUserFriendsIds(id));

        return user;
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

        User user = getUser(id);
        userStorage.deleteFromFriends(id, friendId);
        user.getFriends().remove(friendId);

        return user;
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

        List<User> commonFriends = userStorage.getCommonFriends(id, otherId);
        for (User friend : commonFriends) {
            friend.getFriends().addAll(userStorage.getUserFriendsIds(friend.getId()));
        }
        return commonFriends;
    }

    public boolean contains(long id) {
        return userStorage.contains(id);
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
