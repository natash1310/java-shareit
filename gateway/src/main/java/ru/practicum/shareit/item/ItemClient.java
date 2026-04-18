package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.model.CommentRequestDto;
import ru.practicum.shareit.item.model.ItemDto;

import java.util.Map;

@Service
@Slf4j
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(s -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getByOwnerId(Long userId, Integer from, Integer size) {
        log.info("Вывод всех вещей пользователя с id {}.", userId);

        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getById(Long userId, Long id) {
        log.info("Вывод вещи с id {}.", id);
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> add(Long userId, ItemDto itemDto) {
        log.info("Создание вещи {} пользователем с id {}.", itemDto, userId);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long id, ItemDto itemDto) {
        log.info("Обновление вещи {} с id {} пользователем с id {}.", itemDto, id, userId);
        return patch("/" + id, userId, itemDto);
    }

    public void delete(Long id) {
        log.info("Удаление вещи с id {}.", id);
        delete("/" + id);
    }

    public ResponseEntity<Object> search(String text, Integer from, Integer size) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);

        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, parameters);
    }

    public ResponseEntity<Object> addComment(Long userId, Long id, CommentRequestDto commentDto) {
        log.info("Добавление комментария пользователем с id {} вещи с id {}.", userId, id);
        return post("/" + id + "/comment", userId, commentDto);
    }
}