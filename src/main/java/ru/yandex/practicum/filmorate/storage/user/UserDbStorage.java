package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository("dbUsers")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createNewUser(User user) {
        user.setId(getNextId());
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
    public boolean isEmailTaken(User user) {
        String email = user.getEmail();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class, email);

        return count > 0;
    }

    @Override
    public void updateUser(User user) {
        Long id = user.getId();

        String query = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE users.user_id = ?";
        jdbcTemplate.update(query,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                id);
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * FROM users";
        return jdbcTemplate.query(query, this::mapRowToUser);
    }

    @Override
    public User getUser(long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(query, this::mapRowToUser, id);
    }

    @Override
    public boolean contains(long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?",
                Integer.class, id);

        return count > 0;
    }

    @Override
    public long getNextId() {
        String query = "SELECT max(user_id) FROM users ;";
        Optional<Long> currentId = Optional.ofNullable(jdbcTemplate.queryForObject(query, Long.class));

        return currentId.map(id -> id + 1).orElse(1L);
    }

    @Override
    public void addNewFriend(long id, long friendId) {
        String queryAddFollower = "INSERT INTO friends" +
                "(following_user_id, followed_user_id)" +
                "VALUES(?, ?);";
        jdbcTemplate.update(queryAddFollower, id, friendId);
    }

    @Override
    public List<User> getUserFriends(long id) {
        String queryFindUsersFriends = "SELECT * FROM users " +
                "WHERE user_id IN " +
                "(SELECT f.followed_user_id FROM users AS u " +
                "JOIN friends AS f ON u.user_id = f.following_user_id " +
                "WHERE f.following_user_id = ?);";
        return jdbcTemplate.query(queryFindUsersFriends, this::mapRowToUser, id);
    }

    @Override
    public List<Long> getUserFriendsIds(long id) {
        String queryFindUserFriendsIds = "SELECT user_id FROM users " +
                "WHERE user_id IN " +
                "(SELECT f.followed_user_id FROM users AS u " +
                "JOIN friends AS f ON u.user_id = f.following_user_id " +
                "WHERE f.following_user_id = ?);";
        return jdbcTemplate.queryForList(queryFindUserFriendsIds, Long.class, id);
    }

    @Override
    public Map<Long, List<Long>> getUsersFriendsIds() {
        String getAllFriendsQuery = "SELECT * FROM friends ;";

        return jdbcTemplate.query(getAllFriendsQuery, (ResultSet rs) -> {
            HashMap<Long, List<Long>> results = new HashMap<>();
            while (rs.next()) {
                if (!results.containsKey(rs.getLong("following_user_id"))) {
                    results.put(rs.getLong("following_user_id"), new ArrayList<>());
                }
                results.get(rs.getLong("following_user_id")).add(rs.getLong("followed_user_id"));
            }
            return results;
        });
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        String queryFindFriendsById = "SELECT f.followed_user_id FROM friends AS f " +
                "JOIN users AS u ON f.following_user_id = u.user_id " +
                "WHERE u.user_id = ?;";
        List<Long> friendsById = jdbcTemplate.queryForList(queryFindFriendsById, Long.class, id);
        List<Long> friendsByOtherId = jdbcTemplate.queryForList(queryFindFriendsById, Long.class, otherId);
        friendsById.retainAll(friendsByOtherId);

        String inSql = String.join(",", Collections.nCopies(friendsById.size(), "?"));
        String queryGetFriends = String.format("SELECT * FROM users " +
                "WHERE user_id IN (%s)", inSql);

        return jdbcTemplate.query(queryGetFriends, this::mapRowToUser, friendsById.toArray());
    }

    @Override
    public void deleteFromFriends(long id, long friendId) {
        String deleteFriendQuery = "DELETE FROM friends " +
                "WHERE following_user_id = ? AND followed_user_id = ?;";
        jdbcTemplate.update(deleteFriendQuery, id, friendId);
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

    @Override
    public boolean friendIsAdded(long id, long friendId) {
        String ifRelationAlreadyExistsQuery = "SELECT COUNT(*) FROM friends " +
                "WHERE following_user_id = ? AND followed_user_id = ? ;";
        Integer count = jdbcTemplate.queryForObject(ifRelationAlreadyExistsQuery,
                Integer.class,
                id, friendId);

        return count > 0;
    }
}