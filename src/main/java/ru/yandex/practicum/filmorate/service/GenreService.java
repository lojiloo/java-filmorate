package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getGenres() {
        return genreStorage.getGenres();
    }

    public Genre getGenreById(int id) {
        if (!genreStorage.contains(id)) {
            throw new NotFoundException("Жанра с данным id не существует");
        }
        return genreStorage.getGenreById(id);
    }

    protected boolean existAll(Set<Integer> ids) {
        return genreStorage.containsAll(ids);
    }

    protected List<Genre> getFilmGenres(long filmId) {
        return genreStorage.getFilmGenres(filmId);
    }

    protected void updateGenres(long filmId, Set<Integer> genresIds) {
        genreStorage.updateGenres(filmId, genresIds);
    }
}
