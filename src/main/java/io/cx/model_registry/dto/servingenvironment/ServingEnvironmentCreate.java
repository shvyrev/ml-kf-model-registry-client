package io.cx.model_registry.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO для создания нового ServingEnvironment в Model Registry.
 * <p>
 * Соответствует схеме {@code ServingEnvironmentCreate} из OpenAPI спецификации.
 * Наследует все поля {@code BaseResourceCreate} и добавляет обязательное поле {@code name}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServingEnvironmentCreate extends BaseResourceCreate {

    @AssertTrue(message = "'name' must be provided")
    @JsonIgnore
    public boolean isNameValid() {
        return name() != null && !name().isBlank();
    }
}
