package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {

    Film addNewFilm(Film film);

    void updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(long id);

    void like(long id, long userId);

    boolean isFilmAlreadyLikedByUser(long id, long userId);

    Map<Long, List<Long>> usersLikedFilms(List<Long> filmsIds);

    List<Long> usersLikedFilm(Long filmId);

    void dislike(long id, long userId);

    List<Film> topByLikes(int count);

    boolean contains(long id);
}
