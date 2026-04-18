package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционное тестирование REST-контроллера пользователей")
public class UserControllerFullContextTest {
    private final UserController userController;

    private void checkUserDto(UserDto userDto, UserDto userDtoFromController) {
        assertEquals(userDto.getId(), userDtoFromController.getId());
        assertEquals(userDto.getName(), userDtoFromController.getName());
        assertEquals(userDto.getEmail(), userDtoFromController.getEmail());
    }

    @Nested
    @DisplayName("Интеграционное тестирование создания пользователя")
    class Add {
        @Test
        @DisplayName("Должен успешно создать пользователя в контексте БД")
        public void shouldAdd() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            List<UserDto> usersFromController = new ArrayList<>(Objects.requireNonNull(userController.getAll()
                    .getBody()));

            assertEquals(1, usersFromController.size());

            UserDto userFromController = usersFromController.getFirst();

            checkUserDto(userDto, userFromController);
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке создать пользователя с уже существующим email")
        public void shouldThrowExceptionIfExistedEmail() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester@yandex.ru")
                    .build();

            assertThrows(DataIntegrityViolationException.class, () -> userController.add(userDto2));
            assertEquals(1, Objects.requireNonNull(userController.getAll().getBody()).size());

            UserDto userFromController = Objects.requireNonNull(userController.getAll().getBody()).getFirst();

            checkUserDto(userDto1, userFromController);
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование получения всех пользователей")
    class GetAll {
        @Test
        @DisplayName("Должен успешно получить список всех пользователей из БД")
        public void shouldGet() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            List<UserDto> usersFromController = userController.getAll().getBody();

            assertEquals(2, Objects.requireNonNull(usersFromController).size());

            UserDto userFromController1 = usersFromController.get(0);
            UserDto userFromController2 = usersFromController.get(1);

            checkUserDto(userDto1, userFromController1);
            checkUserDto(userDto2, userFromController2);
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если в БД нет пользователей")
        public void shouldGetIfEmpty() {
            List<UserDto> usersFromController = userController.getAll().getBody();

            assertTrue(Objects.requireNonNull(usersFromController).isEmpty());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование получения пользователя по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить пользователя по существующему идентификатору из БД")
        public void shouldGet() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto usersFromController = userController.get(1L).getBody();

            checkUserDto(userDto1, Objects.requireNonNull(usersFromController));
        }

        @Test
        @DisplayName("Должен выбросить исключение при поиске несуществующего пользователя в БД")
        public void shouldThrowExceptionIfUserIdNotFound() {
            NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.get(10L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            assertTrue(Objects.requireNonNull(userController.getAll().getBody()).isEmpty());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование обновления данных пользователя")
    class Update {
        @Test
        @DisplayName("Должен успешно обновить данные существующего пользователя в БД")
        public void shouldUpdate() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Update test user 1")
                    .email("tester2@yandex.ru")
                    .build();
            userController.update(userDto1.getId(), userDto2);

            List<UserDto> usersFromController = userController.getAll().getBody();

            assertEquals(1, Objects.requireNonNull(usersFromController).size());

            UserDto userFromController = usersFromController.getFirst();

            assertEquals(userFromController.getId(), userDto1.getId());
            assertEquals(userFromController.getName(), userDto2.getName());
            assertEquals(userFromController.getEmail(), userDto2.getEmail());
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении с уже существующим email в БД")
        public void shouldThrowExceptionIfExistedEmail() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            UserDto userDto3 = UserDto.builder()
                    .id(3L)
                    .name("Patch test user 1")
                    .email("tester2@yandex.ru")
                    .build();

            assertThrows(DataIntegrityViolationException.class, () -> userController.update(userDto1.getId(), userDto3));

            List<UserDto> usersFromController = userController.getAll().getBody();

            assertEquals(2, Objects.requireNonNull(usersFromController).size());

            UserDto userFromController1 = usersFromController.get(0);
            UserDto userFromController2 = usersFromController.get(1);

            checkUserDto(userDto1, userFromController1);
            checkUserDto(userDto2, userFromController2);
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование удаления пользователя")
    class Delete {
        @Test
        @DisplayName("Должен успешно удалить существующего пользователя из БД")
        public void shouldDelete() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            List<UserDto> usersFromController = userController.getAll().getBody();

            assertEquals(1, Objects.requireNonNull(usersFromController).size());

            UserDto userFromController = usersFromController.getFirst();

            checkUserDto(userDto1, userFromController);

            userController.delete(userDto1.getId());

            assertTrue(userController.getAll().getBody().isEmpty());
        }

        @Test
        @DisplayName("Должен корректно обработать удаление несуществующего пользователя из БД")
        public void shouldDeleteIfUserIdNotFound() {
            assertDoesNotThrow(() -> userController.delete(10L));
            assertTrue(Objects.requireNonNull(userController.getAll().getBody()).isEmpty());
        }
    }
}