package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addNewFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(long id);

    Film like(long id, long userId);

    Film dislike(long id, long userId);

    List<Film> topByLikes(int count);

    boolean contains(long id);

}
