package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.marks.Create;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class ItemDto {
    Long id;

    @NotBlank(groups = Create.class)
    String name;

    @NotBlank(groups = Create.class)
    String description;

    @NotNull(groups = Create.class)
    Boolean available;
    Long ownerId;
    Long requestId;
}