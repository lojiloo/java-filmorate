package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateValidation;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @DateValidation
    private LocalDate releaseDate;
    @Positive
    private int duration;
}
