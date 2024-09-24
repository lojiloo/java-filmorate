package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    User createNewUser(User user);

    boolean isEmailTaken(User user);

    void updateUser(User user);

    List<User> getUsers();

    User getUser(long id);

    void addNewFriend(long id, long friendId);

    List<User> getUserFriends(long id);

    List<Long> getUserFriendsIds(long id);

    Map<Long, List<Long>> getUsersFriendsIds();

    List<User> getCommonFriends(long id, long otherId);

    void deleteFromFriends(long id, long friendId);

    boolean friendIsAdded(long id, long friendId);

    boolean contains(long id);

    long getNextId();
}
