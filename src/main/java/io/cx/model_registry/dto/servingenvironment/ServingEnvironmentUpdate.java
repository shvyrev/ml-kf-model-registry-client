package io.cx.model_registry.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

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