package ru.practicum.shareit.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.marks.Create;
import ru.practicum.shareit.marks.Update;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@ToString
public class UserDto {
    Long id;
    String name;
    @NotBlank(groups = Create.class)
    @Email(groups = {Create.class, Update.class})
    String email;
}