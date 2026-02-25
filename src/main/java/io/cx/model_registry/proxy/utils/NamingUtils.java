package io.cx.model_registry.proxy.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Утилиты для преобразования имен.
 */

@Slf4j
public final class NamingUtils {
    private static final Pattern DOT_BOUNDARY =
            Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
    public static final String DOT_REGEX = "\\.";
    public static final String EMPTY_STRING = "";

    /**
     * Преобразует имя класса в dot-формат.
     * Пример: {@code UserConnectionEventMessage -> user.connection.event.message}.
     *
     * @param type класс
     * @return имя в dot-формате
     */
    public static String toDotCase(Class<?> type) {
        return ofNullable(type)
                .map(Class::getSimpleName)
                .map(String::trim)
                .map(s -> DOT_BOUNDARY.matcher(s).replaceAll("."))
                .map(String::toLowerCase)
                .orElse("");
    }

    /**
     * Преобразует имя "корневого" класса в dot-формат для вложенных sealed-классов.
     * Пример: {@code UserConnectionEvents.ConnectionOpened -> user.connection.events}.
     *
     * @param type класс (в том числе вложенный)
     * @return имя корневого класса в dot-формате
     */
    public static String toSealedRootDotCase(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        Class<?> root = type.getDeclaringClass();

        if (root == null) {
            root = type.getEnclosingClass();
        }
        if (root == null) {
            root = type;
        }
        return toDotCase(root);
    }
}
