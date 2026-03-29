package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long booker);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusEqualsOrderByStartDesc(Long userId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long booker);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusEqualsOrderByStartDesc(Long userId, Status status);

    List<Booking> findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByItemIdAndStartBeforeAndEndAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start, LocalDateTime end, Status status);

    List<Booking> findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(Long id, Long userId, LocalDateTime end, Status status);

    List<Booking> findByItemIdIn(List<Long> itemIds);

    List<Booking> findByItemId(Long id);

    List<Booking> findByItemIdAndStartAfterAndEndBeforeAndStatusEqualsOrderByStartAsc(Long id, LocalDateTime start, LocalDateTime end, Status status);


}
