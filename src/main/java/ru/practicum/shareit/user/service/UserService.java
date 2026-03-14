package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto get(Long userId);

    UserDto add(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    Boolean delete(Long userId);
}
