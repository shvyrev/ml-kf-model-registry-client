package io.cx.model_registry.dto.inferenceservices;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO для создания нового InferenceService в Model Registry.
 * <p>
 * Соответствует схеме {@code InferenceServiceCreate} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResourceCreate} и добавляет обязательные поля
 * {@code registeredModelId} и {@code servingEnvironmentId}, а также поля из {@code InferenceServiceUpdate}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenceServiceCreate extends BaseResourceCreate {
    /**
     * Идентификатор модели (RegisteredModel), которую нужно обслуживать.
     * Обязательное поле.
     */
    @JsonProperty("registeredModelId")
    @NotBlank(message = "'registeredModelId' must be provided")
    private String registeredModelId;

    /**
     * Идентификатор окружения обслуживания (ServingEnvironment), в котором развёртывается сервис.
     * Обязательное поле.
     */
    @JsonProperty("servingEnvironmentId")
    private String servingEnvironmentId;

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
