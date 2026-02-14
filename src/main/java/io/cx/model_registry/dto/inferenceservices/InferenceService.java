package io.cx.model_registry.dto.inferenceservices;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * Сущность InferenceService в Model Registry.
 * <p>
 * Соответствует схеме {@code InferenceService} из OpenAPI спецификации.
 * Представляет развёрнутую версию модели (ModelVersion) из RegisteredModel в ServingEnvironment.
 * Наследует все поля {@code BaseResource} и добавляет поля {@code registeredModelId},
 * {@code servingEnvironmentId}, {@code modelVersionId}, {@code runtime} и {@code desiredState}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenceService extends BaseResource {
    /**
     * Идентификатор модели (RegisteredModel), которую нужно обслуживать.
     */
    @JsonProperty("registeredModelId")
    private String registeredModelId;

    /**
     * Идентификатор окружения обслуживания (ServingEnvironment), в котором развёртывается сервис.
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