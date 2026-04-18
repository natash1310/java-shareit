package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование маппера запросов вещей")
public class ItemRequestMapperImplTest {
    private final LocalDateTime dateTime = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
    private final User user = User.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    private final List<ItemDto> itemsDto = List.of(ItemDto.builder()
            .id(1L)
            .name("item name")
            .description("item description")
            .available(true)
            .ownerId(user.getId())
            .requestId(1L)
            .build());
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("itemRequest1 description")
            .requesterId(user)
            .created(dateTime)
            .build();
    private final ItemRequestAddDto itemRequestCreateDto = ItemRequestAddDto.builder()
            .description("item description")
            .build();
    @InjectMocks
    private ItemRequestMapperImpl itemRequestMapper;

    @Nested
    @DisplayName("Тестирование преобразования DTO создания в сущность запроса вещи")
    class ToItemRequest {
        @Test
        @DisplayName("Должен успешно преобразовать DTO создания в сущность запроса вещи")
        public void shouldReturnItemRequest() {
            ItemRequest result = itemRequestMapper.toItemRequest(itemRequestCreateDto, user, dateTime);

            assertNull(result.getId());
            assertEquals(itemRequestCreateDto.getDescription(), result.getDescription());
            assertEquals(user.getId(), result.getRequesterId().getId());
            assertEquals(user.getName(), result.getRequesterId().getName());
            assertEquals(user.getEmail(), result.getRequesterId().getEmail());
            assertEquals(dateTime, result.getCreated());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входных параметров")
        public void shouldReturnNull() {
            ItemRequest result = itemRequestMapper.toItemRequest(null, null, null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Тестирование преобразования сущности запроса вещи в базовый DTO")
    class ToItemRequestDto {
        @Test
        @DisplayName("Должен успешно преобразовать сущность запроса вещи в базовый DTO")
        public void shouldReturnItemRequestDto() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входного параметра")
        public void shouldReturnNull() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Тестирование преобразования сущности запроса вещи в расширенный DTO со списком вещей")
    class ToItemRequestExtendedDto {
        @Test
        @DisplayName("Должен успешно преобразовать сущность запроса вещи в расширенный DTO со списком вещей")
        public void shouldReturnItemRequestExtendedDto() {
            ItemRequestExtendedDto result = itemRequestMapper.toItemRequestExtendedDto(itemRequest, itemsDto);

            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
            assertEquals(itemsDto, result.getItems());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входных параметров")
        public void shouldReturnNull() {
            ItemRequestExtendedDto result = itemRequestMapper.toItemRequestExtendedDto(null, null);

            assertNull(result);
        }
    }
}