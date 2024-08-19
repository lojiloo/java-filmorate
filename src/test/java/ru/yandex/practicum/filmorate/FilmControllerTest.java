package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmService filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
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
    public void addNewFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        filmService.addNewFilm(film);

        assertEquals(1, filmService.getFilms().size(),
                "Новый фильм не был добавлен; список фильмов пуст");
    }

    @Test
    public void addNewFilmWithIdTest() {
        Film film = Film.builder()
                .id(2L)
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();

        assertThrows(InvalidRequestException.class, () -> filmService.addNewFilm(film),
                "id был введён вручную");
    }

    @Test
    public void addNoNameFilmTest() {
        Film film = Film.builder()
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм без названия прошёл валидацию");
    }

    @Test
    public void addVeryLongDescriptionFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description".repeat(1000))
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),
                "Описание, превышающее 200 символов, прошло валидацию");
    }

    @Test
    public void addFilmWithInvalidReleaseTimeTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(1500, Month.AUGUST, 1))
                .duration(50)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),
                "Фильм с датой релиза ранее 1985-12-28 прошёл валидацию");
    }

    @Test
    public void updateFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        filmService.addNewFilm(film);

        Film film2 = film.toBuilder()
                .id(film.getId())
                .description("super")
                .build();
        filmService.updateFilm(film2);

        assertEquals(1, filmService.getFilms().size(),
                "При обновлении фильма список увеличился, хотя ожидалось сохранение его размера");
        assertEquals("super", filmService.getFilms().getFirst().getDescription(),
                "При обновлении фильма новая версия не сохранилась");
    }

    @Test
    public void tryToUpdateNonexistentFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        filmService.addNewFilm(film);

        Film film2 = film.toBuilder()
                .id(film.getId() + 1)
                .build();

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film2),
                "Попытка передать несуществующий фильм не вызвала исключения");
    }
}
