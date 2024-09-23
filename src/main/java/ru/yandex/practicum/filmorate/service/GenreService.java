package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDao;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDao genreDao;

    public List<Genre> getGenres() {
        return genreDao.getGenres();
    }

    public Genre getGenreById(int id) {
        if (!genreDao.contains(id)) {
            throw new NotFoundException("Жанра с данным id не существует");
        }
        return genreDao.getGenreById(id);
    }

    protected boolean containsAll(Set<Integer> ids) {
        return genreDao.containsAll(ids);
    }

    protected List<Genre> getFilmGenres(long filmId) {
        return genreDao.getFilmGenres(filmId);
    }

    protected void updateGenres(long filmId, Set<Integer> genresIds) {
        genreDao.updateGenres(filmId, genresIds);
    }

    protected void deleteGenres(long filmId) {
        genreDao.deleteGenres(filmId);
    }
}
