package io.cx.model_registry.dto.experiments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * Эксперимент в Model Registry.
 * <p>
 * Соответствует схеме {@code Experiment} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResource} и добавляет поля {@code owner} и {@code state}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Experiment extends BaseResource {

    /**
     * Владелец эксперимента (опционально).
     */
    @JsonProperty("owner")
    private String owner;

    /**
     * Состояние эксперимента.
     */
    @JsonProperty("state")
    private ExperimentState state;
}