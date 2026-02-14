package io.cx.model_registry.dto.servemodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Состояние выполнения (Execution) в Model Registry.
 * <p>
 * Соответствует схеме {@code ExecutionState} из OpenAPI спецификации.
 * Описывает возможные состояния выполнения, связанные с действием обслуживания модели (ServeModel).
 * </p>
 */
public enum ExecutionState {

    /**
     * Состояние неизвестно (по умолчанию).
     */
    @JsonProperty("UNKNOWN")
    UNKNOWN,

    /**
     * Новое выполнение, ещё не начато.
     */
    @JsonProperty("NEW")
    NEW,

    /**
     * Выполнение запущено и выполняется.
     */
    @JsonProperty("RUNNING")
    RUNNING,

    /**
     * Выполнение успешно завершено.
     */
    @JsonProperty("COMPLETE")
    COMPLETE,

    /**
     * Выполнение завершилось с ошибкой.
     */
    @JsonProperty("FAILED")
    FAILED,

    /**
     * Выполнение пропущено из-за кэшированных результатов.
     */
    @JsonProperty("CACHED")
    CACHED,

    /**
     * Выполнение отменено (не из-за ошибки, а из-за невыполнения предварительных условий).
     */
    @JsonProperty("CANCELED")
    CANCELED
}