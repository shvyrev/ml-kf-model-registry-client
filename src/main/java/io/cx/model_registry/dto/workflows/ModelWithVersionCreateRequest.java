package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelWithVersionCreateRequest {

    @JsonProperty("idempotencyKey")
    private String idempotencyKey;

    @JsonProperty("model")
    @NotNull(message = "'model' must be provided")
    @Valid
    private RegisteredModelCreate model;

    @JsonProperty("version")
    @NotNull(message = "'version' must be provided")
    @Valid
    private ModelVersionCreate version;

    @AssertTrue(message = "'model.name' must be provided")
    @JsonIgnore
    public boolean isModelNameValid() {
        return model != null && model.name() != null && !model.name().isBlank();
    }

    @AssertTrue(message = "'version.name' must be provided")
    @JsonIgnore
    public boolean isVersionNameValid() {
        return version != null && version.name() != null && !version.name().isBlank();
    }
}
