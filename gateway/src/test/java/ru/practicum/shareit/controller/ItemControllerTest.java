package ru.practicum.shareit.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.model.CommentRequestDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тесты контроллера для работы с вещами (ItemController)")
public class ItemControllerTest {

    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .name("Test user")
            .email("tester@yandex.ru")
            .build();
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("Test item 1")
            .description("Test item description 1")
            .available(true)
            .ownerId(userDto1.getId())
            .requestId(null)
            .build();
    private final String text = "text for search";
    @MockBean
    private ItemClient itemClient;
    private ItemDto itemDto;
    private CommentRequestDto commentRequestDto;
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
        commentRequestDto = CommentRequestDto.builder()
                .text("comment 1")
                .build();
        from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    }

    @Nested
    @DisplayName("Тесты добавления вещи (POST /items)")
    class Add {

        @Test
        @DisplayName("Должен успешно добавить вещь при валидных данных")
        public void shouldAdd() throws Exception {
            when(itemClient.add(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.any(ItemDto.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).add(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.any(ItemDto.class));
        }

        @Test
        @DisplayName("Должен выбросить исключение, если название вещи равно null")
        public void shouldThrowExceptionIfNameIsNull() throws Exception {
            itemDto.setName(null);

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если название вещи пустое")
        public void shouldThrowExceptionIfNameIsEmpty() throws Exception {
            itemDto.setName("");

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если название вещи состоит только из пробелов")
        public void shouldThrowExceptionIfNameIsBlank() throws Exception {
            itemDto.setName(" ");

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если описание вещи равно null")
        public void shouldThrowExceptionIfDescriptionIsNull() throws Exception {
            itemDto.setDescription(null);

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если описание вещи пустое")
        public void shouldThrowExceptionIfDescriptionIsEmpty() throws Exception {
            itemDto.setDescription("");

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если описание вещи состоит только из пробелов")
        public void shouldThrowExceptionIfDescriptionIsBlank() throws Exception {
            itemDto.setDescription(" ");

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если статус доступности вещи равен null")
        public void shouldThrowExceptionIfAvailableIsNull() throws Exception {
            itemDto.setAvailable(null);

            mvc.perform(post("/items")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Тесты получения вещей владельца (GET /items)")
    class GetByOwner {

        @Test
        @DisplayName("Должен успешно получить список вещей владельца с пагинацией")
        public void shouldGet() throws Exception {
            when(itemClient.getByOwnerId(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(from),
                    ArgumentMatchers.eq(size))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).getByOwnerId(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(from), ArgumentMatchers.eq(size));
        }

        @Test
        @DisplayName("Должен выбросить исключение, если параметр from отрицательный")
        public void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;

            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).getByOwnerId(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если параметр size равен нулю")
        public void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;

            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).getByOwnerId(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если параметр size отрицательный")
        public void shouldThrowExceptionIfSizeIsNegative() throws Exception {
            size = -1;

            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).getByOwnerId(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Тесты получения вещи по ID (GET /items/{id})")
    class GetById {

        @Test
        @DisplayName("Должен успешно получить вещь по идентификатору")
        public void shouldGet() throws Exception {
            when(itemClient.getById(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(itemDto1.getId())))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/items/{id}", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).getById(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(itemDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Тесты обновления вещи (PATCH /items/{id})")
    class Update {

        @Test
        @DisplayName("Должен успешно обновить данные вещи")
        public void shouldUpdate() throws Exception {
            when(itemClient.update(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(itemDto1.getId()),
                    ArgumentMatchers.any(ItemDto.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(patch("/items/{id}", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).update(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(itemDto1.getId()), ArgumentMatchers.any(ItemDto.class));
        }
    }

    @Nested
    @DisplayName("Тесты удаления вещи (DELETE /items/{id})")
    class Delete {

        @Test
        @DisplayName("Должен успешно удалить вещь по идентификатору")
        public void shouldDelete() throws Exception {
            mvc.perform(delete("/items/{id}", itemDto1.getId()))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).delete(ArgumentMatchers.eq(itemDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Тесты поиска вещей (GET /items/search)")
    class Search {

        @Test
        @DisplayName("Должен успешно выполнить поиск вещей по тексту с пагинацией")
        public void shouldSearch() throws Exception {
            when(itemClient.search(ArgumentMatchers.eq(text), ArgumentMatchers.eq(from),
                    ArgumentMatchers.eq(size))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).search(ArgumentMatchers.eq(text),
                    ArgumentMatchers.eq(from), ArgumentMatchers.eq(size));
        }

        @Test
        @DisplayName("Должен выбросить исключение при поиске, если параметр from отрицательный")
        public void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;

            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).search(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение при поиске, если параметр size равен нулю")
        public void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;

            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).search(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение при поиске, если параметр size отрицательный")
        public void shouldThrowExceptionIfSizeIsNegative() throws Exception {
            size = -1;

            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isInternalServerError());

            verify(itemClient, never()).search(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Тесты добавления комментария к вещи (POST /items/{id}/comment)")
    class AddComment {

        @Test
        @DisplayName("Должен успешно добавить комментарий к вещи")
        public void shouldAdd() throws Exception {
            when(itemClient.addComment(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(itemDto1.getId()),
                    ArgumentMatchers.any(CommentRequestDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(itemClient, times(1)).addComment(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(itemDto1.getId()), ArgumentMatchers.any(CommentRequestDto.class));
        }

        @Test
        @DisplayName("Должен выбросить исключение, если текст комментария равен null")
        public void shouldThrowExceptionIfNull() throws Exception {
            commentRequestDto.setText(null);

            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).addComment(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если текст комментария пустой")
        public void shouldThrowExceptionIfEmpty() throws Exception {
            commentRequestDto.setText("");

            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).addComment(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если текст комментария состоит только из пробелов")
        public void shouldThrowExceptionIfBlank() throws Exception {
            commentRequestDto.setText(" ");

            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemClient, never()).addComment(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }
}