package ru.practicum.shareit.marks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstantsTest {

    @Test
    void constantsClassShouldBeLoaded() {
        assertNotNull(Constants.headerUserId);
        assertNotNull(Constants.PAGE_DEFAULT_FROM);
        assertNotNull(Constants.PAGE_DEFAULT_SIZE);
    }
}