package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
@Slf4j
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(s -> new HttpComponentsClientHttpRequestFactory())
                .build()
        );
    }

    public ResponseEntity<Object> getById(Long userId, Long id) {
        log.info("Вывод бронирования с id {}.", id);
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> getAllByBookerId(Long userId, State state, Integer from, Integer size) {
        log.info("Вывод всех бронирований пользователя {} и статусом {}.", userId, state);

        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllByOwnerId(Long userId, State state, Integer from, Integer size) {
        log.info("Вывод всех вещей пользователя {} и статусом {}.", userId, state);

        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> add(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Создание бронирования {} пользователем с id {}.", bookingRequestDto, userId);
        return post("", userId, bookingRequestDto);
    }

    public ResponseEntity<Object> update(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования {}.", bookingId);

        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }
}