package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUsersIntegrationTests {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    public void setUp() {
        this.userStorage = new UserDbStorage(jdbcTemplate);
        User user1 = User.builder()
                .login("jiloo")
                .name("puk")
                .email("hello@world.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userStorage.createNewUser(user1);

        User user2 = User.builder()
                .login("ljiloo")
                .name("pus")
                .email("he@wo.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userStorage.createNewUser(user2);
    }

    @Test
    public void testCreateUser() {
        User user = User.builder()
                .login("jijiloo")
                .name("hehe")
                .email("hehe@wow.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userStorage.createNewUser(user);
        assertEquals(userStorage.getUser(3), user,
                "Пользователь не был добавлен в БД");
    }

    @Test
    public void testGetUserById() {
        assertEquals("puk", userStorage.getUser(1).getName(),
                "Метод вернул имя пользователя, не соответствующее пользователю, переданному по id");
    }

    @Test
    public void testGetUsers() {
        List<User> users = userStorage.getUsers();
        assertEquals(2, users.size(),
                "Размер таблицы в базе данных не соответствует реальному числу пользователей");
    }

    @Test
    public void testUpdateUser() {
        User user = userStorage.getUser(1);
        user.setLogin("TEST");
        userStorage.updateUser(user);
        assertEquals("TEST", userStorage.getUser(1).getLogin(),
                "Логин пользователя в базе данных не был изменён или изменён неадекватно запросу");
    }

    @Test
    public void testFollowUser() {
        userStorage.addNewFriend(1, 2);

        String query = "SELECT followed_user_id FROM friends WHERE following_user_id = ? ;";
        assertEquals(2, jdbcTemplate.queryForObject(query, Long.class, 1),
                String.format("Пользователь %d не смог добавить пользователя %d в друзья", 1, 2));
    }

    @Test
    public void testAddFriend() {
        userStorage.addNewFriend(1, 2);
        userStorage.addNewFriend(2, 1);

        String query = "SELECT COUNT(*) FROM friends WHERE followed_user_id IN (?, ?) AND following_user_id IN (?, ?) ;";
        assertEquals(2, jdbcTemplate.queryForObject(query, Integer.class, 2, 1, 2, 1),
                String.format("Дружбе между %d и %d не был присвоен взаимный статус, хотя заявка была принята", 1, 2));
    }

    @Test
    public void getUserFriend() {
        User user = User.builder()
                .login("jijiloo")
                .name("hehe")
                .email("hehe@wow.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userStorage.createNewUser(user);
        userStorage.addNewFriend(1, 2);
        userStorage.addNewFriend(2, 1);
        userStorage.addNewFriend(1, 3);
        userStorage.addNewFriend(3, 1);

        assertEquals(2, userStorage.getUserFriends(1).size(),
                "В список друзей попали не все добавленные и принявшие заявку пользователи");
    }

    @Test
    public void getCommonFriend() {
        User user = User.builder()
                .login("jijiloo")
                .name("hehe")
                .email("hehe@wow.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userStorage.createNewUser(user);
        userStorage.addNewFriend(1, 2);
        userStorage.addNewFriend(2, 1);
        userStorage.addNewFriend(3, 2);
        userStorage.addNewFriend(2, 3);

        List<User> commonFriends = userStorage.getCommonFriends(1, 3);
        assertEquals(1, commonFriends.size(),
                "В списке общих друзей более одного пользователя");

        User commonFriend = commonFriends.getFirst();
        assertEquals(2, commonFriend.getId(),
                String.format("В список общих друзей попал пользователь с неверным id: %d", commonFriend.getId()));
    }

    @Test
    public void deleteFriend() {
        userStorage.addNewFriend(1, 2);
        userStorage.addNewFriend(2, 1);
        assertEquals(1, userStorage.getUserFriends(1).size(),
                "В списке друзей более одного пользователя (был добавлен один)");

        userStorage.deleteFromFriends(1, 2);
        assertEquals(0, userStorage.getUserFriends(1).size(),
                "Список друзей не пуст (были удалены все друзья)");
    }
}
