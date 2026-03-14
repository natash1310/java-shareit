package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto add(Long userId, ItemDto itemDto) {
        userService.get(userId);
        log.info("Создание вещи {} пользователем с id {}.", itemDto, userId);
        itemDto.setOwnerId(userId);
        return ItemMapper.toItemDto(itemStorage.addItem(ItemMapper.toItem(itemDto)));
    }

    @Override
    public ItemDto get(Long itemId) {
        log.info("Вывод вещи с id {}.", itemId);
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getAll() {
        log.info("Вывод вcех имеющихся вещей");
        return itemStorage.getAll().stream().map(ItemMapper::toItemDto).collect(Collectors.toList());

    }

    @Override
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        log.info("Обновление вещи {} с id {} пользователем с id {}.", itemDto, itemId, userId);
        itemDto.setOwnerId(userId);
        itemDto.setId(itemId);
        return ItemMapper.toItemDto(itemStorage.update(ItemMapper.toItem(itemDto)));
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        log.info("Получение всех вещей пользователя с id {}.", ownerId);
        return itemStorage.getItemByOwner(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        text = text.toLowerCase();
        return itemStorage.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean delete(Long id) {
        log.info("Удаление вещи с id {}.", id);
        return itemStorage.deleteItem(id);
    }

}
