package io.cx.model_registry.dto.experiments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DTO для обновления существующего эксперимента в Model Registry.
 * <p>
 * Соответствует схеме {@code ExperimentUpdate} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResourceUpdate} и добавляет поля {@code owner} и {@code state}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentUpdate extends BaseResourceUpdate {

    /**
     * Владелец эксперимента (опционально).
     */
    @JsonProperty("owner")
    private String owner;

    /**
     * Состояние эксперимента (опционально).
     */
    @JsonProperty("state")
    private ExperimentState state;
}