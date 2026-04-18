package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto add(@RequestHeader(Constants.headerUserId) Long userId,
                              @RequestBody ItemRequestAddDto itemRequestCreateDto) {
        return itemRequestService.add(userId, itemRequestCreateDto);
    }

    @GetMapping("/{id}")
    public ItemRequestExtendedDto getById(@RequestHeader(Constants.headerUserId) Long userId,
                                          @PathVariable Long id) {
        return itemRequestService.getById(userId, id);
    }

    @GetMapping
    public List<ItemRequestExtendedDto> getByRequesterId(@RequestHeader(Constants.headerUserId) Long userId) {
        return itemRequestService.getByRequesterId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestExtendedDto> getAll(
            @RequestHeader(Constants.headerUserId) Long userId,
            @RequestParam Integer from,
            @RequestParam Integer size) {
        return itemRequestService.getAll(userId, PageRequest.of(from / size, size));
    }
}
