package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @PostMapping("/films")
    public Film addNewFilm(@Valid @RequestBody Film film) {
        return filmStorage.addNewFilm(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    @GetMapping(value = {"/films", "/films/{id}"})
    public List<Film> getFilms(@PathVariable(required = false) String id) {
        if (id != null) {
            return filmStorage.getFilms(Long.parseLong(id));
        }
        return filmStorage.getFilms();
    }

    @GetMapping("/films/popular")
    public List<Film> topByLikes(@RequestParam(defaultValue = "10") int count) {
        return filmService.topByLikes(count);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public Film like(@PathVariable long id,
                     @PathVariable long userId) {
        return filmService.like(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public Film dislike(@PathVariable long id,
                        @PathVariable long userId) {
        return filmService.dislike(id, userId);
    }

}
