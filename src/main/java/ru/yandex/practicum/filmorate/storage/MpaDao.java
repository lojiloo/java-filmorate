package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDao {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> getMpa() {
        String query = "SELECT * FROM mpa ;";
        return jdbcTemplate.query(query, this::mapRowToMpa);
    }

    public Mpa getMpaById(int id) {
        if (!contains(id)) {
            throw new NotFoundException("Данному id не соответствует ни один MPA");
        }
        String query = "SELECT * FROM mpa WHERE mpa_id = ?";
        return jdbcTemplate.queryForObject(query, this::mapRowToMpa, id);
    }

    private Mpa mapRowToMpa(ResultSet resultSet, int numRow) throws SQLException {
        return Mpa.builder()
                .id(resultSet.getInt("mpa_id"))
                .name(resultSet.getString("mpa_name"))
                .build();
    }

    public boolean contains(int id) {
        String query = "SELECT COUNT(*) FROM mpa WHERE mpa_id = ?;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);

        return count > 0;
    }

    public void updateMpa(long filmId, int mpaId) {
        String queryUpdate = "UPDATE films SET mpa = ? WHERE film_id = ? ;";
        jdbcTemplate.update(queryUpdate, mpaId, filmId);
    }

    public void deleteMpa(long filmId) {
        String queryDelete = "UPDATE films SET mpa = NULL WHERE film_id = ? ;";
        jdbcTemplate.update(queryDelete, filmId);
    }
}
