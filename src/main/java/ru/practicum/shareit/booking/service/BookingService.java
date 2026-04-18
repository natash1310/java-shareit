package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;

import java.util.List;

public interface BookingService {
    BookingResponseDto getById(Long userId, Long id);

    List<BookingResponseDto> getAllByBookerId(Long userId, State state);

    List<BookingResponseDto> getAllByOwnerId(Long userId, State state);

    BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto update(Long userId, Long id, Boolean approved);

    State checkStateValid(String state);
}
