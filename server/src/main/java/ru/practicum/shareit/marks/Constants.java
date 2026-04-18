package ru.practicum.shareit.marks;

public abstract class Constants {
    public static final String headerUserId = "X-Sharer-User-Id";
    public static final String PAGE_DEFAULT_FROM = "0";
    public static final String PAGE_DEFAULT_SIZE = "10";

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
