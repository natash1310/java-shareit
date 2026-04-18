package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса пользователей")
class UserServiceImplTest {
    private final User user1 = User.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("Test user 2")
            .email("tester2@yandex.ru")
            .build();
    private final UserMapperImpl userMapper = new UserMapperImpl();
    @Mock
    private UserRepository userRepository;
    private UserServiceImpl userService;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;
    private UserDto updateUserDto;

    @BeforeEach
    public void beforeEachPatch() {
        updateUserDto = UserDto.builder()
                .id(1L)
                .name("Update test user 1")
                .email("tester2@yandex.ru")
                .build();
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    private void checkUserDto(User user, UserDto userDtoFromService) {
        assertEquals(user.getId(), userDtoFromService.getId());
        assertEquals(user.getName(), userDtoFromService.getName());
        assertEquals(user.getEmail(), userDtoFromService.getEmail());
    }

    @Nested
    @DisplayName("Тестирование получения всех пользователей")
    class GetAll {
        @Test
        @DisplayName("Должен успешно получить список всех пользователей")
        public void shouldGet() {
            when(userRepository.findAll()).thenReturn(List.of(user1, user2));

            List<UserDto> usersFromService = userService.getAll();

            assertEquals(2, usersFromService.size());

            UserDto userFromService1 = usersFromService.get(0);
            UserDto userFromService2 = usersFromService.get(1);

            checkUserDto(user1, userFromService1);
            checkUserDto(user2, userFromService2);
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если пользователей нет")
        public void shouldGetIfEmpty() {
            when(userRepository.findAll()).thenReturn(new ArrayList<>());

            List<UserDto> usersFromService = userService.getAll();

            assertTrue(usersFromService.isEmpty());
            verify(userRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Тестирование получения пользователя по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить пользователя по существующему идентификатору")
        public void shouldGet() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

            UserDto userFromService = userService.get(1L);

            checkUserDto(user1, userFromService);
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Должен выбросить исключение, если пользователь с указанным идентификатором не найден")
        public void shouldThrowExceptionIfUserIdNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            verify(userRepository, times(1)).findById(any());
        }
    }

    @Nested
    @DisplayName("Тестирование добавления нового пользователя")
    class Add {
        @Test
        @DisplayName("Должен успешно добавить нового пользователя")
        public void shouldAdd() {
            userService.add(userMapper.toUserDto(user1));

            verify(userRepository, times(1)).save(user1);
        }
    }

    @Nested
    @DisplayName("Тестирование обновления данных пользователя")
    class Update {
        @Test
        @DisplayName("Должен успешно обновить данные существующего пользователя")
        public void shouldUpdate() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

            userService.update(user1.getId(), updateUserDto);

            verify(userRepository, times(1)).save(userArgumentCaptor.capture());

            User savedUser = userArgumentCaptor.getValue();

            assertEquals(user1.getId(), savedUser.getId());
            assertEquals(updateUserDto.getName(), savedUser.getName());
            assertEquals(updateUserDto.getEmail(), savedUser.getEmail());
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении несуществующего пользователя")
        public void shouldThrowExceptionIfUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.update(99L, updateUserDto));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            verify(userRepository, times(1)).findById(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Тестирование удаления пользователя")
    class Delete {
        @Test
        @DisplayName("Должен успешно удалить существующего пользователя")
        public void shouldDelete() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            userService.delete(user1.getId());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            verify(userRepository, times(1)).deleteById(1L);
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Должен корректно обработать удаление несуществующего пользователя")
        public void shouldDeleteIfUserIdNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            userService.delete(99L);

            NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            verify(userRepository, times(1)).deleteById(99L);
            verify(userRepository, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("Тестирование внутреннего метода получения пользователя по идентификатору")
    class GetUserById {
        @Test
        @DisplayName("Должен успешно получить сущность пользователя по существующему идентификатору")
        public void shouldGet() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

            User userFromService = userService.getUserById(1L);

            assertEquals(user1.getId(), userFromService.getId());
            assertEquals(user1.getName(), userFromService.getName());
            assertEquals(user1.getEmail(), userFromService.getEmail());
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке получить сущность несуществующего пользователя")
        public void shouldThrowExceptionIfUserIdNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            verify(userRepository, times(1)).findById(any());
        }
    }
}