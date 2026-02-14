package io.cx.model_registry.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * Сущность ServingEnvironment в Model Registry.
 * <p>
 * Соответствует схеме {@code ServingEnvironment} из OpenAPI спецификации.
 * Представляет среду обслуживания моделей (Model Serving Environment) для развёртывания RegisteredModels.
 * Наследует все поля {@code BaseResource}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServingEnvironment extends BaseResource {

}