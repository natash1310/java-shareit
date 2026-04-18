package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.marks.Constants;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingResponseDto getById(@RequestHeader(Constants.headerUserId) Long userId,
                                      @PathVariable Long id) {
        return bookingService.getById(userId, id);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBookerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM) Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE) Integer size) {

        State stateEnum = State.stringToState(state).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + state));

        return bookingService.getAllByBookerId(userId, stateEnum, PageRequest.of(from / size, size));
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwnerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM, required = false) Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE, required = false) Integer size) {
        State stateEnum = State.stringToState(state).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + state));

        return bookingService.getAllByOwnerId(userId, stateEnum, PageRequest.of(from / size, size));
    }

    @PostMapping
    public BookingResponseDto add(@RequestHeader(Constants.headerUserId) Long userId,
                                  @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.add(userId, bookingRequestDto);
    }

    @PatchMapping("/{id}")
    public BookingResponseDto update(@RequestHeader(Constants.headerUserId) Long userId,
                                     @PathVariable Long id,
                                     @RequestParam Boolean approved) {
        return bookingService.update(userId, id, approved);
    }
}