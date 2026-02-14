package io.cx.model_registry.dto.servemodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DTO для создания нового действия обслуживания модели (ServeModel) в Model Registry.
 * <p>
 * Соответствует схеме {@code ServeModelCreate} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResourceCreate} и добавляет обязательное поле
 * {@code modelVersionId}, а также поле {@code lastKnownState} из {@code ServeModelUpdate}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServeModelCreate extends BaseResourceCreate {

    /**
     * Идентификатор версии модели (ModelVersion), которая была обслуживана в InferenceService.
     * Обязательное поле.
     */
    @JsonProperty("modelVersionId")
    private String modelVersionId;

    /**
     * Последнее известное состояние выполнения действия обслуживания.
     */
    @JsonProperty("lastKnownState")
    private ExecutionState lastKnownState;
}