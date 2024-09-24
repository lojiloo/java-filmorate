package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository("dbFilms")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addNewFilm(Film film) {
        film.setId(getNextId());
        String queryFilms = "INSERT INTO films (film_id, name, description, releaseDate, duration) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(queryFilms,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());
        return film;
    }

    @Override
    public void updateFilm(Film film) {
        long filmId = film.getId();

        if (film.getMpa() != null) {
            String queryFilms = "UPDATE films SET film_id = ?, name = ?, description = ?, releaseDate = ?, duration = ?, mpa = ? WHERE film_id = ?";
            jdbcTemplate.update(queryFilms,
                    filmId,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    filmId);
        } else {
            String queryFilms = "UPDATE films SET film_id = ?, name = ?, description = ?, releaseDate = ?, duration = ?, mpa = NULL WHERE film_id = ?";
            jdbcTemplate.update(queryFilms,
                    filmId,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    filmId);
        }
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT * FROM films;";
        return jdbcTemplate.query(query, this::mapRowToFilm);
    }

    @Override
    public Film getFilm(long id) {
        String query = "SELECT * FROM films WHERE film_id = ?";
        return jdbcTemplate.queryForObject(query, this::mapRowToFilm, id);
    }

    @Override
    public void like(long id, long userId) {
        String query = "INSERT INTO likes (film_id, user_id, created) VALUES (?, ?, ?);";
        jdbcTemplate.update(query, id, userId, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public void dislike(long id, long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
        jdbcTemplate.update(query, id, userId);
    }

    @Override
    public List<Film> topByLikes(int count) {
        String query = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, f.mpa, COUNT(user_id) AS count " +
                "FROM films AS f JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE l.film_id IN " +
                "(SELECT film_id FROM likes GROUP BY film_id LIMIT ?) " +
                "GROUP BY f.film_id " +
                "ORDER BY count DESC;";
        List<Film> topFilms = jdbcTemplate.query(query, this::mapRowToFilm, count);

        return topFilms;
    }

    @Override
    public boolean contains(long id) {
        String query = "SELECT COUNT(*) FROM films WHERE film_id = ?;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);

        return count > 0;
    }

    @Override
    public boolean isFilmAlreadyLikedByUser(long id, long userId) {
        String query = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id, userId);

        return count > 0;
    }

    @Override
    public List<Long> usersLikedFilm(Long filmId) {
        String queryLikes = "SELECT user_id FROM likes WHERE film_id = ?;";
        return jdbcTemplate.queryForList(queryLikes, Long.class, filmId);
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .build();
        if (resultSet.getInt("mpa") != 0) {
            Mpa mpa = Mpa.builder().id(resultSet.getInt("mpa")).build();
            film.setMpa(mpa);
        }
        return film;
    }

    @Override
    public long getNextId() {
        String query = "SELECT max(film_id) FROM films ;";
        Optional<Long> currentId = Optional.ofNullable(jdbcTemplate.queryForObject(query, Long.class));

        return currentId.map(id -> id + 1).orElse(1L);
    }
}
