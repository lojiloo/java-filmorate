package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User createNewUser(User user) {
        return userStorage.createNewUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public List<User> getUsers(long id) {
        return userStorage.getUsers(id);
    }

    public User addNewFriend(long id, long friendId) {
        if (!userStorage.contains(id)) {
            log.warn("При добавлении пользователя в друзья произошла ошибка поиска по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userStorage.contains(friendId)) {
            log.warn("При добавлении пользователя в друзья произошла ошибка поиска по id: {}", friendId);
            throw new NotFoundException("Недействительный id");
        }

        User user = userStorage.getUsers(id).getFirst();
        User friend = userStorage.getUsers(friendId).getFirst();

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        log.info("Пользователь {} успешно добавил(а) в друзья пользователя {}", user.getLogin(), friend.getLogin());
        return user;
    }

    public User deleteFromFriends(long id, long friendId) {
        if (!userStorage.contains(id)) {
            log.warn("При удалении пользователя из друзей произошла ошибка поиска по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userStorage.contains(friendId)) {
            log.warn("При удалении пользователя из друзей произошла ошибка поиска по id: {}", friendId);
            throw new NotFoundException("Недействительный id");
        }

        User user = userStorage.getUsers(id).getFirst();
        User friend = userStorage.getUsers(friendId).getFirst();

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        log.info("Пользователь {} успешно удалил(а) из друзей пользователя {}", user.getLogin(), friend.getLogin());
        return user;
    }

    public List<User> getUserFriends(long id) {
        if (!userStorage.contains(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Недействительный id");
        }

        List<User> friends = new ArrayList<>();

        User user = userStorage.getUsers(id).getFirst();
        for (long friendId : user.getFriends()) {
            friends.add(userStorage.getUsers(friendId).getFirst());
        }
        return friends;
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

        Set<Long> u1FriendsId = userStorage.getUsers(id).getFirst().getFriends();
        Set<Long> u2FriendsId = userStorage.getUsers(otherId).getFirst().getFriends();
        u1FriendsId.retainAll(u2FriendsId);

        List<User> intersection = new ArrayList<>();
        for (long friendId : u1FriendsId) {
            intersection.add(userStorage.getUsers(friendId).getFirst());
        }

        log.trace("Пересечения: {}", intersection);
        return intersection;
    }
}
