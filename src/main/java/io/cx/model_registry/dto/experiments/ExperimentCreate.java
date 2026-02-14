package io.cx.model_registry.dto.experiments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO для создания нового эксперимента в Model Registry.
 * <p>
 * Соответствует схеме {@code ExperimentCreate} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResourceCreate} и добавляет обязательное поле {@code name}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentCreate extends BaseResourceCreate {

    /**
     * Владелец эксперимента (опционально).
     */
    @JsonProperty("owner")
    private String owner;

    /**
     * Состояние эксперимента (опционально).
     */
    @JsonProperty("state")
    private ExperimentState state;

    @AssertTrue(message = "'name' must be provided")
    @JsonIgnore
    public boolean isNameValid() {
        return name() != null && !name().isBlank();
    }
}
