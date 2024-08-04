package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);
    private static final LocalDate BDAY_OF_CINEMA = LocalDate.of(1895, Month.DECEMBER, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addNewFilm(@Valid @RequestBody final Film film) {
        validateFilm(film);

        if (film.getId() != null) {
            logger.warn("У фильма, не добавленного в сервис, обнаружен id: {}", film.getName());
            throw new InvalidRequestException("id не может быть введен вручную");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        logger.info("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody final Film film) {
        validateFilm(film);

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            logger.info("Фильм с id {} успешно изменён", film.getId());
            return film;
        }
        logger.warn("Фильм с id {} не найден", film.getId());
        throw new NotFoundException("Фильм с данным id не найден");
    }

    @GetMapping
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }

    private void validateFilm(final Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            logger.warn("Не передано название фильма");
            throw new InvalidRequestException("Имя не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            logger.warn("Передано слишком длинное описание");
            throw new InvalidRequestException("Описание не должно превышать 200 символов. Текущая длина: "
                    + film.getDescription().length());
        }
        if (film.getReleaseDate().isBefore(BDAY_OF_CINEMA)) {
            logger.warn("Дата релиза не может быть ранее 25.12.1895, передано: {}", film.getReleaseDate());
            throw new InvalidRequestException("Некорректная дата релиза: не может быть ранее 1895 года");
        }
        if (film.getDuration() <= 0) {
            logger.warn("Продолжительность фильма меньше или равна 0");
            throw new InvalidRequestException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
