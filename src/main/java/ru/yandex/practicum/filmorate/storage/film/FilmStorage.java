package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addNewFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(long id);

    List<Film> topByLikes(int count);

    boolean contains(long id);

}
