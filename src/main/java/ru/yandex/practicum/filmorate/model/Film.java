package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateValidation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Film {
    private final List<Long> usersLiked = new ArrayList<>();
    private final List<Genre> genres = new ArrayList<>();
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @DateValidation
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Mpa mpa;
}
