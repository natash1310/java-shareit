package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();

    User getUserById(Long userId);

    User add(User user);

    User update(User user);

    void delete(Long userId);
}
