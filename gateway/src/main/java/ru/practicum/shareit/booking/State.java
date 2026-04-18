package ru.practicum.shareit.booking;

import java.util.Optional;

public enum State {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<State> stringToState(String state) {
        for (State value : State.values()) {
            if (value.name().equals(state.toUpperCase())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
