package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/test-data.sql"})
public class DbFilmsIntegrationTests {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    public void setUp() {
        this.userStorage = new UserDbStorage(jdbcTemplate);
        this.filmStorage = new FilmDbStorage(jdbcTemplate, userStorage);
    }

    @Test
    public void testCreateFilm() {
        Film film = Film.builder()
                .name("aaa")
                .description("bbb")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 2))
                .duration(120)
                .build();
        filmStorage.addNewFilm(film);
        assertEquals(filmStorage.getFilm(4), film,
                "Фильм не был добавлен в БД");
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.getFilm(1);
        film.setName("TEST");
        filmStorage.updateFilm(film);
        assertEquals("TEST", filmStorage.getFilm(1).getName(),
                "Название фильма в базе данных не было изменено или изменено неадекватно запросу");
    }

    @Test
    public void testGetFilmById() {
        Film film = filmStorage.getFilm(1);
        assertEquals("nice film", film.getName(),
                "Метод вернул название фильма, не соответствующее фильму, переданному по id");
    }

    @Test
    public void testGetFilms() {
        List<Film> films = filmStorage.getFilms();
        assertEquals(3, films.size(),
                "Размер таблицы в базе данных не соответствует реальному числу фильмов");
    }

    @Test
    public void testLike() {
        filmStorage.like(1, 2);
        assertTrue(filmStorage.getFilm(1).getUsersLiked().contains(2L),
                "Лайк не был поставлен фильму");
    }

    @Test
    public void testDislike() {
        filmStorage.like(1, 2);
        filmStorage.dislike(1, 2);
        assertFalse(filmStorage.getFilm(1).getUsersLiked().contains(2L),
                "Лайк не был снят");
    }

    @Test
    public void testGetTopByLikes() {
        filmStorage.like(1, 1);
        filmStorage.like(1, 2);
        filmStorage.like(1, 3);
        filmStorage.like(1, 2);
        filmStorage.like(3, 1);
        filmStorage.like(3, 2);
        filmStorage.like(3, 4);
        filmStorage.like(2, 1);

        assertEquals(2, filmStorage.topByLikes(2).size(),
                "Ограничение на количество выводимых фильмов за раз не вернуло ожидаемый результат");
        assertEquals(filmStorage.getFilm(1), filmStorage.topByLikes(3).getFirst(),
                "Запрос к базе не смог определить самый популярный фильм");
        assertEquals(filmStorage.getFilm(2), filmStorage.topByLikes(3).getLast(),
                "Запрос к базе не смог определить самый непопулярный фильм");
    }
}
