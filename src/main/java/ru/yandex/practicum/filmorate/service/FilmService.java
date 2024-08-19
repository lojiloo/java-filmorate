package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
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

    public List<Film> getFilms(long id) {
        return filmStorage.getFilms(id);
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

        Film film = filmStorage.getFilms(id).getFirst();
        film.getUsersLiked().add(userStorage.getUsers(userId).getFirst());

        log.info("Пользователь {} поставил лайк фильму {}",
                userStorage.getUsers(userId).getFirst().getLogin(),
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

        Film film = filmStorage.getFilms(id).getFirst();
        film.getUsersLiked().remove(userStorage.getUsers(userId).getFirst());
        log.info("Пользователь {} успешно убрал лайк с фильма {}",
                userStorage.getUsers(userId).getFirst().getLogin(),
                film.getName());
        return film;
    }

    public List<Film> topByLikes(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(film -> film.getUsersLiked().size(), Comparator.reverseOrder()))
                .limit(count)
                .toList();
    }
}
