package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.marks.Constants;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(Constants.headerUserId) Long userId,
                                          @PathVariable Long id) {
        return bookingClient.getById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBookerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        State stateEnum = State.stringToState(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getAllByBookerId(userId, stateEnum, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwnerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        State stateEnum = State.stringToState(state).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getAllByOwnerId(userId, stateEnum, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(Constants.headerUserId) Long userId,
                                      @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new BookingException("Недопустимое время брони.");
        }
        if (bookingRequestDto.getEnd().isEqual(bookingRequestDto.getStart())) {
            throw new BookingException("Недопустимое время брони.");
        }
        return bookingClient.add(userId, bookingRequestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(Constants.headerUserId) Long userId,
                                         @PathVariable Long id,
                                         @RequestParam Boolean approved) {
        return bookingClient.update(userId, id, approved);
    }
}