package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemExtendedDto> getByOwnerId(Long userId) {
        log.info("Вывод всех вещей пользователя с id {}.", userId);

        List<Item> items = itemRepository.getAllByOwnerIdOrderByIdAsc(userId);

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        Map<Item, List<Comment>> commentsByItem = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(Comment::getItem));
        Map<Item, List<Booking>> bookingsByItem = bookingRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(Booking::getItem));

        Map<Long, List<CommentDto>> commentDtosByItem = new HashMap<>();
        for (Item item : items) {
            if (commentsByItem.get(item) == null) {
                commentDtosByItem.put(item.getId(), new ArrayList<>());
            } else {
                commentDtosByItem.put(item.getId(), commentsByItem.get(item)
                        .stream()
                        .map(itemMapper::commentToCommentDto)
                        .collect(Collectors.toList()));
            }
        }

        Map<Long, List<BookingDto>> bookingDtosByItem = new HashMap<>();

        for (Item item : items) {
            if (bookingsByItem.get(item) != null) {
                bookingDtosByItem.put(item.getId(), bookingsByItem.get(item)
                        .stream()
                        .filter(Booking -> Booking.getStatus().equals(Status.APPROVED))
                        .map(itemMapper::bookingToBookingDto)
                        .sorted(Comparator.comparing(BookingDto::getStart))
                        .collect(Collectors.toList()));
            }
        }
        Map<Long, BookingDto> lastBookingByItem = new HashMap<>();
        Map<Long, BookingDto> nextBookingByItem = new HashMap<>();

        for (Item item : items) {
            if (bookingsByItem.get(item) == null) {
                lastBookingByItem.put(item.getId(), null);
                nextBookingByItem.put(item.getId(), null);
            } else {
                nextBookingByItem.put(item.getId(), bookingDtosByItem.get(item.getId())
                        .stream()
                        .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                        .findFirst().orElse(null));

                lastBookingByItem.put(item.getId(), bookingDtosByItem.get(item.getId())
                        .stream()
                        .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                        .findFirst().orElse(null));
            }
        }

        List<ItemExtendedDto> itemExtendedDtos = items.stream()
                .map(item -> itemMapper.toItemExtendedDto(item, null, null, null))
                .collect(Collectors.toList());

        itemExtendedDtos.forEach(ItemDto -> ItemDto.setComments(commentDtosByItem.get(ItemDto.getId())));
        itemExtendedDtos.forEach(ItemDto -> ItemDto.setNextBooking(lastBookingByItem.get(ItemDto.getId())));
        itemExtendedDtos.forEach(ItemDto -> ItemDto.setLastBooking(nextBookingByItem.get(ItemDto.getId())));
        return itemExtendedDtos;
    }

    @Override
    public ItemExtendedDto getById(Long userId, Long id) {
        log.info("Вывод вещи с id {}.", id);

        Item item = getItemById(id);
        if (!Objects.equals(userId, item.getOwner().getId())) {
            return itemMapper.toItemExtendedDto(item, null, null, addComment(item));
        } else {
            return itemMapper.toItemExtendedDto(item, addLastBooking(item),
                    addNextBooking(item), addComment(item));
        }
    }

    @Override
    @Transactional
    public ItemDto add(Long userId, ItemDto itemDto) {
        log.info("Создание вещи {} пользователем с id {}.", itemDto, userId);
        Item item = itemMapper.toItem(itemDto, userService.getUserById(userId));
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long id, ItemDto itemDto) {
        log.info("Обновление вещи {} с id {} пользователем с id {}.", itemDto, id, userId);

        Item repoItem = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещи с таким id не существует."));
        if (!Objects.equals(userId, repoItem.getOwner().getId())) {
            throw new AuthorizationException("Изменение вещи доступно только владельцу.");
        }


        if (itemDto.getName() != null) {
            repoItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            repoItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            repoItem.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.save(repoItem));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление вещи с id {}.", id);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);

        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long id, CommentRequestDto commentRequestDto) {
        log.info("Добавление комментария пользователем с id {} вещи с id {}.", userId, id);

        Comment comment = itemMapper.commentRequestDtoToComment(commentRequestDto,
                LocalDateTime.now(),
                userService.getUserById(userId),
                getItemById(id));

        if (bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(
                id, userId, LocalDateTime.now(), Status.APPROVED).isEmpty()) {
            throw new BookingException("Пользователь не брал данную вещь в аренду.");
        }
        return itemMapper.commentToCommentDto(commentRepository.save(comment));
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещи с таким id не существует."));
    }

    private BookingDto addLastBooking(Item item) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                item.getId(), LocalDateTime.now(), Status.APPROVED);

        if (bookings.isEmpty()) {
            return null;
        } else {
            Booking lastBooking = bookings.getFirst();
            return itemMapper.bookingToBookingDto(lastBooking);
        }
    }

    private BookingDto addNextBooking(Item item) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                item.getId(), LocalDateTime.now(), Status.APPROVED);

        if (bookings.isEmpty()) {
            return null;
        } else {
            Booking nextBooking = bookings.getFirst();
            return itemMapper.bookingToBookingDto(nextBooking);
        }
    }

    private List<CommentDto> addComment(Item item) {
        return commentRepository.findByItemId(item.getId()).stream()
                .map(itemMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }
}