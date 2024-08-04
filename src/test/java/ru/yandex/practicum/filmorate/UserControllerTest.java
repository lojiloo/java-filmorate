package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserControllerTest {
    UserController controller = new UserController();

    @Test
    public void addNewUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        controller.createNewUser(user);

        assertEquals(1, controller.getUsers().size(),
                "Новый пользователь не был добавлен; список пользователей пуст");
    }

    @Test
    public void addNewUserWithIdTest() {
        User user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "id был введён вручную");
    }

    @Test
    public void addNoEmailUserTest() {
        User user = User.builder()
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "Попытка передать пользователя без email не вызвала исключения");
    }

    @Test
    public void addInvalidEmailUserTest() {
        User user = User.builder()
                .email("tyuuiijkj")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "Попытка передать пользователя с некорректным email не вызвала исключения");
    }

    @Test
    public void addNoLoginUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "Попытка передать пользователя без логина не вызвала исключения");
    }

    @Test
    public void addInvalidLoginUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("loji loo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "Попытка передать пользователя с некорректным логином не вызвала исключения");
    }

    @Test
    public void loginIsUsedIfNameIsEmptyTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        controller.createNewUser(user);

        assertEquals("lojiloo", controller.getUsers().getFirst().getName(),
                "Для пользователя без имени в переменную имени не был записан логин");
    }

    @Test
    public void birthdayIsNotAFutureEvent() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .birthday(LocalDate.of(2900, Month.AUGUST, 4))
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.createNewUser(user),
                "Попытка передать пользователя с датой рождения, старше текущего момента, не вызвала исключения");
    }

    @Test
    public void UpdateUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        controller.createNewUser(user);

        User user2 = user.toBuilder()
                .id(1L)
                .login("oolijol")
                .build();
        controller.updateUser(user2);

        assertEquals(1, controller.getUsers().size(),
                "При обновлении пользователя список увеличился, хотя ожидалось сохранение его размера");
        assertEquals("oolijol", controller.getUsers().getFirst().getLogin(),
                "При обновлении пользователя новая версия не сохранилась");
    }

    @Test
    public void tryToUpdateNonexistentUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        controller.createNewUser(user);

        User user2 = user.toBuilder()
                .id(10L)
                .login("oolijol")
                .build();

        assertThrows(NotFoundException.class, () -> controller.updateUser(user2),
                "Попытка передать несуществующего пользователя не вызвала исключения");
    }
}
