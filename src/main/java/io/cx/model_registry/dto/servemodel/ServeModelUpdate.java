package io.cx.model_registry.dto.servemodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DTO для обновления существующего действия обслуживания модели (ServeModel) в Model Registry.
 * <p>
 * Соответствует схеме {@code ServeModelUpdate} из OpenAPI спецификации.
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
public class ServeModelUpdate extends BaseResourceUpdate {

    /**
     * Последнее известное состояние выполнения действия обслуживания.
     */
    @JsonProperty("lastKnownState")
    private ExecutionState lastKnownState;
}