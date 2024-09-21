package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createNewUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUser(long id);

    User addNewFriend(long id, long friendId);

    List<User> getUserFriends(long id);

    List<User> getCommonFriends(long id, long otherId);

    User deleteFromFriends(long id, long friendId);

    boolean contains(long id);

}
