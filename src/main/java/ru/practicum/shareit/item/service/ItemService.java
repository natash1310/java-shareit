package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getByOwner(Long userId);

    List<ItemDto> getAll();

    ItemDto get(Long itemId);

    ItemDto add(Long userId, ItemDto itemDto);

    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    Boolean delete(Long itemId);

    List<ItemDto> search(String text);
}
