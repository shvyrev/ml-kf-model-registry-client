package io.cx.model_registry.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVersionCreate extends BaseResourceCreate {
    @JsonProperty("registeredModelId")
    private String registeredModelId;

    @AssertTrue(message = "'name' must be provided")
    @JsonIgnore
    public boolean isNameValid() {
        return name() != null && !name().isBlank();
    }
}
