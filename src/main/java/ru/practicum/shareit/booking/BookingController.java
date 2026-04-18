package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.user.UserController;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingResponseDto getById(@RequestHeader(UserController.headerUserId) Long userId,
                                      @PathVariable Long id) {
        return bookingService.getById(userId, id);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBookerId(@RequestHeader(UserController.headerUserId) Long userId,
                                                     @RequestParam(defaultValue = "ALL", required = false) String state) {

        return bookingService.getAllByBookerId(userId, bookingService.checkStateValid(state));
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwnerId(@RequestHeader(UserController.headerUserId) Long userId,
                                                    @RequestParam(defaultValue = "ALL", required = false) String state) {
        return bookingService.getAllByOwnerId(userId, bookingService.checkStateValid(state));
    }

    @PostMapping
    public BookingResponseDto add(@RequestHeader(UserController.headerUserId) Long userId,
                                  @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.add(userId, bookingRequestDto);
    }

    @PatchMapping("/{id}")
    public BookingResponseDto update(@RequestHeader(UserController.headerUserId) Long userId,
                                     @PathVariable Long id,
                                     @RequestParam() Boolean approved) {
        return bookingService.update(userId, id, approved);
    }
}
