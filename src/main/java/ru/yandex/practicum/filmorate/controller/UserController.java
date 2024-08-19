package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @PostMapping("/users")
    public User createNewUser(@Valid @RequestBody User user) {
        return userStorage.createNewUser(user);
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        return userStorage.updateUser(user);
    }

    @GetMapping(value = {"/users", "/users/{id}"})
    public List<User> getUsers(@PathVariable Optional<Long> id) {
        if (id.isPresent()) {
            return userStorage.getUsers(id.get());
        }
        return userStorage.getUsers();
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getUserFriends(@PathVariable long id) {
        return userService.getUserFriends(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id,
                                       @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public User addNewFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        return userService.addNewFriend(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public User deleteFromFriends(@PathVariable Long id,
                                  @PathVariable Long friendId) {
        return userService.deleteFromFriends(id, friendId);
    }
}
