package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmsIntegrationTests {
    private final JdbcTemplate jdbcTemplate;
    private UserService userService;
    private FilmService filmService;

    @BeforeEach
    public void setUp() {
        this.userService = new UserService(new UserDbStorage(jdbcTemplate));
        this.filmService = new FilmService(new FilmDbStorage(jdbcTemplate),
                new UserService(new UserDbStorage(jdbcTemplate)),
                new GenreService(new GenreStorage(jdbcTemplate)),
                new MpaService(new MpaStorage(jdbcTemplate)));

        Film film1 = Film.builder()
                .name("aaa")
                .description("bbb")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 2))
                .duration(120)
                .build();
        filmService.addNewFilm(film1);

        Film film2 = Film.builder()
                .name("aa")
                .description("bb")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 2))
                .duration(120)
                .build();
        filmService.addNewFilm(film2);
    }

    @Test
    public void testCreateFilm() {
        Film film = Film.builder()
                .name("a")
                .description("b")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 2))
                .duration(120)
                .build();
        filmService.addNewFilm(film);

        assertEquals(filmService.getFilm(3), film,
                "Фильм не был добавлен в БД");
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmService.getFilm(1);
        film.setName("TEST");
        filmService.updateFilm(film);
        assertEquals("TEST", filmService.getFilm(1).getName(),
                "Название фильма в базе данных не было изменено или изменено неадекватно запросу");
    }

    @Test
    public void testGetFilmById() {
        Film film = filmService.getFilm(1);
        assertEquals("aaa", film.getName(),
                "Метод вернул название фильма, не соответствующее фильму, переданному по id");
    }

    @Test
    public void testGetFilms() {
        List<Film> films = filmService.getFilms();
        assertEquals(2, films.size(),
                "Размер таблицы в базе данных не соответствует реальному числу фильмов");
    }

    @Test
    public void testLike() {
        createUsers();
        filmService.like(1, 1);
        assertTrue(filmService.getFilm(1).getUsersLiked().contains(1L),
                "Лайк не был поставлен фильму");
    }

    @Test
    public void testDislike() {
        createUsers();
        filmService.like(1, 1);
        filmService.dislike(1, 1);
        assertFalse(filmService.getFilm(1).getUsersLiked().contains(1L),
                "Лайк не был снят");
    }

    @Test
    public void testGetTopByLikes() {
        createUsers();
        Film film = Film.builder()
                .name("a")
                .description("b")
                .releaseDate(LocalDate.of(2000, Month.AUGUST, 2))
                .duration(120)
                .build();
        filmService.addNewFilm(film);

        filmService.like(1, 1);
        filmService.like(1, 2);
        filmService.like(1, 3);
        filmService.like(1, 2);
        filmService.like(3, 1);
        filmService.like(3, 2);
        filmService.like(2, 1);

        assertEquals(2, filmService.topByLikes(2).size(),
                "Ограничение на количество выводимых фильмов за раз не вернуло ожидаемый результат");
        assertEquals(filmService.getFilm(1), filmService.topByLikes(3).getFirst(),
                "Запрос к базе не смог определить самый популярный фильм");
        assertEquals(filmService.getFilm(2), filmService.topByLikes(3).getLast(),
                "Запрос к базе не смог определить самый непопулярный фильм");
    }

    private void createUsers() {
        User user1 = User.builder()
                .login("jiloo")
                .name("puk")
                .email("hello@world.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userService.createNewUser(user1);

        User user2 = User.builder()
                .login("ljiloo")
                .name("pus")
                .email("he@wo.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userService.createNewUser(user2);

        User user3 = User.builder()
                .login("jijiloo")
                .name("hehe")
                .email("hehe@wow.com")
                .birthday(LocalDate.of(1909, Month.SEPTEMBER, 19))
                .build();
        userService.createNewUser(user3);
    }
}
