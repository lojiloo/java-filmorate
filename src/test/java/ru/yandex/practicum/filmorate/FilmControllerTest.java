package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FilmControllerTest {
    FilmController controller = new FilmController();

    @Test
    public void addNewFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        controller.addNewFilm(film);

        assertEquals(1, controller.getFilms().size(),
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

        assertThrows(InvalidRequestException.class, () -> controller.addNewFilm(film),
                "id был введён вручную");
    }

    @Test
    public void addNoNameFilmTest() {
        Film film = Film.builder()
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.addNewFilm(film),
                "Попытка передать пустое имя не вызвала исключения");
    }

    @Test
    public void addVeryLongDescriptionFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description".repeat(1000))
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.addNewFilm(film),
                "Попытка передать описание, превышающее допустимую длину, не вызвала исключения");
    }

    @Test
    public void addFilmWithInvalidReleaseTimeTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(1500, Month.AUGUST, 1))
                .duration(50)
                .build();

        assertThrows(InvalidRequestException.class, () -> controller.addNewFilm(film),
                "Попытка перердать фильм, снятый до появления кинематографа, не вызвала исключения");
    }

    @Test
    public void updateFilmTest() {
        Film film = Film.builder()
                .name("film_name")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 1))
                .duration(50)
                .build();
        controller.addNewFilm(film);

        Film film2 = film.toBuilder()
                .id(1L)
                .description("super")
                .build();
        controller.updateFilm(film2);

        assertEquals(1, controller.getFilms().size(),
                "При обновлении фильма список увеличился, хотя ожидалось сохранение его размера");
        assertEquals("super", controller.getFilms().getFirst().getDescription(),
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
        controller.addNewFilm(film);

        Film film2 = film.toBuilder()
                .id(2L)
                .build();

        assertThrows(NotFoundException.class, () -> controller.updateFilm(film2),
                "Попытка передать несуществующий фильм не вызвала исключения");
    }
}
