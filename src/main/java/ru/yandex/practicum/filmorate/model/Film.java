package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateValidation;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;
    private Set<User> usersLiked;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @DateValidation
    private LocalDate releaseDate;
    @Positive
    private int duration;

    //я добавила этот метод, потому что навернулся 1 тест постмана; я не понимаю, каким образом в тесте был вызван
    // геттер для usersLiked так, что он вернул null, -- в UserStorage у меня каждому новому пользователю присваивается
    // пустое множество в это поле... я не смогла разгадать эту тайну и решила пожертвовать меньшей кровью -_-
    public Set<User> getUsersLiked() {
        if (usersLiked == null) {
            return new HashSet<>();
        }
        return usersLiked;
    }
}
