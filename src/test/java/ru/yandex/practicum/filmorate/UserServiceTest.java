package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    UserService userService = new UserService(new InMemoryUserStorage());
    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void addNewUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        userService.createNewUser(user);

        assertEquals(1, userService.getUsers().size(),
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

        assertThrows(InvalidRequestException.class, () -> userService.createNewUser(user),
                "id был введён вручную");
    }

    @Test
    public void addNoEmailUserTest() {
        User user = User.builder()
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "Пользователь без email прошёл валидацию");
    }

    @Test
    public void addInvalidEmailUserTest() {
        User user = User.builder()
                .email("tyuuiijkj")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "Пользователь с некорректным email прошёл валидацию");
    }

    @Test
    public void addNoLoginUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "Пользователь без логина прошёл валидацию");
    }

    @Test
    public void addInvalidLoginUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("loji loo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "Пользователь с логином, содержащим пробел(ы), прошёл валидацию");
    }

    @Test
    public void loginIsUsedIfNameIsEmptyTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        userService.createNewUser(user);

        assertEquals("lojiloo", userService.getUsers().getFirst().getName(),
                "Для пользователя без имени в переменную имени не был записан логин");
    }

    @Test
    public void birthdayIsNotAFutureEvent() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .birthday(LocalDate.of(2900, Month.AUGUST, 4))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "День рождения из будущего прошёл валидацию");
    }

    @Test
    public void updateUserTest() {
        User user = User.builder()
                .email("mail@mail.com")
                .login("lojiloo")
                .name("io ammit")
                .birthday(LocalDate.of(1900, Month.AUGUST, 4))
                .build();
        userService.createNewUser(user);

        User user2 = user.toBuilder()
                .id(1L)
                .login("oolijol")
                .build();
        userService.updateUser(user2);

        assertEquals(1, userService.getUsers().size(),
                "При обновлении пользователя список увеличился, хотя ожидалось сохранение его размера");
        assertEquals("oolijol", userService.getUsers().getFirst().getLogin(),
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
        userService.createNewUser(user);

        User user2 = user.toBuilder()
                .id(10L)
                .login("oolijol")
                .build();

        assertThrows(NotFoundException.class, () -> userService.updateUser(user2),
                "Попытка передать несуществующего пользователя не вызвала исключения");
    }
}
