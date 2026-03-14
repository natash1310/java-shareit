package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marks.Create;
import ru.practicum.shareit.marks.Update;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final String headerUserId = "X-Sharer-User-Id";

    @GetMapping("/all")
    public List<ItemDto> getAll() {
        return itemService.getAll();
    }

    @PostMapping
    public ItemDto add(@RequestBody @Validated(Create.class) ItemDto itemDto, @RequestHeader(headerUserId) Long userId) {
        return itemService.add(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable Long itemId) {
        return itemService.get(itemId);
    }

    @GetMapping
    public List<ItemDto> getByOwner(@RequestHeader(headerUserId) Long userId) {
        return itemService.getByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getByOwner(@RequestParam String text) {
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Validated(Update.class) @RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(headerUserId) Long userId) {
        return itemService.update(itemDto, itemId, userId);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteItem(@PathVariable Long itemId) {
        return itemService.delete(itemId);
    }
}
