package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 0;

    @Override
    public Film addNewFilm(Film film) {
        if (film.getId() != null) {
            log.warn("У фильма, не добавленного в сервис, обнаружен id: {}", film.getName());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        film.setId(++id);
        film.setUsersLiked(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм с id {} успешно изменён", film.getId());
            return film;
        }
        log.warn("Фильм с id {} не найден", film.getId());
        throw new NotFoundException("Фильм с данным id не найден");
    }

    @Override
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public List<Film> getFilms(long id) {
        if (films.containsKey(id)) {
            return List.of(films.get(id));
        }
        log.warn("Фильм с id {} не найден", id);
        throw new NotFoundException("Фильм с данным id не найден");
    }

    public boolean contains(long filmId) {
        return films.containsKey(filmId);
    }

}
