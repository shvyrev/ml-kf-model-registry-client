package io.cx.model_registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(chain = true, fluent = true)
@Data
public class BaseResourceUpdate {
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
}