package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addNewFilm(Film film) {
        return filmStorage.addNewFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    public Film like(long id, long userId) {
        if (!filmStorage.contains(id)) {
            log.warn("При добавлении лайка произошла ошибка в поиске фильма по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userStorage.contains(userId)) {
            log.warn("При добавлении лайка произошла ошибка в поиске пользователя по id: {}", userId);
            throw new NotFoundException("Недействительный id");
        }

        Film film = filmStorage.getFilm(id);
        film.getUsersLiked().add(userStorage.getUser(userId));

        log.info("Пользователь {} поставил лайк фильму {}",
                userStorage.getUser(userId).getLogin(),
                film.getName());
        return film;
    }

    public Film dislike(long id, long userId) {
        if (!filmStorage.contains(id)) {
            log.warn("При удалении лайка произошла ошибка в поиске фильма по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userStorage.contains(userId)) {
            log.warn("При удалении лайка произошла ошибка в поиске пользователя по id: {}", userId);
            throw new NotFoundException("Недействительный id");
        }

        Film film = filmStorage.getFilm(id);
        film.getUsersLiked().remove(userStorage.getUser(userId));
        log.info("Пользователь {} успешно убрал лайк с фильма {}",
                userStorage.getUser(userId).getLogin(),
                film.getName());
        return film;
    }

    public List<Film> topByLikes(int count) {
        return filmStorage.topByLikes(count);
    }
}
