package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerIdOrderByStartDesc(Long booker, Pageable pageable);

    Page<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start,
                                                                          LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start,
                                                                            Status status, Pageable pageable);

    Page<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatusEqualsOrderByStartDesc(Long userId, Status status, Pageable pageable);

    Page<Booking> findByItemOwnerIdOrderByStartDesc(Long booker, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start,
                                                                             LocalDateTime end, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start,
                                                                               Status status, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatusEqualsOrderByStartDesc(Long userId, Status status, Pageable pageable);

    List<Booking> findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start, Status status);

    List<Booking> findByItemIdAndStartBeforeAndEndAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start, LocalDateTime end, Status status);

    List<Booking> findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(Long id, Long userId, LocalDateTime end, Status status);

    List<Booking> findByItemIdIn(List<Long> itemIds);

    List<Booking> findByItemId(Long id);

    List<Booking> findByItemIdAndStartAfterAndEndBeforeAndStatusEqualsOrderByStartAsc(Long id, LocalDateTime start, LocalDateTime end, Status status);


}