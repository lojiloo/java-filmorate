package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createNewUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    List<User> getUsers(long id);

    boolean contains(long id);

}
