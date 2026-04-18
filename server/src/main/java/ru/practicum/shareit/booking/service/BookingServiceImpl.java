package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.StateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository bookingStorage;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto getById(Long userId, Long id) {
        log.info("Вывод бронирования с id {}.", id);

        Booking booking = getBookingById(id);
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new AuthorizationException("Просмотр бронирования доступно только автору или владельцу.");
        }

        return bookingMapper.bookingToBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBookerId(Long userId, State state, Pageable pageable) {
        log.info("Вывод всех бронирований пользователя {} и статусом {}.", userId, state);

        userService.getUserById(userId);

        List<Booking> bookings = null;
        LocalDateTime dateTime = LocalDateTime.now();
        bookings = switch (state) {
            case ALL -> bookingStorage.findByBookerIdOrderByStartDesc(userId, pageable).toList();
            case CURRENT -> bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, dateTime, dateTime, pageable).toList();
            case PAST -> bookingStorage.findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(
                    userId, dateTime, Status.APPROVED, pageable).toList();
            case FUTURE -> bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(
                    userId, dateTime, pageable).toList();
            case WAITING -> bookingStorage.findByBookerIdAndStatusEqualsOrderByStartDesc(
                    userId, Status.WAITING, pageable).toList();
            case REJECTED -> bookingStorage.findByBookerIdAndStatusEqualsOrderByStartDesc(
                    userId, Status.REJECTED, pageable).toList();
        };

        return bookings.stream()
                .map(bookingMapper::bookingToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwnerId(Long userId, State state, Pageable pageable) {
        log.info("Вывод всех вещей пользователя {} и статусом {}.", userId, state);

        userService.getUserById(userId);

        List<Booking> bookings = null;
        LocalDateTime dateTime = LocalDateTime.now();

        bookings = switch (state) {
            case ALL -> bookingStorage.findByItemOwnerIdOrderByStartDesc(userId, pageable).toList();
            case CURRENT -> bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, dateTime, dateTime, pageable).toList();
            case PAST -> bookingStorage.findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(
                    userId, dateTime, Status.APPROVED, pageable).toList();
            case FUTURE -> bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                    userId, dateTime, pageable).toList();
            case WAITING -> bookingStorage.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(
                    userId, Status.WAITING, pageable).toList();
            case REJECTED -> bookingStorage.findByItemOwnerIdAndStatusEqualsOrderByStartDesc(
                    userId, Status.REJECTED, pageable).toList();
        };

        return bookings.stream()
                .map(bookingMapper::bookingToBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Создание бронирования {} пользователем с id {}.", bookingRequestDto, userId);

        validateBookingTime(bookingRequestDto);

        Item item = itemService.getItemById(bookingRequestDto.getItemId());
        validateItemAvailable(item);
        validateNoBookingOverlap(item.getId(), bookingRequestDto);

        User user = userService.getUserById(userId);
        validateNotOwnerBooking(userId, item);

        Booking booking = bookingMapper.requestDtoToBooking(bookingRequestDto, item, user, Status.WAITING);
        return bookingMapper.bookingToBookingResponseDto(bookingStorage.save(booking));
    }

    private void validateBookingTime(BookingRequestDto dto) {
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new BookingException("Недопустимое время брони.");
        }
    }

    private void validateItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new BookingException("Предмет недоступен для бронирования.");
        }
    }

    private void validateNotOwnerBooking(Long userId, Item item) {
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец не может бронировать собственную вещь.");
        }
    }

    private void validateNoBookingOverlap(Long itemId, BookingRequestDto request) {
        List<Booking> allBookings = bookingStorage.findByItemId(itemId);

        boolean hasOverlap = allBookings.stream()
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .anyMatch(booking -> isOverlapping(booking, request));

        if (hasOverlap) {
            throw new BookingException("Предмет недоступен для бронирования. В это время его еще кто-то использует!");
        }
    }

    private boolean isOverlapping(Booking existing, BookingRequestDto newBooking) {
        return existing.getStart().isBefore(newBooking.getEnd())
                && existing.getEnd().isAfter(newBooking.getStart());
    }

    @Override
    @Transactional
    public BookingResponseDto update(Long userId, Long id, Boolean approved) {
        log.info("Обновление статуса бронирования {}.", id);

        Booking repoBooking = getBookingById(id);

        if (!userId.equals(repoBooking.getItem().getOwner().getId())) {
            throw new AuthorizationException("Изменение статуса бронирования доступно только владельцу.");
        }
        if (!repoBooking.getStatus().equals(Status.WAITING)) {
            throw new BookingException("Ответ по бронированию уже дан.");
        }


        repoBooking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingMapper.bookingToBookingResponseDto(bookingStorage.save(repoBooking));
    }

    private Booking getBookingById(Long id) {
        return bookingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с таким id не существует."));
    }

    public State checkStateValid(String state) {
        return State.stringToState(state).orElseThrow(
                () -> new StateException("Unknown state: " + state));
    }
}