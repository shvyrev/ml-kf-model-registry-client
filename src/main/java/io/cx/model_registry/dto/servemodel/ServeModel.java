package io.cx.model_registry.dto.servemodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * Сущность ServeModel в Model Registry.
 * <p>
 * Соответствует схеме {@code ServeModel} из OpenAPI спецификации.
 * Представляет действие обслуживания модели (ModelVersion) в InferenceService.
 * Наследует все поля {@code BaseResource} и добавляет {@code modelVersionId} и {@code lastKnownState}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServeModel extends BaseResource {

    /**
     * Идентификатор версии модели (ModelVersion), которая была обслуживана в InferenceService.
     */
    @JsonProperty("modelVersionId")
    private String modelVersionId;

    /**
     * Последнее известное состояние выполнения действия обслуживания.
     */
    @JsonProperty("lastKnownState")
    private ExecutionState lastKnownState;
}