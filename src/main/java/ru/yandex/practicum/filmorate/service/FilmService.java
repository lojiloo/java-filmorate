package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FilmService {
    @Qualifier("DbFilms")
    private final FilmStorage filmStorage;
    @Qualifier("DbUsers")
    private final UserStorage userStorage;

    public Film addNewFilm(Film film) {
        if (film.getId() != null) {
            log.warn("У фильма, не добавленного в сервис, обнаружен id: {}", film.getName());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        return filmStorage.addNewFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new InvalidRequestException("Фильм не найден, поскольку не указан id");
        } else if (!filmStorage.contains(film.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("Фильм с таким id не найден");
        }
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

        return filmStorage.like(id, userId);
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

        return filmStorage.dislike(id, userId);
    }

    public List<Film> topByLikes(int count) {
        return filmStorage.topByLikes(count);
    }
}
