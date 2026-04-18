package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тестирование REST-контроллера вещей")
public class ItemControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .name("Test user")
            .email("tester@yandex.ru")
            .build();
    private final UserDto userDto2 = UserDto.builder()
            .id(2L)
            .name("Test user 2")
            .email("tester2@yandex.ru")
            .build();
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("Test item 1")
            .description("Test item description 1")
            .available(true)
            .ownerId(userDto1.getId())
            .requestId(null)
            .build();
    private final ItemDto itemDto2 = ItemDto.builder()
            .id(2L)
            .name("Test item 2")
            .description("Test item description 2")
            .available(true)
            .ownerId(userDto2.getId())
            .requestId(null)
            .build();
    private final ItemExtendedDto itemExtendedDto2 = ItemExtendedDto.builder()
            .id(itemDto2.getId())
            .name(itemDto2.getName())
            .description(itemDto2.getDescription())
            .available(itemDto2.getAvailable())
            .ownerId(itemDto2.getOwnerId())
            .requestId(null)
            .lastBooking(null)
            .nextBooking(null)
            .comments(List.of())
            .build();
    private final BookingDto bookingItemDto1 = BookingDto.builder()
            .id(1L)
            .bookerId(userDto2.getId())
            .start(LocalDateTime.now().minusMinutes(10))
            .end(LocalDateTime.now().minusMinutes(5))
            .build();
    private final BookingDto bookingItemDto2 = BookingDto.builder()
            .id(2L)
            .bookerId(userDto2.getId())
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusMinutes(10))
            .build();
    private final CommentDto commentDto1 = CommentDto.builder()
            .id(1L)
            .text("comment 1")
            .created(LocalDateTime.now().minusMinutes(10))
            .authorName(userDto2.getName())
            .build();
    private final CommentDto commentDto2 = CommentDto.builder()
            .id(2L)
            .text("comment 2")
            .created(LocalDateTime.now().minusMinutes(5))
            .authorName(userDto2.getName())
            .build();
    private final ItemExtendedDto itemExtendedDto1 = ItemExtendedDto.builder()
            .id(itemDto1.getId())
            .name(itemDto1.getName())
            .description(itemDto1.getDescription())
            .available(itemDto1.getAvailable())
            .ownerId(itemDto1.getOwnerId())
            .requestId(null)
            .lastBooking(bookingItemDto1)
            .nextBooking(bookingItemDto2)
            .comments(List.of(commentDto1, commentDto2))
            .build();
    private final CommentRequestDto commentRequestDto = CommentRequestDto.builder()
            .text("comment 1")
            .build();
    @MockBean
    private ItemService itemService;
    private ItemDto itemDto;
    private int from;
    private int size;

    @BeforeEach
    public void beforeEach() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test item")
                .description("Test item description")
                .available(true)
                .ownerId(userDto1.getId())
                .requestId(null)
                .build();
        from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    }

    @Nested
    @DisplayName("Тестирование эндпоинта создания вещи")
    class Add {
        @Test
        @DisplayName("Должен успешно создать вещь через REST-контроллер")
        void shouldAdd() throws Exception {
            when(itemService.add(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.any(ItemDto.class)))
                    .thenReturn(itemDto);

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemDto)));

            verify(itemService, times(1)).add(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.any(ItemDto.class));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта получения всех вещей владельца")
    class GetByOwner {
        @Test
        @DisplayName("Должен успешно получить все вещи владельца через REST-контроллер с пагинацией")
        void shouldGet() throws Exception {
            when(itemService.getByOwnerId((userDto1.getId()),
                    (PageRequest.of(from / size, size))))
                    .thenReturn(List.of(itemExtendedDto1, itemExtendedDto2));

            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemExtendedDto1, itemExtendedDto2))));

            verify(itemService, times(1)).getByOwnerId((userDto1.getId()),
                    (PageRequest.of(from / size, size)));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта получения вещи по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить расширенную вещь по идентификатору через REST-контроллер")
        void shouldGet() throws Exception {
            when(itemService.getById((userDto1.getId()), (itemDto1.getId())))
                    .thenReturn(itemExtendedDto1);

            mvc.perform(get("/items/{id}", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemExtendedDto1)));

            verify(itemService, times(1)).getById((userDto1.getId()),
                    (itemDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта обновления вещи")
    class Update {
        @Test
        @DisplayName("Должен успешно обновить вещь через REST-контроллер")
        void shouldUpdate() throws Exception {
            when(itemService.update(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(itemDto1.getId()),
                    ArgumentMatchers.any(ItemDto.class)))
                    .thenReturn(itemDto1);

            mvc.perform(patch("/items/{id}", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemDto1)));

            verify(itemService, times(1)).update(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(itemDto1.getId()), ArgumentMatchers.any(ItemDto.class));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта удаления вещи")
    class Delete {
        @Test
        @DisplayName("Должен успешно удалить вещь через REST-контроллер")
        void shouldDelete() throws Exception {
            mvc.perform(delete("/items/{id}", itemDto1.getId()))
                    .andExpect(status().isOk());

            verify(itemService, times(1)).delete((itemDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта поиска вещей по тексту")
    class Search {
        @Test
        @DisplayName("Должен успешно выполнить поиск доступных вещей по тексту через REST-контроллер")
        void shouldSearch() throws Exception {
            String text = "text for search";
            when(itemService.search((text),
                    (PageRequest.of(from / size, size))))
                    .thenReturn(List.of(itemDto1, itemDto2));

            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto1, itemDto2))));

            verify(itemService, times(1)).search((text),
                    (PageRequest.of(from / size, size)));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта добавления комментария к вещи")
    class AddComment {
        @Test
        @DisplayName("Должен успешно добавить комментарий к вещи через REST-контроллер")
        void shouldAdd() throws Exception {
            when(itemService.addComment(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(itemDto1.getId()),
                    ArgumentMatchers.any(CommentRequestDto.class)))
                    .thenReturn(commentDto1);

            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(commentDto1)));

            verify(itemService, times(1)).addComment(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(itemDto1.getId()), ArgumentMatchers.any(CommentRequestDto.class));
        }
    }
}