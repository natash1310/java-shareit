package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.marks.Constants;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционное тестирование REST-контроллера вещей")
public class ItemFullContextTest {
    private final UserController userController;
    private final ItemController itemController;
    private final BookingController bookingController;
    private final BookingService bookingService;

    private void checkItemExtendedDto(ItemExtendedDto itemFromController, ItemDto itemDto) {
        assertEquals(itemFromController.getId(), itemDto.getId());
        assertEquals(itemFromController.getName(), itemDto.getName());
        assertEquals(itemFromController.getDescription(), itemDto.getDescription());
        assertEquals(itemFromController.getAvailable(), itemDto.getAvailable());
        assertEquals(itemFromController.getOwnerId(), itemDto.getOwnerId());
        assertEquals(itemFromController.getRequestId(), itemDto.getRequestId());
    }

    private void checkItemExtendedDtoBooking(ItemExtendedDto itemFromController,
                                             BookingResponseDto lastBookingResponseDto,
                                             BookingResponseDto nextBookingResponseDto) {
        assertEquals(itemFromController.getLastBooking().getId(), lastBookingResponseDto.getId());
        assertEquals(itemFromController.getLastBooking().getBookerId(), lastBookingResponseDto.getBooker().getId());
        assertEquals(itemFromController.getLastBooking().getStart(), lastBookingResponseDto.getStart());
        assertEquals(itemFromController.getLastBooking().getEnd(), lastBookingResponseDto.getEnd());

        assertEquals(itemFromController.getNextBooking().getId(), nextBookingResponseDto.getId());
        assertEquals(itemFromController.getNextBooking().getBookerId(), nextBookingResponseDto.getBooker().getId());
        assertEquals(itemFromController.getNextBooking().getStart(), nextBookingResponseDto.getStart());
        assertEquals(itemFromController.getNextBooking().getEnd(), nextBookingResponseDto.getEnd());
    }

    private void checkItemDto(ItemDto itemDtoFromController, ItemDto itemDto) {
        assertEquals(itemDtoFromController.getId(), itemDto.getId());
        assertEquals(itemDtoFromController.getName(), itemDto.getName());
        assertEquals(itemDtoFromController.getDescription(), itemDto.getDescription());
        assertEquals(itemDtoFromController.getAvailable(), itemDto.getAvailable());
        assertEquals(itemDtoFromController.getOwnerId(), itemDto.getOwnerId());
        assertEquals(itemDtoFromController.getRequestId(), itemDto.getRequestId());
    }

    @Nested
    @DisplayName("Интеграционное тестирование создания вещи")
    class Add {
        @Test
        @DisplayName("Должен успешно создать вещь в контексте БД")
        void shouldAdd() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(1, itemsFromController.size());

            ItemExtendedDto itemFromController = itemsFromController.get(0);

            checkItemExtendedDto(itemFromController, itemDto);
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке создания вещи с несуществующим владельцем")
        void shouldThrowExceptionIfItemOwnerIdNotFound() {
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(10L)
                    .requestId(null)
                    .build();
            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.add(10L, itemDto));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование получения вещей владельца")
    class GetByOwner {
        @Test
        @DisplayName("Должен успешно получить все вещи владельца")
        void shouldGet() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2")
                    .description("Test item description 2")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto2.getOwnerId(), itemDto2);

            ItemDto itemDto3 = ItemDto.builder()
                    .id(3L)
                    .name("Test item 3")
                    .description("Test item description 3")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto3.getOwnerId(), itemDto3);

            List<ItemExtendedDto> itemsFromController1 = itemController.getByOwnerId(
                    userDto1.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(2, itemsFromController1.size());

            ItemExtendedDto itemFromController1 = itemsFromController1.get(0);
            ItemExtendedDto itemFromController3 = itemsFromController1.get(1);

            checkItemExtendedDto(itemFromController1, itemDto1);
            checkItemExtendedDto(itemFromController3, itemDto3);

            List<ItemExtendedDto> itemsFromController2 = itemController.getByOwnerId(
                    userDto2.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(1, itemsFromController2.size());

            ItemExtendedDto itemFromController2 = itemsFromController2.getFirst();

            checkItemExtendedDto(itemFromController2, itemDto2);
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у владельца нет вещей")
        void shouldGetIfEmpty() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertTrue(itemsFromController.isEmpty());
        }

        @Test
        @DisplayName("Должен получить вещи владельца с корректными датами бронирований и комментариями")
        void shouldHaveBookingDateAndComments() {
            LocalDateTime now = LocalDateTime.now();

            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2")
                    .description("Test item description 2")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto2.getOwnerId(), itemDto2);

            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(now.plusDays(5))
                    .end(now.plusDays(10))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.add(userDto2.getId(), bookingRequestDto2);
            bookingController.update(userDto1.getId(), bookingResponseDto2.getId(), true);

            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(now.minusDays(45))
                    .end(now.minusDays(40))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto1 = bookingService.add(userDto2.getId(), bookingRequestDto1);

            bookingController.update(userDto1.getId(), bookingResponseDto1.getId(), true);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addComment(userDto2.getId(), itemDto1.getId(), commentRequestDto);

            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto1.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(2, itemsFromController.size());

            ItemExtendedDto itemFromController1 = itemsFromController.get(0);
            ItemExtendedDto itemFromController2 = itemsFromController.get(1);

            assertEquals(itemFromController1.getId(), itemDto1.getId());

            assertNotNull(itemFromController1.getLastBooking());
            assertNotNull(itemFromController1.getNextBooking());

            assertEquals(itemFromController1.getLastBooking().getId(), bookingResponseDto1.getId());
            assertEquals(itemFromController1.getNextBooking().getId(), bookingResponseDto2.getId());

            List<CommentDto> commentsItem1 = itemFromController1.getComments();

            assertEquals(1, commentsItem1.size());
            CommentDto commentDto = commentsItem1.getFirst();

            assertEquals(commentDto.getText(), commentRequestDto.getText());
            assertEquals(commentDto.getAuthorName(), userDto2.getName());

            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());

            List<CommentDto> commentsItem2 = itemFromController2.getComments();

            assertTrue(commentsItem2.isEmpty());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование получения вещи по идентификатору")
    class GetById {
        @Test
        @DisplayName("Должен успешно получить вещь по существующему идентификатору")
        void shouldGet() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            ItemExtendedDto itemFromController = itemController.getById(userDto.getId(), itemDto.getId());

            checkItemExtendedDto(itemFromController, itemDto);
        }

        @Test
        @DisplayName("Должен выбросить исключение при поиске несуществующей вещи")
        void shouldThrowExceptionIfItemIdNotFound() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.getById(userDto.getId(), 10L));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
        }

        @Test
        @DisplayName("При запросе вещи владельцем должны возвращаться даты бронирований и комментарии")
        void shouldRequestByOwnerHaveBookingDateAndComments() {
            LocalDateTime now = LocalDateTime.now();

            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2")
                    .description("Test item description 2")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto2.getOwnerId(), itemDto2);

            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(now.plusDays(5))
                    .end(now.plusDays(10))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.add(userDto2.getId(), bookingRequestDto2);
            bookingController.update(userDto1.getId(), bookingResponseDto2.getId(), true);

            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(now.minusDays(45))
                    .end(now.minusDays(40))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto1 = bookingService.add(userDto2.getId(), bookingRequestDto1);
            bookingController.update(userDto1.getId(), bookingResponseDto1.getId(), true);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addComment(userDto2.getId(), itemDto1.getId(), commentRequestDto);

            ItemExtendedDto itemFromController1 = itemController.getById(userDto1.getId(), itemDto1.getId());

            assertEquals(itemFromController1.getId(), itemDto1.getId());
            assertNotNull(itemFromController1.getLastBooking());
            assertNotNull(itemFromController1.getNextBooking());

            assertEquals(itemFromController1.getLastBooking().getId(), bookingResponseDto1.getId());
            assertEquals(itemFromController1.getNextBooking().getId(), bookingResponseDto2.getId());

            List<CommentDto> commentsItem1 = itemFromController1.getComments();

            assertEquals(1, commentsItem1.size());
            CommentDto comment = commentsItem1.getFirst();

            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());

            ItemExtendedDto itemFromController2 = itemController.getById(userDto1.getId(), itemDto2.getId());

            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());

            List<CommentDto> commentsItem2 = itemFromController2.getComments();

            assertTrue(commentsItem2.isEmpty());
        }

        @Test
        @DisplayName("При запросе вещи не владельцем не должны возвращаться даты бронирований, но должны комментарии")
        void shouldRequestByNoOwnerHaveNotBookingDateAndHaveComments() {
            LocalDateTime now = LocalDateTime.now();

            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2")
                    .description("Test item description 2")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto2.getOwnerId(), itemDto2);

            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(now.plusDays(5))
                    .end(now.plusDays(10))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.add(userDto2.getId(), bookingRequestDto2);
            bookingController.update(userDto1.getId(), bookingResponseDto2.getId(), true);

            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(now.minusDays(45))
                    .end(now.minusDays(40))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto1 = bookingService.add(userDto2.getId(), bookingRequestDto1);
            bookingController.update(userDto1.getId(), bookingResponseDto1.getId(), true);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addComment(userDto2.getId(), itemDto1.getId(), commentRequestDto);

            ItemExtendedDto itemFromController1 = itemController.getById(userDto2.getId(), itemDto1.getId());

            assertEquals(itemFromController1.getId(), itemDto1.getId());

            assertNull(itemFromController1.getLastBooking());
            assertNull(itemFromController1.getNextBooking());

            List<CommentDto> commentsItem1 = itemFromController1.getComments();

            assertEquals(1, commentsItem1.size());
            CommentDto comment = commentsItem1.getFirst();

            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());

            ItemExtendedDto itemFromController2 = itemController.getById(userDto2.getId(), itemDto2.getId());

            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());

            List<CommentDto> commentsItem2 = itemFromController2.getComments();

            assertTrue(commentsItem2.isEmpty());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование обновления вещи")
    class Update {
        @Test
        @DisplayName("Должен успешно обновить данные существующей вещи владельцем")
        void shouldUpdate() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Update test item 1")
                    .description("Update test item description 1")
                    .available(false)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.update(itemDto2.getOwnerId(), itemDto1.getId(), itemDto2);

            ItemExtendedDto itemFromController = itemController.getById(userDto.getId(), itemDto1.getId());

            assertEquals(itemFromController.getId(), itemDto1.getId());
            assertEquals(itemFromController.getName(), itemDto2.getName());
            assertEquals(itemFromController.getDescription(), itemDto2.getDescription());
            assertEquals(itemFromController.getAvailable(), itemDto2.getAvailable());
            assertEquals(itemFromController.getOwnerId(), itemDto2.getOwnerId());
            assertEquals(itemFromController.getRequestId(), itemDto2.getRequestId());
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке обновления вещи не владельцем")
        void shouldThrowExceptionIfItemOwnerIdForbidden() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Update test item 1")
                    .description("Update test item description 1")
                    .available(false)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();

            AuthorizationException exception = assertThrows(AuthorizationException.class, () -> itemController.update(itemDto2.getOwnerId(), itemDto1.getId(), itemDto2));
            assertEquals("Изменение вещи доступно только владельцу.", exception.getMessage());

            ItemExtendedDto itemFromController = itemController.getById(userDto1.getId(), itemDto1.getId());

            checkItemExtendedDto(itemFromController, itemDto1);
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование удаления вещи")
    class Delete {
        @Test
        @DisplayName("Должен успешно удалить существующую вещь")
        void shouldDelete() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.add(userDto.getId(), itemDto);

            itemController.delete(itemDto.getId());

            assertTrue(itemController.getByOwnerId(userDto.getId(),
                            Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                            Integer.parseInt(Constants.PAGE_DEFAULT_SIZE))
                    .isEmpty());
        }

        @Test
        @DisplayName("Должен корректно обработать удаление несуществующей вещи")
        void shouldDeleteIfItemIdNotFound() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.getById(userDto.getId(), 10L));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование поиска вещей по тексту")
    class Search {
        @Test
        @DisplayName("Должен успешно найти доступные вещи по тексту поиска")
        void shouldSearch() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1 SeCREt")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto1.getOwnerId(), itemDto1);

            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2 SeCREt")
                    .description("Test item description 2 SeCREt")
                    .available(false)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto2.getOwnerId(), itemDto2);

            ItemDto itemDto3 = ItemDto.builder()
                    .id(3L)
                    .name("Test item 3")
                    .description("Test item description 3 SeCREt")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto3.getOwnerId(), itemDto3);

            ItemDto itemDto4 = ItemDto.builder()
                    .id(4L)
                    .name("Test item 4")
                    .description("Test item description 4")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto4.getOwnerId(), itemDto4);

            List<ItemDto> itemsFromController = itemController.search(
                    "sEcrEt",
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(2, itemsFromController.size());

            ItemDto itemFromController1 = itemsFromController.get(0);
            ItemDto itemFromController2 = itemsFromController.get(1);

            checkItemDto(itemFromController1, itemDto1);
            checkItemDto(itemFromController2, itemDto3);
        }

        @Test
        @DisplayName("Должен вернуть пустой список при пустой строке поиска")
        void shouldSearchIfEmpty() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("Test user")
                    .email("tester@yandex.ru")
                    .build();
            userController.add(userDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            List<ItemDto> itemsFromController = itemController.search(
                    " ",
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertTrue(itemsFromController.isEmpty());
        }
    }

    @Nested
    @DisplayName("Интеграционное тестирование добавления комментария к вещи")
    class AddComment {
        @Test
        @DisplayName("Должен успешно добавить комментарий пользователем, бравшим вещь в аренду")
        void shouldCreate() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.now().minusHours(2))
                    .end(LocalDateTime.now().minusHours(1))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.add(userDto2.getId(), bookingRequestDto);
            bookingController.update(userDto1.getId(), bookingResponseDto.getId(), true);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addComment(userDto2.getId(), itemDto.getId(), commentRequestDto);

            ItemExtendedDto item = itemController.getById(userDto1.getId(), itemDto.getId());

            List<CommentDto> comments = item.getComments();

            assertEquals(1, comments.size());
            CommentDto comment = comments.getFirst();

            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке добавления комментария пользователем, не бравшим вещь")
        void shouldThrowExceptionIfNoBooking() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");

            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addComment(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке добавления комментария до завершения бронирования")
        void shouldThrowExceptionIfBookingNotFinished() {
            LocalDateTime now = LocalDateTime.now();

            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(now.plusDays(5))
                    .end(now.plusDays(10))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.add(userDto2.getId(), bookingRequestDto);
            bookingController.update(userDto1.getId(), bookingResponseDto.getId(), true);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");

            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addComment(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }


        @Test
        @DisplayName("Должен выбросить исключение при попытке добавления комментария при неподтверждённом бронировании")
        void shouldThrowExceptionIfBookingNotApproved() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.add(userDto1);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.add(userDto2);

            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.add(userDto2.getId(), bookingRequestDto);

            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");

            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addComment(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }
    }
}