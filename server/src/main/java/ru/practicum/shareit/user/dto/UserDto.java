package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.marks.Create;
import ru.practicum.shareit.marks.Update;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@ToString
public class UserDto {
    Long id;
    String name;
    @NotBlank(groups = Create.class)
    @Email(groups = {Create.class, Update.class})
    String email;
}
