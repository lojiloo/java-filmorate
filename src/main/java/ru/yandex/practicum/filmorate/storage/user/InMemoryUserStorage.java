package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component("InMemoryUsers")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public User createNewUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с email {} успешно добавлен", user.getEmail());

        return user;
    }

    @Override
    public void updateUser(User user) {
        if (users.containsKey(user.getId())) {
            checkName(user);
            users.put(user.getId(), user);
            log.info("Пользователь с id {} успешно обновлён", user.getId());
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

    @Override
    public void addNewFriend(long id, long friendId) {
        User user = getUser(id);
        User friend = getUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        log.info("Пользователь {} успешно добавил(а) в друзья пользователя {}", user.getLogin(), friend.getLogin());
    }

    @Override
    public List<User> getUserFriends(long id) {
        List<User> friends = new ArrayList<>();

        User user = getUser(id);
        for (long friendId : user.getFriends()) {
            friends.add(getUser(friendId));
        }
        return friends;
    }

    @Override
    public List<Long> getUserFriendsIds(long id) {
        return new ArrayList<>(getUser(id).getFriends());
    }

    @Override
    public Map<Long, List<Long>> getUsersFriendsIds() {
        Map<Long, List<Long>> usersFriendsIds = new HashMap<>();
        for (User user : users.values()) {
            usersFriendsIds.put(user.getId(), new ArrayList<>(user.getFriends()));
        }
        return usersFriendsIds;
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        Set<Long> u1FriendsId = getUser(id).getFriends();
        Set<Long> u2FriendsId = getUser(otherId).getFriends();
        u1FriendsId.retainAll(u2FriendsId);

        List<User> intersection = new ArrayList<>();
        for (long friendId : u1FriendsId) {
            intersection.add(getUser(friendId));
        }

        log.trace("Пересечения: {}", intersection);
        return intersection;
    }

    @Override
    public void deleteFromFriends(long id, long friendId) {
        User user = getUser(id);
        User friend = getUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        log.info("Пользователь {} успешно удалил(а) из друзей пользователя {}", user.getLogin(), friend.getLogin());
    }

    @Override
    public boolean friendIsAdded(long id, long friendId) {
        return users.get(id).getFriends().contains(friendId);
    }

    public boolean contains(long id) {
        return users.containsKey(id);
    }

    @Override
    public long getNextId() {
        return ++id;
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public boolean isEmailTaken(User user) {
        String email = user.getEmail();
        for (User u : users.values()) {
            if (email.equals(u.getEmail())) {
                return true;
            }
        }
        return false;
    }

}
