package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    private Long id;
    private Set<Long> friends;
    @NotEmpty
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "[^ ]*$")
    private String login;
    private String name;
    @PastOrPresent
    @NotNull
    private LocalDate birthday;
}
