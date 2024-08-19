package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 0;

    @PostMapping
    public Film addNewFilm(@Valid @RequestBody Film film) {

        if (film.getId() != null) {
            log.warn("У фильма, не добавленного в сервис, обнаружен id: {}", film.getName());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        film.setId(++id);
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм с id {} успешно изменён", film.getId());
            return film;
        }
        log.warn("Фильм с id {} не найден", film.getId());
        throw new NotFoundException("Фильм с данным id не найден");
    }

    @GetMapping
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }
}
