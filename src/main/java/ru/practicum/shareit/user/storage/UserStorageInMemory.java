package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserStorageInMemory implements UserStorage {

    private final Map<Long, User> userStorage = new HashMap<>();
    private final Set<String> emailStorage = new HashSet<>();
    private long idCount = 1;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(userStorage.values());
    }

    @Override
    public User getUserById(Long userId) {
        if (userStorage.containsKey(userId)) {
            return userStorage.get(userId);
        } else {
            throw new NotFoundException("Пользователя с таким id не существует.");
        }
    }

    @Override
    public User add(User user) {
        emailCheck(user);
        user.setId(idCount++);
        userStorage.put(user.getId(), user);
        emailStorage.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        User repoUser = getUserById(user.getId());
        if (user.getEmail() != null) {
            if (user.getEmail().equals(repoUser.getEmail())) {
                return repoUser;
            }
            emailCheck(user);
            emailStorage.remove(repoUser.getEmail());
            repoUser.setEmail(user.getEmail());
            emailStorage.add(repoUser.getEmail());
        }
        if (user.getName() != null) {
            repoUser.setName(user.getName());
        }
        return repoUser;
    }

    @Override
    public Boolean delete(Long userId) {
        if (userStorage.containsKey(userId)) {
            emailStorage.remove(userStorage.get(userId).getEmail());
            userStorage.remove(userId);
        }
        return true;
    }

    private void emailCheck(User user) {
        if (emailStorage.contains(user.getEmail())) {
            throw new EmailException("Такой email уже используется.");
        }
    }
}
