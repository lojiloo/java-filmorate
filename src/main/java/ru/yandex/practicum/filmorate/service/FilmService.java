package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    @Qualifier("dbFilms")
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    public Film addNewFilm(Film film) {
        if (film.getId() != null) {
            log.warn("У фильма, не добавленного в сервис, обнаружен id: {}", film.getName());
            throw new InvalidRequestException("id не может быть введен вручную");
        } else {
            film.setId(filmStorage.getNextId());
            filmStorage.addNewFilm(film);
        }

        if (film.getGenres().size() > 0) {
            updateGenres(film);
        }

        if (film.getMpa() != null) {
            updateMpa(film);
        }
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new InvalidRequestException("Фильм не найден, поскольку не указан id");
        } else if (!filmStorage.contains(film.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }

        if (film.getGenres().size() > 0) {
            updateGenres(film);
        } else {
            deleteGenres(film);
        }

        if (film.getMpa() != null) {
            updateMpa(film);
        } else {
            deleteMpa(film);
        }

        filmStorage.updateFilm(film);
        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        for (Film film : films) {
            getFilmGenres(film);
            updateMpa(film);
            updateLikes(film);
        }

        return films;
    }

    public Film getFilm(long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("Фильм с таким id не найден");
        }
        Film film = filmStorage.getFilm(id);
        getFilmGenres(film);
        updateMpa(film);
        updateLikes(film);

        return film;
    }

    public Film like(long id, long userId) {
        if (!filmStorage.contains(id)) {
            log.warn("При добавлении лайка произошла ошибка в поиске фильма по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userService.contains(userId)) {
            log.warn("При добавлении лайка произошла ошибка в поиске пользователя по id: {}", userId);
            throw new NotFoundException("Недействительный id");
        }

        Film film = filmStorage.getFilm(id);
        if (!filmStorage.isFilmAlreadyLikedByUser(id, userId)) {
            film.getUsersLiked().add(userId);
            filmStorage.like(id, userId);
        }
        return film;
    }

    public Film dislike(long id, long userId) {
        if (!filmStorage.contains(id)) {
            log.warn("При удалении лайка произошла ошибка в поиске фильма по id: {}", id);
            throw new NotFoundException("Недействительный id");
        }
        if (!userService.contains(userId)) {
            log.warn("При удалении лайка произошла ошибка в поиске пользователя по id: {}", userId);
            throw new NotFoundException("Недействительный id");
        }

        Film film = getFilm(id);
        if (filmStorage.isFilmAlreadyLikedByUser(id, userId)) {
            film.getUsersLiked().remove(userId);
            filmStorage.dislike(id, userId);
        }
        return film;
    }

    public List<Film> topByLikes(int count) {
        List<Film> top = filmStorage.topByLikes(count);

        for (Film film : top) {
            film.getUsersLiked().addAll(filmStorage.usersLikedFilm(film.getId()));
        }
        return top;
    }

    private void getFilmGenres(Film film) {
        List<Genre> genres = genreService.getFilmGenres(film.getId());
        film.getGenres().addAll(genres);
    }

    private void updateGenres(Film film) {
        List<Genre> genres = film.getGenres();
        Set<Integer> ids = new HashSet<>();
        for (Genre genre : genres) {
            ids.add(genre.getId());
        }
        if (!genreService.containsAll(ids)) {
            throw new InvalidRequestException("Данному id не соответствует ни одно наименование жанра");
        }
        genreService.updateGenres(film.getId(), ids);
    }

    private void updateMpa(Film film) {
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            if (!mpaService.contains(mpaId)) {
                throw new InvalidRequestException("Данному id не соответствует ни одно наименование рейтинга MPA");
            }
            film.setMpa(mpaService.getMpaById(mpaId));
            mpaService.updateMpa(film.getId(), mpaId);
        }
    }

    private void deleteMpa(Film film) {
        mpaService.deleteMpa(film.getId());
    }

    private void deleteGenres(Film film) {
        genreService.deleteGenres(film.getId());
    }

    private void updateLikes(Film film) {
        film.getUsersLiked().addAll(filmStorage.usersLikedFilm(film.getId()));
    }
}
