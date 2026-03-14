package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    List<Item> getItemByOwner(Long userId);

    Item getItemById(Long itemId);

    Item addItem(Item item);

    Item update(Item item);

    Boolean deleteItem(Long itemId);

    List<Item> search(String text);

    List<Item> getAll();
}
