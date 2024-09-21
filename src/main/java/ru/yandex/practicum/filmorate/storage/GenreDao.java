package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDao {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenres() {
        String query = "SELECT * FROM genres;";
        return jdbcTemplate.query(query, this::mapRowToGenre);
    }

    public Genre getGenreById(int id) {
        if (!contains(id)) {
            throw new NotFoundException("Жанра с данным id не существует");
        }
        String query = "SELECT * FROM  genres WHERE genre_id = ? ;";
        return jdbcTemplate.queryForObject(query, this::mapRowToGenre, id);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre_name"))
                .build();
    }

    private boolean contains(int id) {
        String query = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count > 0;
    }
}
