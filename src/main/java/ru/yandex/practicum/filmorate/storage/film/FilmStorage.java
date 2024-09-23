package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    void addNewFilm(Film film);

    void updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(long id);

    void like(long id, long userId);

    boolean isFilmAlreadyLikedByUser(long id, long userId);

    List<Long> usersLikedFilm(Long filmId);

    void dislike(long id, long userId);

    List<Film> topByLikes(int count);

    boolean contains(long id);

    long getNextId();
}
