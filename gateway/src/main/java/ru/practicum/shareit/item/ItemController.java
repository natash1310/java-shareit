package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.CommentRequestDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.marks.Create;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getByOwnerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM, required = false) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE, required = false) @Positive Integer size) {
        return itemClient.getByOwnerId(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(Constants.headerUserId) Long userId,
                                          @PathVariable Long id) {
        return itemClient.getById(userId, id);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(Constants.headerUserId) Long userId,
                                      @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemClient.add(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(Constants.headerUserId) Long userId,
                                         @PathVariable Long id,
                                         @RequestBody ItemDto itemDto) {
        return itemClient.update(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemClient.delete(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam String text,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_FROM, required = false) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = Constants.PAGE_DEFAULT_SIZE, required = false) @Positive Integer size) {
        return itemClient.search(text, from, size);
    }

    @PostMapping("{id}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(Constants.headerUserId) Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemClient.addComment(userId, id, commentRequestDto);
    }
}