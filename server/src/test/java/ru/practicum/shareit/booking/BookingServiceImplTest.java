package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса бронирований")
public class BookingServiceImplTest {
    private final int from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
    private final int size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime dateTime = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
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
    private final User user3 = User.builder()
            .id(3L)
            .name("Test user 3")
            .email("tester3@yandex.ru")
            .build();
    private final UserDto user2Dto = UserDto.builder()
            .id(2L)
            .name("Test user 2")
            .email("tester2@yandex.ru")
            .build();
    private final Item item1 = Item.builder()
            .id(1L)
            .name("item1 name")
            .description("seaRch1 description ")
            .available(true)
            .owner(user1)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(dateTime.minusYears(10))
            .end(dateTime.minusYears(9))
            .item(item1)
            .booker(user2)
            .status(Status.APPROVED)
            .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .start(dateTime.plusYears(5))
            .end(dateTime.plusYears(6))
            .itemId(item1.getId())
            .build();
    private final ItemDto item1Dto = ItemDto.builder()
            .id(1L)
            .name("item1 name")
            .description("seaRch1 description ")
            .available(true)
            .ownerId(user1.getId())
            .build();
    private final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
            .id(1L)
            .start(dateTime.minusYears(10))
            .end(dateTime.minusYears(9))
            .item(item1Dto)
            .booker(user2Dto)
            .status(Status.APPROVED)
            .build();
    private final Item itemIsNoAvailable = Item.builder()
            .id(3L)
            .name("item3 name")
            .description("itEm3 description")
            .available(false)
            .owner(user1)
            .build();
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapperImpl bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;
    private Booking bookingIsWaiting1;

    @BeforeEach
    public void beforeEach() {
        bookingIsWaiting1 = Booking.builder()
                .id(3L)
                .start(dateTime.plusYears(8))
                .end(dateTime.plusYears(9))
                .item(item1)
                .booker(user2)
                .status(Status.WAITING)
                .build();
    }

    private void checkBookingResponseDto(Booking booking, BookingResponseDto bookingResponseDto) {
        assertEquals(booking.getId(), bookingResponseDto.getId());
        assertEquals(booking.getStart(), bookingResponseDto.getStart());
        assertEquals(booking.getEnd(), bookingResponseDto.getEnd());
        assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        assertEquals(booking.getBooker().getName(), bookingResponseDto.getBooker().getName());
        assertEquals(booking.getBooker().getEmail(), bookingResponseDto.getBooker().getEmail());
        assertEquals(booking.getStatus(), bookingResponseDto.getStatus());
        assertEquals(booking.getItem().getId(), bookingResponseDto.getItem().getId());
        assertEquals(booking.getItem().getName(), bookingResponseDto.getItem().getName());
        assertEquals(booking.getItem().getDescription(), bookingResponseDto.getItem().getDescription());
        assertEquals(booking.getItem().getAvailable(), bookingResponseDto.getItem().getAvailable());
        assertEquals(booking.getItem().getRequestId(), bookingResponseDto.getItem().getRequestId());
        assertEquals(booking.getItem().getOwner().getId(), bookingResponseDto.getItem().getOwnerId());
    }

    @Nested
    @DisplayName("Тестирование получения бронирования по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить бронирование автором бронирования")
        public void shouldGetByAuthor() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            BookingResponseDto result = bookingService.getById(user2.getId(), booking.getId());

            checkBookingResponseDto(booking, result);
            verify(bookingRepository, times(1)).findById(1L);
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен успешно получить бронирование владельцем вещи")
        public void shouldGetByOwner() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            BookingResponseDto result = bookingService.getById(user1.getId(), booking.getId());

            checkBookingResponseDto(booking, result);
            verify(bookingRepository, times(1)).findById(1L);
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен выбросить исключение, если пользователь не автор и не владелец")
        public void shouldThrowExceptionIfNotOwnerOrAuthor() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            AuthorizationException exception = assertThrows(AuthorizationException.class,
                    () -> bookingService.getById(user3.getId(), booking.getId()));
            assertEquals("Просмотр бронирования доступно только автору или владельцу.", exception.getMessage());
            verify(bookingRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Тестирование получения всех бронирований пользователя по статусу")
    class GetAllByBookerId {
        @Test
        @DisplayName("Должен получить все бронирования пользователя со статусом ALL")
        public void shouldGetAllIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdOrderByStartDesc(user2.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.ALL, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdOrderByStartDesc(user2.getId(), pageable);
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у пользователя нет бронирований")
        public void shouldGetAllEmptyIfNotBooker() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByBookerIdOrderByStartDesc(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user1.getId(), State.ALL, pageable);

            assertTrue(results.isEmpty());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdOrderByStartDesc(user1.getId(), pageable);
        }

        @Test
        @DisplayName("Должен получить текущие бронирования пользователя со статусом CURRENT")
        public void shouldGetCurrentIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.CURRENT, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить завершённые бронирования пользователя со статусом PAST")
        public void shouldGetPastIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.PAST, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить будущие бронирования пользователя со статусом FUTURE")
        public void shouldGetFutureIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.FUTURE, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdAndStartAfterOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить ожидающие подтверждения бронирования пользователя со статусом WAITING")
        public void shouldGetWaitingIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.WAITING, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить отклонённые бронирования пользователя со статусом REJECTED")
        public void shouldGetRejectedIfBooker() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByBookerId(user2.getId(), State.REJECTED, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }
    }

    @Nested
    @DisplayName("Тестирование получения всех бронирований вещей владельца по статусу")
    class GetAllByOwnerId {
        @Test
        @DisplayName("Должен получить все бронирования вещей владельца со статусом ALL")
        public void shouldGetAllIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdOrderByStartDesc(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.ALL, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdOrderByStartDesc(user1.getId(), pageable);
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить текущие бронирования вещей владельца со статусом CURRENT")
        public void shouldGetCurrentIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.CURRENT, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить завершённые бронирования вещей владельца со статусом PAST")
        public void shouldGetPastIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.PAST, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить будущие бронирования вещей владельца со статусом FUTURE")
        public void shouldGetFutureIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.FUTURE, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdAndStartAfterOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить ожидающие подтверждения бронирования вещей владельца со статусом WAITING")
        public void shouldGetWaitingIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.WAITING, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Должен получить отклонённые бронирования вещей владельца со статусом REJECTED")
        public void shouldGetRejectedIfOwner() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.bookingToBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.getAllByOwnerId(user1.getId(), State.REJECTED, pageable);

            assertEquals(1, results.size());

            BookingResponseDto result = results.getFirst();

            checkBookingResponseDto(booking, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, times(1))
                    .findByItemOwnerIdAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingMapper, times(1)).bookingToBookingResponseDto(booking);
        }
    }

    @Nested
    @DisplayName("Тестирование создания нового бронирования")
    class Add {
        @Test
        @DisplayName("Должен успешно создать новое бронирование")
        public void shouldAdd() {
            when(itemService.getItemById(bookingRequestDto.getItemId())).thenReturn(item1);
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingMapper.requestDtoToBooking(bookingRequestDto, item1, user2, Status.WAITING))
                    .thenReturn(booking);

            bookingService.add(user2.getId(), bookingRequestDto);

            verify(itemService, times(1)).getItemById(bookingRequestDto.getItemId());
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingMapper, times(1))
                    .requestDtoToBooking(bookingRequestDto, item1, user2, Status.WAITING);
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(booking, savedBooking);
            assertEquals(booking.getId(), savedBooking.getId());
            assertEquals(booking.getStatus(), savedBooking.getStatus());
            assertEquals(booking.getStart(), savedBooking.getStart());
            assertEquals(booking.getEnd(), savedBooking.getEnd());
            assertEquals(booking.getBooker().getId(), savedBooking.getBooker().getId());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если вещь недоступна для бронирования")
        public void shouldThrowExceptionIfItemIsNotAvailable() {
            when(itemService.getItemById(bookingRequestDto.getItemId())).thenReturn(itemIsNoAvailable);

            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingService.add(user2.getId(), bookingRequestDto));
            assertEquals("Предмет недоступен для бронирования.", exception.getMessage());
            verify(itemService, times(1)).getItemById(bookingRequestDto.getItemId());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке бронирования своей вещи")
        public void shouldThrowExceptionIfBookingByOwner() {
            when(itemService.getItemById(bookingRequestDto.getItemId())).thenReturn(item1);
            when(userService.getUserById(user1.getId())).thenReturn(user1);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingService.add(user1.getId(), bookingRequestDto));
            assertEquals("Владелец не может бронировать собственную вещь.", exception.getMessage());
            verify(itemService, times(1)).getItemById(bookingRequestDto.getItemId());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Тестирование обновления статуса бронирования")
    class Update {
        @Test
        @DisplayName("Должен успешно подтвердить бронирование (APPROVED)")
        public void shouldApprove() {
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));

            bookingService.update(user1.getId(), bookingIsWaiting1.getId(), true);

            verify(bookingRepository, times(1)).findById(bookingIsWaiting1.getId());
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(Status.APPROVED, savedBooking.getStatus());
        }

        @Test
        @DisplayName("Должен успешно отклонить бронирование (REJECTED)")
        public void shouldReject() {
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));

            bookingService.update(user1.getId(), bookingIsWaiting1.getId(), false);

            verify(bookingRepository, times(1)).findById(bookingIsWaiting1.getId());
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(Status.REJECTED, savedBooking.getStatus());
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке обновления статуса не владельцем")
        public void shouldThrowExceptionIfUpdateNotOwner() {
            when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

            AuthorizationException exception = assertThrows(AuthorizationException.class,
                    () -> bookingService.update(user2.getId(), booking.getId(), false));
            assertEquals("Изменение статуса бронирования доступно только владельцу.", exception.getMessage());
            verify(bookingRepository, times(1)).findById(booking.getId());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить исключение при повторном обновлении статуса бронирования")
        public void shouldThrowExceptionIfAlreadyUpdateBefore() {
            when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingService.update(user1.getId(), booking.getId(), false));
            assertEquals("Ответ по бронированию уже дан.", exception.getMessage());
            verify(bookingRepository, times(1)).findById(booking.getId());
            verify(bookingRepository, never()).save(any());
        }
    }
}