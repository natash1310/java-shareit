package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemStorageInMemory implements ItemStorage {
    public final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private long idCount = 1;

    @Override
    public Item addItem(Item item) {
        item.setId(idCount++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getItemByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(Long itemId) {
        if (items.containsKey(itemId)) {
            return items.get(itemId);
        } else {
            throw new NotFoundException("Вещи с таким id не существует.");
        }
    }

    @Override
    public Item update(Item item) {
        userService.get(item.getOwnerId());
        Item repoItem = getItemById(item.getId());

        if (!Objects.equals(item.getOwnerId(), repoItem.getOwnerId())) {
            throw new AuthorizationException("Изменение вещи доступно только владельцу.");
        }
        if (item.getName() != null) {
            repoItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            repoItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            repoItem.setAvailable(item.getAvailable());
        }
        return repoItem;
    }

    @Override
    public Boolean deleteItem(Long itemId) {
        items.remove(itemId);
        return true;
    }

    @Override
    public List<Item> search(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text) || item.getDescription().toLowerCase()
                        .contains(text))
                .collect(Collectors.toList());
    }
}
