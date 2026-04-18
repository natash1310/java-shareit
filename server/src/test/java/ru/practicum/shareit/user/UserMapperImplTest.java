package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapperImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование маппера пользователей")
class UserMapperImplTest {
    private final User user = User.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    @InjectMocks
    private UserMapperImpl userMapper;

    @Nested
    @DisplayName("Тестирование преобразования сущности пользователя в DTO")
    class ToUserDto {
        @Test
        @DisplayName("Должен успешно преобразовать сущность пользователя в DTO")
        public void shouldReturnUserDto() {
            UserDto result = userMapper.toUserDto(user);

            assertEquals(user.getId(), result.getId());
            assertEquals(user.getName(), result.getName());
            assertEquals(user.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входного параметра")
        public void shouldReturnNull() {
            UserDto result = userMapper.toUserDto(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Тестирование преобразования DTO в сущность пользователя")
    class ToUser {
        @Test
        @DisplayName("Должен успешно преобразовать DTO в сущность пользователя")
        public void shouldReturnUser() {
            User result = userMapper.toUser(userDto);

            assertEquals(userDto.getId(), result.getId());
            assertEquals(userDto.getName(), result.getName());
            assertEquals(userDto.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входного параметра")
        public void shouldReturnNull() {
            User result = userMapper.toUser(null);

            assertNull(result);
        }
    }
}