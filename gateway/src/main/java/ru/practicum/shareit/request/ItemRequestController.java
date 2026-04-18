package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.marks.Constants;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(Constants.headerUserId) Long userId,
                                      @Valid @RequestBody ItemRequestAddDto itemRequestAddDto) {
        return itemRequestClient.add(userId, itemRequestAddDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(Constants.headerUserId) Long userId,
                                          @PathVariable Long id) {
        return itemRequestClient.getById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getByRequesterId(@RequestHeader(Constants.headerUserId) Long userId) {
        return itemRequestClient.getByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM, required = false) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE, required = false) @Positive Integer size) {
        return itemRequestClient.getAll(userId, from, size);
    }
}