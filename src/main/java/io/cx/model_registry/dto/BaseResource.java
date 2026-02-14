package io.cx.model_registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

@Accessors(chain = true, fluent = true)
@Data
public class BaseResource {
    /**
     * Уникальный идентификатор ресурса, сгенерированный сервером.
     */
    @JsonProperty("id")
    private String id;

    /**
     * Имя ресурса, предоставленное клиентом.
     * Должно быть уникальным среди всех экспериментов в рамках экземпляра Model Registry.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Внешний идентификатор из системы клиента (опционально).
     * Если указан, должен быть уникальным среди всех ресурсов в БД.
     */
    @JsonProperty("externalId")
    private String externalId;

    /**
     * Описание ресурса (опционально).
     */
    @JsonProperty("description")
    private String description;

    /**
     * Пользовательские свойства, не определённые схемой.
     */
    @JsonProperty("customProperties")
    private Map<String, MetadataValue> customProperties;

    /**
     * Время создания ресурса в миллисекундах с эпохи Unix (только для чтения).
     */
    @JsonProperty("createTimeSinceEpoch")
    private Long createTimeSinceEpoch;

    /**
     * Время последнего обновления ресурса в миллисекундах с эпохи Unix (только для чтения).
     */
    @JsonProperty("lastUpdateTimeSinceEpoch")
    private Long lastUpdateTimeSinceEpoch;

    /**
     * Возвращает время создания как Instant.
     */
    public Instant getCreateTime() {
        return createTimeSinceEpoch != null ?
                Instant.ofEpochMilli(createTimeSinceEpoch) : null;
    }

    /**
     * Возвращает время последнего обновления как Instant.
     */
    public Instant getLastUpdateTime() {
        return lastUpdateTimeSinceEpoch != null ?
                Instant.ofEpochMilli(lastUpdateTimeSinceEpoch) : null;
    }
}