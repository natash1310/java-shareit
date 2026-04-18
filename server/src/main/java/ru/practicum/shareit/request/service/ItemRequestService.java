package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Long userId, ItemRequestAddDto itemRequestCreateDto);

    ItemRequestExtendedDto getById(Long userId, Long id);

    List<ItemRequestExtendedDto> getByRequesterId(Long userId);

    List<ItemRequestExtendedDto> getAll(Long userId, Pageable pageable);
}
