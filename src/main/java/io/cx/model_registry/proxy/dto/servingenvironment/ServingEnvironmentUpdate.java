package io.cx.model_registry.proxy.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cx.model_registry.proxy.dto.BaseResourceUpdate;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO для обновления существующего ServingEnvironment в Model Registry.
 * <p>
 * Соответствует схеме {@code ServingEnvironmentUpdate} из OpenAPI спецификации.
 * Содержит только те поля, которые могут быть изменены после создания:
 * {@code description}, {@code externalId} и {@code customProperties}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServingEnvironmentUpdate extends BaseResourceUpdate {

}