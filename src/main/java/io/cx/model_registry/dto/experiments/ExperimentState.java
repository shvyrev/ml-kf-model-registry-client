package io.cx.model_registry.dto.experiments;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Состояние эксперимента в Model Registry.
 * <p>
 * Соответствует схеме {@code ExperimentState} из OpenAPI спецификации.
 * </p>
 */
public enum ExperimentState {
    /**
     * Эксперимент существует и активен.
     */
    @JsonProperty("LIVE")
    LIVE,

    /**
     * Эксперимент заархивирован.
     */
    @JsonProperty("ARCHIVED")
    ARCHIVED
}