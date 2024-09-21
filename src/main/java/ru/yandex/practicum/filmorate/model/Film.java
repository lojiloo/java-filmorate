package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateValidation;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film implements Serializable {
    private final Set<Long> usersLiked = new HashSet<>();
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
