package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository("DbUsers")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private long id;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createNewUser(User user) {
        checkName(user);
        isEmailTaken(user);

        setId();
        user.setId(++id);
        String query = "INSERT INTO users (user_id, login, name, email, birthday) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(query,
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday());

        return user;
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();

        String query = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE users.user_id = ?";
        jdbcTemplate.update(query,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                id);
        return checkFriends(user);
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(query, this::mapRowToUser);

        for (int i = 0; i < users.size(); i++) {
            checkFriends(users.get(i));
        }
        return users;
    }

    @Override
    public User getUser(long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        User user = jdbcTemplate.queryForObject(query, this::mapRowToUser, id);

        return checkFriends(user);
    }

    @Override
    public boolean contains(long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?",
                Integer.class, id);

        return count > 0;
    }

    @Override
    public User addNewFriend(long id, long friendId) {
        User user = getUser(id);

        if (!usersAlreadyFriends(id, friendId)) {
            String queryAddFollower = "INSERT INTO friends" +
                    "(following_user_id, followed_user_id)" +
                    "VALUES(?, ?);";
            jdbcTemplate.update(queryAddFollower, id, friendId);
            return checkFriends(user);
        }

        return checkFriends(user);
    }

    @Override
    public List<User> getUserFriends(long id) {
        String queryFindUsersFriends = "SELECT * FROM users WHERE user_id IN " +
                "(SELECT f.followed_user_id FROM users AS u " +
                "JOIN friends AS f ON u.user_id = f.following_user_id " +
                "WHERE f.following_user_id = ?);";
        return jdbcTemplate.query(queryFindUsersFriends, this::mapRowToUser, id);
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        String queryFindFriendsById = "SELECT f.followed_user_id FROM friends AS f " +
                "JOIN users AS u ON f.following_user_id = u.user_id " +
                "WHERE u.user_id = ?;";
        List<Long> friendsById = jdbcTemplate.queryForList(queryFindFriendsById, Long.class, id);
        List<Long> friendsByOtherId = jdbcTemplate.queryForList(queryFindFriendsById, Long.class, otherId);

        friendsById.retainAll(friendsByOtherId);

        List<User> commonFriends = new ArrayList<>();
        for (Long i : friendsById) {
            commonFriends.add(getUser(i));
        }

        return commonFriends;
    }

    @Override
    public User deleteFromFriends(long id, long friendId) {
        User user = getUser(id);
        String deleteFriendQuery = "DELETE FROM friends " +
                "WHERE following_user_id = ? AND followed_user_id = ?;";
        jdbcTemplate.update(deleteFriendQuery, id, friendId);
        user.getFriends().remove(friendId);

        return user;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User checkFriends(User user) {
        long id = user.getId();
        String query = "SELECT followed_user_id FROM friends WHERE following_user_id = ? ;";
        List<Long> friendsId = jdbcTemplate.queryForList(query, Long.class, id);

        user.getFriends().addAll(friendsId);
        return user;
    }

    private void isEmailTaken(User user) {
        String email = user.getEmail();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class, email);

        if (count > 0) {
            log.warn("У пользователя {} указан email, использованный в другом профиле", user.getLogin());
            throw new RuntimeException("Данный email уже используется другим пользователем");
        }
    }

    private boolean usersAlreadyFriends(long id, long friendId) {
        String ifRelationAlreadyExistsQuery = "SELECT COUNT(*) FROM friends " +
                "WHERE following_user_id = ? AND followed_user_id = ? ;";
        Integer count = jdbcTemplate.queryForObject(ifRelationAlreadyExistsQuery,
                Integer.class,
                id, friendId);

        return count > 0;
    }

    private void setId() {
        if (contains(1)) {
            String query = "SELECT max(user_id) FROM users ;";
            this.id = jdbcTemplate.queryForObject(query, Long.class);
        }
    }
}