package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryFilms")
@RequiredArgsConstructor
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 0;
    @Qualifier("InMemoryUsers")
    private UserStorage userStorage;

    @Override
    public Film addNewFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film.getName());

        return film;
    }

    @Override
    public void updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм с id {} успешно изменён", film.getId());
        }
        log.warn("Фильм с id {} не найден", film.getId());
        throw new NotFoundException("Фильм с данным id не найден");
    }

    @Override
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public Film getFilm(long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        log.warn("Фильм с id {} не найден", id);
        throw new NotFoundException("Фильм с данным id не найден");
    }

    @Override
    public void like(long id, long userId) {
        Film film = getFilm(id);
        film.getUsersLiked().add(userId);

        log.info("Пользователь {} поставил лайк фильму {}",
                userStorage.getUser(userId).getLogin(),
                film.getName());
    }

    @Override
    public boolean isFilmAlreadyLikedByUser(long id, long userId) {
        return films.get(id).getUsersLiked().contains(userId);
    }

    @Override
    public List<Long> usersLikedFilm(Long filmId) {
        return films.get(filmId).getUsersLiked();
    }

    @Override
    public void dislike(long id, long userId) {
        Film film = getFilm(id);
        film.getUsersLiked().remove(userStorage.getUser(userId));
        log.info("Пользователь {} успешно убрал лайк с фильма {}",
                userStorage.getUser(userId).getLogin(),
                film.getName());
    }

    @Override
    public List<Film> topByLikes(int count) {
        return getFilms().stream()
                .sorted(Comparator.comparing(film -> film.getUsersLiked().size(), Comparator.reverseOrder()))
                .limit(count)
                .toList();
    }

    public boolean contains(long filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public long getNextId() {
        return ++id;
    }
}
