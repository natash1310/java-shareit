package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;

import java.util.List;

public interface BookingService {
    BookingResponseDto getById(Long userId, Long id);

    List<BookingResponseDto> getAllByBookerId(Long userId, State state, Pageable pageable);

    List<BookingResponseDto> getAllByOwnerId(Long userId, State state, Pageable pageable);

    BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto update(Long userId, Long id, Boolean approved);

}