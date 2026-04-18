package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        log.info("Вывод всех пользователей.");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto get(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return userMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует.")));
    }

    @Override
    @Transactional
    public UserDto add(UserDto userDto) {
        log.info("Добавление пользователя {}", userDto);
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        log.info("Обновление пользователя {} с id {}.", userDto, id);

        User repoUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует."));

        userDto.setId(id);
        User user = userMapper.toUser(userDto);

        if (user.getEmail() != null) {
            repoUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            repoUser.setName(user.getName());
        }

        return userMapper.toUserDto(userRepository.save(repoUser));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление пользователя с id {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует."));
    }
}