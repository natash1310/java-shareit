package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        log.info("Вывод всех пользователей.");
        return userStorage.getAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto get(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public UserDto add(UserDto userDto) {
        log.info("Добавление пользователя {}", userDto);
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userStorage.add(user));
    }

    public UserDto update(Long userId, UserDto userDto) {
        log.info("Обновление пользователя {} с id {}.", userDto, userId);
        User user = userMapper.toUser(userDto);
        user.setId(userId);
        return userMapper.toUserDto(userStorage.update(user));
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя с id {}", id);
        userStorage.delete(id);
    }

    @Override
    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }
}