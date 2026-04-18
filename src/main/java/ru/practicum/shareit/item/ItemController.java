package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marks.Create;
import ru.practicum.shareit.marks.Update;
import ru.practicum.shareit.user.UserController;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemExtendedDto> getByOwnerId(@RequestHeader(UserController.headerUserId) Long userId) {
        return itemService.getByOwnerId(userId);
    }

    @GetMapping("/{id}")
    public ItemExtendedDto getById(@RequestHeader(UserController.headerUserId) Long userId,
                                   @PathVariable Long id) {
        return itemService.getById(userId, id);
    }

    @PostMapping
    public ItemDto add(@RequestHeader(UserController.headerUserId) Long userId,
                       @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(UserController.headerUserId) Long userId,
                          @PathVariable Long id,
                          @Validated(Update.class) @RequestBody ItemDto itemDto) {
        return itemService.update(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("{id}/comment")
    public CommentDto addComment(@RequestHeader(UserController.headerUserId) long userId,
                                 @PathVariable long id,
                                 @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addComment(userId, id, commentRequestDto);
    }
}