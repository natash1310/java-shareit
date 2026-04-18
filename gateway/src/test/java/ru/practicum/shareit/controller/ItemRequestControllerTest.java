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
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.request.ItemRequestAddDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тесты контроллера запросов на вещи (ItemRequestController)")
public class ItemRequestControllerTest {

    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    private final UserDto userDto2 = UserDto.builder()
            .id(2L)
            .name("Test user 2")
            .email("tester2@yandex.ru")
            .build();
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("item dto 1")
            .description("item dto 1 description")
            .available(true)
            .ownerId(userDto1.getId())
            .requestId(1L)
            .build();
    @MockBean
    private ItemRequestClient itemRequestClient;
    private ItemRequestAddDto itemRequestCreateDto;
    private int from;
    private int size;

    @BeforeEach
    public void beforeEach() {
        itemRequestCreateDto = ItemRequestAddDto.builder()
                .description("item description")
                .build();
        from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    }

    @Nested
    @DisplayName("Создание запроса на вещь (POST /requests)")
    class Add {
        @Test
        @DisplayName("Должен создать запрос с корректным описанием")
        public void shouldAdd() throws Exception {
            when(itemRequestClient.add(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.any(ItemRequestAddDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(post("/requests")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(itemRequestClient, times(1))
                    .add(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.any(ItemRequestAddDto.class));
        }

        @Test
        @DisplayName("Должен вернуть ошибку 400, если описание отсутствует (null)")
        public void shouldThrowExceptionIfNotDescription() throws Exception {
            itemRequestCreateDto.setDescription(null);

            mvc.perform(post("/requests")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemRequestClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку 400, если описание пустое")
        public void shouldThrowExceptionIfDescriptionIsEmpty() throws Exception {
            itemRequestCreateDto.setDescription("");

            mvc.perform(post("/requests")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemRequestClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку 400, если описание содержит только пробелы")
        public void shouldThrowExceptionIfDescriptionIsBlank() throws Exception {
            itemRequestCreateDto.setDescription(" ");

            mvc.perform(post("/requests")
                            .header(Constants.headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemRequestClient, never()).add(ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Получение запроса по ID (GET /requests/{id})")
    class GetById {
        @Test
        @DisplayName("Должен вернуть запрос по существующему ID")
        public void shouldGet() throws Exception {
            when(itemRequestClient.getById(ArgumentMatchers.eq(userDto2.getId()), ArgumentMatchers.eq(itemDto1.getId())))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/requests/{id}", itemDto1.getId())
                            .header(Constants.headerUserId, userDto2.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(itemRequestClient, times(1))
                    .getById(ArgumentMatchers.eq(userDto2.getId()), ArgumentMatchers.eq(itemDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Получение списка запросов по ID автора (GET /requests)")
    class GetByRequesterId {
        @Test
        @DisplayName("Должен вернуть список запросов, созданных пользователем")
        public void shouldGet() throws Exception {
            when(itemRequestClient.getByRequesterId(ArgumentMatchers.eq(userDto2.getId())))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/requests")
                            .header(Constants.headerUserId, userDto2.getId()))
                    .andExpect(status().isOk());

            verify(itemRequestClient, times(1))
                    .getByRequesterId(ArgumentMatchers.eq(userDto2.getId()));
        }
    }

    @Nested
    @DisplayName("Получение списка всех запросов других пользователей (GET /requests/all)")
    class GetAll {
        @Test
        @DisplayName("Должен вернуть список запросов с корректной пагинацией")
        public void shouldGet() throws Exception {
            when(itemRequestClient.getAll(ArgumentMatchers.eq(userDto1.getId()), ArgumentMatchers.eq(from),
                    ArgumentMatchers.eq(size))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isOk());

            verify(itemRequestClient, times(1)).getAll(ArgumentMatchers.eq(userDto1.getId()),
                    ArgumentMatchers.eq(from), ArgumentMatchers.eq(size));
        }

        @Test
        @DisplayName("Должен вернуть ошибку 500, если параметр from отрицательный")
        public void shouldThrowExceptionIfInvalidFrom() throws Exception {
            from = -1;

            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemRequestClient, never()).getAll(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку 500, если параметр size равен нулю")
        public void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;

            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemRequestClient, never()).getAll(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку 500, если параметр size отрицательный")
        public void shouldThrowExceptionIfSizeIsNegative() throws Exception {
            size = -1;

            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(Constants.headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());

            verify(itemRequestClient, never()).getAll(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }
}