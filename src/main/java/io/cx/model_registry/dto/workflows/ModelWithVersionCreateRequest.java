package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
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

    @JsonProperty("model")
    private RegisteredModelCreate model;

    @JsonProperty("version")
    private ModelVersionCreate version;
}
