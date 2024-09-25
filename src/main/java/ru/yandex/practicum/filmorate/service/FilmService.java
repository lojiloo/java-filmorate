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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            film = filmStorage.addNewFilm(film);
        }

        updateGenres(film);
        setMpa(film);

        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new InvalidRequestException("Фильм не найден, поскольку не указан id");
        } else if (!filmStorage.contains(film.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }

        updateGenres(film);
        setMpa(film);
        filmStorage.updateFilm(film);

        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        for (Film film : films) {
            setGenres(film);
            setMpa(film);
            setLikes(film);
        }

        return films;
    }

    public Film getFilm(long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("Фильм с таким id не найден");
        }
        Film film = filmStorage.getFilm(id);
        setGenres(film);
        setMpa(film);
        setLikes(film);

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

        Film film = getFilm(id);
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

        List<Long> ids = new ArrayList<>();
        for (Film film : top) {
            ids.add(film.getId());
        }

        Map<Long, List<Long>> likesPerFilms = filmStorage.usersLikedFilms(ids);
        for (int i = 0; i < top.size(); i++) {
            if (likesPerFilms.containsKey(top.get(i).getId())) {
                top.get(i).getUsersLiked().addAll(likesPerFilms.get(top.get(i).getId()));
            }
        }
        return top;
    }

    private void updateGenres(Film film) {
        List<Genre> genres = film.getGenres();
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        if (!genreService.existAll(ids)) {
            throw new InvalidRequestException("Данному id не соответствует ни одно наименование жанра");
        }
        genreService.updateGenres(film.getId(), ids);
    }

    private void setGenres(Film film) {
        List<Genre> genres = genreService.getFilmGenres(film.getId());
        film.getGenres().addAll(genres);
    }

    private void setMpa(Film film) {
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            if (!mpaService.exists(mpaId)) {
                throw new InvalidRequestException("Данному id не соответствует ни одно наименование рейтинга MPA");
            }
            film.setMpa(mpaService.getMpaById(mpaId));
            mpaService.updateMpa(film.getId(), mpaId);
        }
    }

    private void setLikes(Film film) {
        film.getUsersLiked().addAll(filmStorage.usersLikedFilm(film.getId()));
    }
}
