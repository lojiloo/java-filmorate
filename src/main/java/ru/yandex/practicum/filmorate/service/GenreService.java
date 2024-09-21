package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDao;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreService {
    private final GenreDao genreDao;

    public List<Genre> getGenres() {
        return genreDao.getGenres();
    }

    public Genre getGenreById(int id) {
        return genreDao.getGenreById(id);
    }
}
