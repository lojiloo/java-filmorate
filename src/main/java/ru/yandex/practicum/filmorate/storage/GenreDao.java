package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDao {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenres() {
        String query = "SELECT * FROM genres;";
        return jdbcTemplate.query(query, this::mapRowToGenre);
    }

    public Genre getGenreById(int id) {
        String query = "SELECT * FROM genres WHERE genre_id = ? ;";
        return jdbcTemplate.queryForObject(query, this::mapRowToGenre, id);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre_name"))
                .build();
    }

    public boolean contains(int id) {
        String query = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count > 0;
    }

    public boolean containsAll(Set<Integer> ids) {
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String query = String.format("SELECT COUNT(*) FROM genres WHERE genre_id IN (%s)", inSql);

        Integer count = jdbcTemplate.queryForObject(query, Integer.class, ids.toArray());
        return count == ids.size();
    }

    public List<Genre> getFilmGenres(long filmId) {
        String findGenresForFilm = "SELECT g.genre_id, g.genre_name FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE film_id = ?;";
        return jdbcTemplate.query(findGenresForFilm, this::mapRowToGenre, filmId);
    }

    public void updateGenres(long filmId, Set<Integer> ids) {
        String queryDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?;";
        jdbcTemplate.update(queryDeleteGenres, filmId);

        String queryAddGenre = "INSERT INTO film_genres (film_id, genre_id, last_update) " +
                "VALUES (?, ?, ?) ;";
        for (int genre : ids) {
            jdbcTemplate.update(queryAddGenre,
                    filmId,
                    genre,
                    new Timestamp(System.currentTimeMillis()));
        }
    }

    public void deleteGenres(long filmId) {
        String queryDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?;";
        jdbcTemplate.update(queryDeleteGenres, filmId);
    }
}
