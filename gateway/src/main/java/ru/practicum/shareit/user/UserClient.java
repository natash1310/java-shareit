package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

@Service
@Slf4j
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(s -> new HttpComponentsClientHttpRequestFactory())
                .build()
        );
    }

    public ResponseEntity<Object> getAll() {
        log.info("Вывод всех пользователей.");
        return get("");
    }

    public ResponseEntity<Object> getById(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return get("/" + id);
    }

    public ResponseEntity<Object> add(UserDto requestDto) {
        log.info("Добавление пользователя {}", requestDto);
        return post("", requestDto);
    }

    public ResponseEntity<Object> update(Long id, UserDto requestDto) {
        log.info("Обновление пользователя {} с id {}.", requestDto, id);
        return patch("/" + id, requestDto);
    }

    public void delete(Long id) {
        log.info("Удаление пользователя с id {}", id);
        delete("/" + id);
    }
}