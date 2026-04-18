package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<ItemExtendedDto> getByOwnerId(Long userId, Pageable pageable);

    ItemExtendedDto getById(Long userId, Long id);

    ItemDto add(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long id, ItemDto itemDto);

    void delete(Long id);

    List<ItemDto> search(String text, Pageable pageable);

    CommentDto addComment(Long userId, Long id, CommentRequestDto commentRequestDto);

    Item getItemById(Long id);
}