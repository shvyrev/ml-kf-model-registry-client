package io.cx.model_registry.dto.inferenceservices;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DTO для обновления существующего InferenceService в Model Registry.
 * <p>
 * Соответствует схеме {@code InferenceServiceUpdate} из OpenAPI спецификации.
 * Содержит только те поля, которые могут быть изменены после создания.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenceServiceUpdate extends BaseResourceUpdate {
    /**
     * Идентификатор конкретной версии модели (ModelVersion) для обслуживания.
     * Если не указан, будет использована последняя по времени создания версия.
     */
    @JsonProperty("modelVersionId")
    private String modelVersionId;

    /**
     * Среда выполнения модели (например, "tensorflow:2.12", "pytorch:1.14").
     */
    @JsonProperty("runtime")
    private String runtime;

    /**
     * Желаемое состояние сервиса вывода.
     */
    @JsonProperty("desiredState")
    private InferenceServiceState desiredState;
}