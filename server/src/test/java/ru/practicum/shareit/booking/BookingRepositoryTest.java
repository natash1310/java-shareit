package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Тестирование репозитория бронирований")
public class BookingRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private final int from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
    private final int size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
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
    private final Item item1 = Item.builder()
            .id(1L)
            .name("item1 name")
            .description("seaRch1 description ")
            .available(true)
            .owner(user1)
            .build();
    private final Booking bookingPast = Booking.builder()
            .id(1L)
            .start(dateTime.minusYears(10))
            .end(dateTime.minusYears(9))
            .item(item1)
            .booker(user2)
            .status(Status.APPROVED)
            .build();
    private final Booking bookingCurrent = Booking.builder()
            .id(2L)
            .start(dateTime.minusYears(5))
            .end(dateTime.plusYears(5))
            .item(item1)
            .booker(user2)
            .status(Status.APPROVED)
            .build();
    private final Booking bookingFuture = Booking.builder()
            .id(3L)
            .start(dateTime.plusYears(8))
            .end(dateTime.plusYears(9))
            .item(item1)
            .booker(user2)
            .status(Status.WAITING)
            .build();
    private final Booking bookingRejected = Booking.builder()
            .id(4L)
            .start(dateTime.plusYears(9))
            .end(dateTime.plusYears(10))
            .item(item1)
            .booker(user2)
            .status(Status.REJECTED)
            .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingRepository.save(bookingPast);
        bookingRepository.save(bookingCurrent);
        bookingRepository.save(bookingFuture);
        bookingRepository.save(bookingRejected);
    }

    @Nested
    @DisplayName("Тестирование поиска всех бронирований пользователя с сортировкой по дате начала (убывание)")
    class FindByBookerIdOrderByStartDesc {
        @Test
        @DisplayName("Должен найти все бронирования пользователя с сортировкой по убыванию даты начала")
        public void shouldGetAll() {
            List<Booking> result = bookingRepository.findByBookerIdOrderByStartDesc(user2.getId(), pageable)
                    .get().collect(Collectors.toList());

            assertEquals(4, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
            assertEquals(bookingCurrent.getId(), result.get(2).getId());
            assertEquals(bookingPast.getId(), result.get(3).getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у пользователя нет бронирований")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByBookerIdOrderByStartDesc(user1.getId(), pageable)
                    .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска текущих бронирований пользователя")
    class FindByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc {
        @Test
        @DisplayName("Должен найти текущее бронирование пользователя")
        public void shouldGetCurrent() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(user2.getId(), dateTime,
                            dateTime, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingCurrent.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет текущих бронирований")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(user1.getId(), dateTime,
                            dateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска завершённых бронирований пользователя по статусу")
    class FindByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Должен найти завершённые бронирования пользователя с указанным статусом")
        public void shouldGetPast() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(user2.getId(), dateTime,
                            Status.APPROVED, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingPast.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет завершённых бронирований")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(user1.getId(), dateTime,
                            Status.APPROVED, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска будущих бронирований пользователя")
    class FindByBookerIdAndStartAfterOrderByStartDesc {
        @Test
        @DisplayName("Должен найти будущие бронирования пользователя")
        public void shouldGetFuture() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(user2.getId(), dateTime, pageable)
                    .get().collect(Collectors.toList());

            assertEquals(2, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет будущих бронирований")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(user1.getId(), dateTime, pageable)
                    .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска бронирований пользователя по статусу")
    class FindByBookerIdAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Должен найти бронирования пользователя со статусом WAITING")
        public void shouldGetWaiting() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(user2.getId(), Status.WAITING, pageable)
                    .get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен найти бронирования пользователя со статусом REJECTED")
        public void shouldGetRejected() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(user2.getId(), Status.REJECTED, pageable)
                    .get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingRejected.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет бронирований с указанным статусом")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(user1.getId(), Status.WAITING, pageable)
                    .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска всех бронирований вещей владельца с сортировкой по дате начала (убывание)")
    class FindByItemOwnerIdOrderByStartDesc {
        @Test
        @DisplayName("Должен найти все бронирования вещей владельца с сортировкой по убыванию даты начала")
        public void shouldGetAll() {
            List<Booking> result = bookingRepository.findByItemOwnerIdOrderByStartDesc(user1.getId(), pageable)
                    .get().collect(Collectors.toList());

            assertEquals(4, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
            assertEquals(bookingCurrent.getId(), result.get(2).getId());
            assertEquals(bookingPast.getId(), result.get(3).getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у владельца нет бронирований вещей")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemOwnerIdOrderByStartDesc(user2.getId(), pageable)
                    .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска текущих бронирований вещей владельца")
    class FindByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc {
        @Test
        @DisplayName("Должен найти текущие бронирования вещей владельца")
        public void shouldGetCurrent() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    user1.getId(), dateTime, dateTime, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingCurrent.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет текущих бронирований вещей владельца")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    user2.getId(), dateTime, dateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска завершённых бронирований вещей владельца по статусу")
    class FindByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Должен найти завершённые бронирования вещей владельца с указанным статусом")
        public void shouldGetPast() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(
                    user1.getId(), dateTime, Status.APPROVED, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingPast.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет завершённых бронирований вещей владельца")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(
                    user2.getId(), dateTime, Status.APPROVED, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска будущих бронирований вещей владельца")
    class FindByItemOwnerIdAndStartAfterOrderByStartDesc {
        @Test
        @DisplayName("Должен найти будущие бронирования вещей владельца")
        public void shouldGetFuture() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(user1.getId(),
                    dateTime, pageable).get().collect(Collectors.toList());

            assertEquals(2, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет будущих бронирований вещей владельца")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(user2.getId(),
                    dateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска бронирований вещей владельца по статусу")
    class FindByItemOwnerIdAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Должен найти бронирования вещей владельца со статусом WAITING")
        public void shouldGetWaiting() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(user1.getId(),
                    Status.WAITING, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен найти бронирования вещей владельца со статусом REJECTED")
        public void shouldGetRejected() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(user1.getId(),
                    Status.REJECTED, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingRejected.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет бронирований вещей владельца с указанным статусом")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(user2.getId(),
                    Status.WAITING, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска последних завершённых бронирований для конкретной вещи")
    class FindByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Должен найти последние завершённые бронирования вещи с указанным статусом")
        public void shouldGetLastBookings() {
            List<Booking> result = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                    item1.getId(), dateTime, Status.APPROVED);

            assertEquals(2, result.size());
            assertEquals(bookingCurrent.getId(), result.get(0).getId());
            assertEquals(bookingPast.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет завершённых бронирований вещи")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                    item1.getId(), dateTime.minusYears(15), Status.APPROVED);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска следующих будущих бронирований для конкретной вещи")
    class FindByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc {
        @Test
        @DisplayName("Должен найти следующие будущие бронирования вещи с указанным статусом")
        public void shouldGetNextBookings() {
            List<Booking> result = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                    item1.getId(), dateTime, Status.WAITING);

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет будущих бронирований вещи")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                    item1.getId(), dateTime, Status.APPROVED);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тестирование поиска завершённых бронирований пользователя для конкретной вещи")
    class FindByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals {
        @Test
        @DisplayName("Должен найти завершённые бронирования пользователя для конкретной вещи")
        public void shouldGetFinishedBookings() {
            List<Booking> result = bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(
                    item1.getId(), user2.getId(), dateTime, Status.APPROVED);

            assertEquals(1, result.size());
            assertEquals(bookingPast.getId(), result.getFirst().getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если нет завершённых бронирований пользователя для вещи")
        public void shouldGetEmpty() {
            List<Booking> result = bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(
                    item1.getId(), user2.getId(), dateTime.minusYears(15), Status.APPROVED);

            assertTrue(result.isEmpty());
        }
    }
}