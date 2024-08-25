package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping("/films")
    public Film addNewFilm(@Valid @RequestBody Film film) {
        return filmService.addNewFilm(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/films/{id}")
    public Film getFilm(@PathVariable long id) {
        return filmService.getFilm(id);
    }

    @GetMapping("/films")
    public List<Film> getFilms() {
        return filmService.getFilms();
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
