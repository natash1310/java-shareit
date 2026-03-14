package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    @Override
    public UserDto add(UserDto userDto) {
        log.info("Добавление пользователя {}", userDto);
        return UserMapper.toUserDto(userStorage.add(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto get(Long userId) {
        log.info("Вывод пользователя с id {}.", userId);
        return UserMapper.toUserDto(userStorage.getUserById(userId));
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Вывод всех пользователей.");
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Обновление пользователя {} с id {}.", userDto, userId);
        userDto.setId(userId);
        return UserMapper.toUserDto(userStorage.update(UserMapper.toUser(userDto)));
    }

    @Override
    public Boolean delete(Long userId) {
        log.info("Удаление пользователя с id {}", userId);
        return userStorage.delete(userId);
    }
}