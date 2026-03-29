package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "ownerId", expression = "java(item.getOwnerId())")
    ItemDto toItemDto(Item item);

    @Mapping(target = "id", expression = "java(itemDto.getId())")
    @Mapping(target = "name", expression = "java(itemDto.getName())")
    @Mapping(target = "ownerId", expression = "java(user.getId())")
    Item toItem(ItemDto itemDto, User user);
}
