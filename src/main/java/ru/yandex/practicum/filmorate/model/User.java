package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    private final Set<Long> friends = new HashSet<>();
    private Long id;
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
