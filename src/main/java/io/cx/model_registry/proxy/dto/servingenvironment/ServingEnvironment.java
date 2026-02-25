package io.cx.model_registry.proxy.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cx.model_registry.proxy.dto.BaseResource;
import lombok.*;
import lombok.experimental.Accessors;

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