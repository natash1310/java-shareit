package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marks.Constants;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemExtendedDto> getByOwnerId(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam Integer from,
            @RequestParam Integer size) {
        return itemService.getByOwnerId(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/{id}")
    public ItemExtendedDto getById(@RequestHeader(Constants.headerUserId) Long userId,
                                   @PathVariable Long id) {
        return itemService.getById(userId, id);
    }

    @PostMapping
    public ItemDto add(@RequestHeader(Constants.headerUserId) Long userId,
                       @RequestBody ItemDto itemDto) {
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(Constants.headerUserId) Long userId,
                          @PathVariable Long id,
                          @RequestBody ItemDto itemDto) {
        return itemService.update(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text,
            @RequestParam Integer from,
            @RequestParam Integer size) {
        return itemService.search(text, PageRequest.of(from / size, size));
    }

    @PostMapping("{id}/comment")
    public CommentDto addComment(@RequestHeader(Constants.headerUserId) long userId,
                                 @PathVariable long id,
                                 @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addComment(userId, id, commentRequestDto);
    }
}