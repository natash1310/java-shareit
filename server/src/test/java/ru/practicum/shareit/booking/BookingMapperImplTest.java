package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapperImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование маппера бронирований")
public class BookingMapperImplTest {
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
    private final Status status = Status.WAITING;
    private final User user = User.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@yandex.ru")
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .name("item1 name")
            .description("seaRch1 description ")
            .available(true)
            .owner(user)
            .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .start(dateTime.plusYears(5))
            .end(dateTime.plusYears(6))
            .itemId(item.getId())
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(dateTime.minusYears(10))
            .end(dateTime.minusYears(9))
            .item(item)
            .booker(user)
            .status(Status.APPROVED)
            .build();
    @Mock
    private UserMapperImpl userMapper;
    @Mock
    private ItemMapperImpl itemMapper;
    @InjectMocks
    private BookingMapperImpl bookingMapper;

    @Nested
    @DisplayName("Тестирование преобразования DTO запроса бронирования в сущность бронирования")
    class RequestDtoToBooking {
        @Test
        @DisplayName("Должен успешно преобразовать DTO запроса бронирования в сущность бронирования")
        public void shouldReturnBooking() {
            Booking result = bookingMapper.requestDtoToBooking(bookingRequestDto, item, user, status);

            assertNull(result.getId());
            assertEquals(status, result.getStatus());
            assertEquals(bookingRequestDto.getStart(), result.getStart());
            assertEquals(bookingRequestDto.getEnd(), result.getEnd());
            assertEquals(user.getId(), result.getBooker().getId());
            assertEquals(user.getName(), result.getBooker().getName());
            assertEquals(user.getEmail(), result.getBooker().getEmail());
            assertEquals(bookingRequestDto.getItemId(), result.getItem().getId());
            assertEquals(item.getDescription(), result.getItem().getDescription());
            assertEquals(item.getAvailable(), result.getItem().getAvailable());
            assertEquals(item.getName(), result.getItem().getName());
            assertEquals(item.getOwner().getId(), result.getItem().getOwner().getId());
            assertEquals(item.getOwner().getName(), result.getItem().getOwner().getName());
            assertEquals(item.getOwner().getEmail(), result.getItem().getOwner().getEmail());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входных параметров")
        public void shouldReturnNull() {
            Booking result = bookingMapper.requestDtoToBooking(null, null,
                    null, null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Тестирование преобразования сущности бронирования в DTO ответа бронирования")
    class BookingToBookingResponseDto {
        @Test
        @DisplayName("Должен успешно преобразовать сущность бронирования в DTO ответа бронирования")
        public void shouldReturnBookingResponseDto() {
            when(userMapper.toUserDto(any())).thenCallRealMethod();
            when(itemMapper.toItemDto(any())).thenCallRealMethod();

            BookingResponseDto result = bookingMapper.bookingToBookingResponseDto(booking);

            assertEquals(booking.getId(), result.getId());
            assertEquals(booking.getStart(), result.getStart());
            assertEquals(booking.getEnd(), result.getEnd());
            assertEquals(booking.getStatus(), result.getStatus());
            assertEquals(booking.getBooker().getId(), result.getBooker().getId());
            assertEquals(booking.getBooker().getName(), result.getBooker().getName());
            assertEquals(booking.getBooker().getEmail(), result.getBooker().getEmail());
            assertEquals(booking.getItem().getId(), result.getItem().getId());
            assertEquals(booking.getItem().getName(), result.getItem().getName());
            assertEquals(booking.getItem().getDescription(), result.getItem().getDescription());
            assertEquals(booking.getItem().getAvailable(), result.getItem().getAvailable());
            assertEquals(booking.getItem().getOwner().getId(), result.getItem().getOwnerId());
            assertEquals(booking.getItem().getRequestId(), result.getItem().getRequestId());
            verify(userMapper, times(1)).toUserDto(any());
            verify(itemMapper, times(1)).toItemDto(any());
        }

        @Test
        @DisplayName("Должен вернуть null при передаче null в качестве входного параметра")
        public void shouldReturnNull() {
            BookingResponseDto result = bookingMapper.bookingToBookingResponseDto(null);

            assertNull(result);
        }
    }
}