package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ItemDto>> getAll() {
        return ResponseEntity.ok(itemService.getAll());
    }

    @PostMapping
    public ResponseEntity<ItemDto> add(@RequestBody @Validated(Create.class) ItemDto itemDto,
                                       @RequestHeader(headerUserId) Long userId) {
        return ResponseEntity.ok(itemService.add(userId, itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> get(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.get(itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getByOwner(@RequestHeader(headerUserId) Long userId) {
        return ResponseEntity.ok(itemService.getByOwner(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {  // Исправил имя параметра
        return ResponseEntity.ok(itemService.search(text));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@Validated(Update.class) @RequestBody ItemDto itemDto,
                                          @PathVariable Long itemId,
                                          @RequestHeader(headerUserId) Long userId) {
        return ResponseEntity.ok(itemService.update(itemDto, itemId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.delete(itemId);
        return ResponseEntity.ok().build();
    }
}