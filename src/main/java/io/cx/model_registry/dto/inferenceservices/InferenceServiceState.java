package io.cx.model_registry.dto.inferenceservices;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Состояние InferenceService в Model Registry.
 * <p>
 * Соответствует схеме {@code InferenceServiceState} из OpenAPI спецификации.
 * Указывает желаемое состояние сервиса вывода.
 * </p>
 */
public enum InferenceServiceState {
    /**
     * Сервис должен быть развёрнут.
     */
    @JsonProperty("DEPLOYED")
    DEPLOYED,

    /**
     * Сервис должен быть остановлен (не развёрнут).
     */
    @JsonProperty("UNDEPLOYED")
    UNDEPLOYED
}