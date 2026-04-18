package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тестирование REST-контроллера бронирований")
public class BookingControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
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
    private final UserDto userDto2 = UserDto.builder()
            .id(user2.getId())
            .name(user2.getName())
            .email(user2.getEmail())
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("item dto 1")
            .description("item dto 1 description")
            .available(true)
            .ownerId(user1.getId())
            .requestId(1L)
            .build();
    private final BookingResponseDto bookingResponseDto1 = BookingResponseDto.builder()
            .id(1L)
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusMinutes(10))
            .item(itemDto)
            .booker(userDto2)
            .status(Status.WAITING)
            .build();
    private final BookingResponseDto bookingResponseDto2 = BookingResponseDto.builder()
            .id(2L)
            .start(LocalDateTime.now().plusMinutes(15))
            .end(LocalDateTime.now().plusMinutes(20))
            .item(itemDto)
            .booker(userDto2)
            .status(Status.WAITING)
            .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusMinutes(10))
            .itemId(1L)
            .build();
    @MockBean
    private BookingService bookingService;
    private BookingResponseDto bookingResponseDto;
    private int from;
    private int size;

    @BeforeEach
    public void beforeEach() {
        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .item(itemDto)
                .booker(userDto2)
                .status(Status.WAITING)
                .build();
        from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    }

    @Nested
    @DisplayName("Тестирование эндпоинта создания бронирования")
    class Add {
        @Test
        @DisplayName("Должен успешно создать бронирование через REST-контроллер")
        public void shouldAdd() throws Exception {
            when(bookingService.add(ArgumentMatchers.eq(user2.getId()), ArgumentMatchers.any(BookingRequestDto.class)))
                    .thenReturn(bookingResponseDto1);

            mvc.perform(post("/bookings")
                            .header(Constants.headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(bookingRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto1)));

            verify(bookingService, times(1)).add(ArgumentMatchers.eq(user2.getId()),
                    ArgumentMatchers.any(BookingRequestDto.class));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта обновления статуса бронирования")
    class Update {
        @Test
        @DisplayName("Должен успешно подтвердить бронирование через REST-контроллер")
        public void shouldApproved() throws Exception {
            bookingResponseDto.setStatus(Status.APPROVED);

            when(bookingService.update(ArgumentMatchers.eq(user2.getId()), ArgumentMatchers.eq(bookingResponseDto.getId()),
                    ArgumentMatchers.eq(true)))
                    .thenReturn(bookingResponseDto);

            mvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), true)
                            .header(Constants.headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

            verify(bookingService, times(1)).update(ArgumentMatchers.eq(user2.getId()),
                    ArgumentMatchers.eq(bookingResponseDto.getId()), ArgumentMatchers.eq(true));
        }

        @Test
        @DisplayName("Должен успешно отклонить бронирование через REST-контроллер")
        public void shouldReject() throws Exception {
            bookingResponseDto.setStatus(Status.REJECTED);

            when(bookingService.update(ArgumentMatchers.eq(user2.getId()), ArgumentMatchers.eq(bookingResponseDto.getId()),
                    ArgumentMatchers.eq(false)))
                    .thenReturn(bookingResponseDto);

            mvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), false)
                            .header(Constants.headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

            verify(bookingService, times(1)).update(ArgumentMatchers.eq(user2.getId()),
                    ArgumentMatchers.eq(bookingResponseDto.getId()), ArgumentMatchers.eq(false));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта получения бронирования по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить бронирование по идентификатору через REST-контроллер")
        public void shouldGet() throws Exception {
            when(bookingService.getById(ArgumentMatchers.eq(user2.getId()), ArgumentMatchers.eq(bookingResponseDto1.getId())))
                    .thenReturn(bookingResponseDto1);

            mvc.perform(get("/bookings/{id}", bookingResponseDto1.getId())
                            .header(Constants.headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto1)));

            verify(bookingService, times(1))
                    .getById(ArgumentMatchers.eq(user2.getId()), ArgumentMatchers.eq(bookingResponseDto1.getId()));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта получения всех бронирований текущего пользователя")
    class GetAllByByBookerId {
        @Test
        @DisplayName("Должен успешно получить все бронирования пользователя с пагинацией и фильтрацией по статусу через REST-контроллер")
        public void shouldGetWithValidState() throws Exception {
            when(bookingService.getAllByBookerId(ArgumentMatchers.eq(userDto2.getId()), ArgumentMatchers.eq(State.ALL),
                    ArgumentMatchers.eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", from, size)
                            .header(Constants.headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                    .getAllByBookerId(ArgumentMatchers.eq(userDto2.getId()), ArgumentMatchers.eq(State.ALL),
                            ArgumentMatchers.eq(PageRequest.of(from / size, size)));
        }
    }

    @Nested
    @DisplayName("Тестирование эндпоинта получения всех бронирований вещей владельца")
    class GetAllByByOwnerId {
        @Test
        @DisplayName("Должен успешно получить все бронирования вещей владельца с пагинацией и фильтрацией по статусу через REST-контроллер")
        public void shouldGetWithValidState() throws Exception {
            when(bookingService.getAllByOwnerId(ArgumentMatchers.eq(itemDto.getOwnerId()), ArgumentMatchers.eq(State.ALL),
                    ArgumentMatchers.eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", from, size)
                            .header(Constants.headerUserId, user1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                    .getAllByOwnerId(ArgumentMatchers.eq(itemDto.getOwnerId()), ArgumentMatchers.eq(State.ALL),
                            ArgumentMatchers.eq(PageRequest.of(from / size, size)));
        }
    }
}