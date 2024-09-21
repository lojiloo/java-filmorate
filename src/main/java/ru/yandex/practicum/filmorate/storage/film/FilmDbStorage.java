package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InvalidRequestException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("DbFilms")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    @Qualifier("DbUsers")
    private final UserStorage userStorage;
    private long id;

    @Override
    public Film addNewFilm(Film film) {
        validate(film);

        setId();
        film.setId(++id);
        String queryFilms = "INSERT INTO films (film_id, name, description, releaseDate, duration) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(queryFilms,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());
        checkGenres(film);
        checkMpa(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validate(film);

        long id = film.getId();
        String queryFilms = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ? WHERE films.film_id = ?";

        jdbcTemplate.update(queryFilms,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                id);
        checkGenres(film);
        checkMpa(film);

        return film;
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
    public Film like(long id, long userId) {
        Film film = getFilm(id);
        if (isFilmAlreadyLikedByUser(id, userId)) {
            return film;
        }
        String query = "INSERT INTO likes (film_id, user_id, created) VALUES (?, ?, ?);";
        jdbcTemplate.update(query, id, userId, new Timestamp(System.currentTimeMillis()));
        film.getUsersLiked().add(userId);

        return film;
    }

    @Override
    public Film dislike(long id, long userId) {
        Film film = getFilm(id);
        if (isFilmAlreadyLikedByUser(id, userId)) {
            String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
            jdbcTemplate.update(query, id, userId);
            film.getUsersLiked().remove(userId);
            return film;
        }
        return film;
    }

    @Override
    public List<Film> topByLikes(int count) {
        String query = "SELECT film_id FROM likes " +
                "GROUP BY film_id " +
                "ORDER BY COUNT(user_id) DESC " +
                "LIMIT ?;";
        List<Long> filmsIdByLike = jdbcTemplate.queryForList(query, Long.class, count);

        List<Film> topFilms = new ArrayList<>();
        for (Long id : filmsIdByLike) {
            topFilms.add(getFilm(id));
        }
        return topFilms;
    }

    @Override
    public boolean contains(long id) {
        String query = "SELECT COUNT(*) FROM films WHERE film_id = ?;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);

        return count > 0;
    }

    private boolean containsGenreInfo(long id) {
        String query = "SELECT COUNT(*) FROM film_genres WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);

        return count > 0;
    }

    private boolean containsMpaInfo(long id) {
        String query = "SELECT COUNT(*) FROM films WHERE film_id = ? AND mpa IS NOT NULL;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);

        return count > 0;
    }

    private boolean isFilmAlreadyLikedByUser(long id, long userId) {
        String query = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?;";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id, userId);

        return count > 0;
    }

    private void checkGenres(Film film) {
        List<Genre> genres = film.getGenres();
        Set<Integer> genresId = new HashSet<>();
        for (Genre g : genres) {
            genresId.add(g.getId());
        }

        long filmId = film.getId();

        if (containsGenreInfo(filmId)) {
            String queryDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?;";
            jdbcTemplate.update(queryDeleteGenres, filmId);
        }

        String queryAddGenres = "INSERT INTO film_genres (film_id, genre_id, last_update) " +
                "VALUES (?, ?, ?);";
        for (int genre : genresId) {
            jdbcTemplate.update(queryAddGenres,
                    filmId,
                    genre,
                    new Timestamp(System.currentTimeMillis()));
        }
    }

    private void checkMpa(Film film) {
        if (film.getMpa() == null) {
            return;
        }

        String query = "SELECT * FROM mpa WHERE mpa_id = ?;";
        Mpa mpa = jdbcTemplate.queryForObject(query, (rs, rn) -> Mpa.builder()
                        .id(rs.getInt("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .build(),
                film.getMpa().getId());
        film.setMpa(mpa);

        String querySetMpa = "UPDATE films SET mpa = ? WHERE film_id = ? ;";
        jdbcTemplate.update(querySetMpa, film.getMpa().getId(), film.getId());
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        long filmId = resultSet.getLong("film_id");

        Film film = Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .build();

        String queryLikes = "SELECT user_id FROM likes WHERE film_id = ?;";
        List<Long> userLiked = jdbcTemplate.queryForList(queryLikes, Long.class, filmId);
        film.getUsersLiked().addAll(userLiked);

        if (containsGenreInfo(filmId)) {
            String queryGenres = "SELECT g.genre_id, g.genre_name FROM film_genres AS fg " +
                    "JOIN genres AS g ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ?;";
            List<Genre> genres = jdbcTemplate.query(queryGenres, (rs, rn) -> Genre.builder()
                            .id(rs.getInt("genre_id"))
                            .name(rs.getString("genre_name"))
                            .build(), filmId);

            film.getGenres().addAll(genres);
        }

        if (containsMpaInfo(filmId)) {
            String queryMpa = "SELECT mpa_id, mpa_name FROM films JOIN mpa ON mpa = mpa_id WHERE film_id = ?;";
            Mpa mpa = jdbcTemplate.queryForObject(queryMpa, (rs, rn) -> Mpa.builder()
                            .id(rs.getInt("mpa_id"))
                            .name(rs.getString("mpa_name"))
                            .build(), filmId);

            film.setMpa(mpa);
        }

        return film;
    }

    private void setId() {
        if (contains(1)) {
            String query = "SELECT max(film_id) FROM films ;";
            this.id = jdbcTemplate.queryForObject(query, Long.class);
        }
    }

    private void validate(Film film) {
        if (film.getGenres().size() > 0) {
            List<Genre> genres = film.getGenres();
            String queryValidateGenre = "SELECT COUNT(*) FROM genres WHERE genre_id = ?;";
            for (Genre genre : genres) {
                int genreId = genre.getId();
                Integer genresCount = jdbcTemplate.queryForObject(queryValidateGenre, Integer.class, genreId);

                if (genresCount == 0) {
                    throw new InvalidRequestException("Данному id не соответствует ни одно наименование жанра");
                }
            }
        }

        if (film.getMpa() != null) {
            String queryValidateMpa = "SELECT COUNT(*) FROM mpa WHERE mpa_id = ?;";
            Integer mpaCount = jdbcTemplate.queryForObject(queryValidateMpa, Integer.class, film.getMpa().getId());

            if (mpaCount == 0) {
                throw new InvalidRequestException("Данному id не соответствует ни одно наименование рейтинга MPA");
            }
        }
    }
}
