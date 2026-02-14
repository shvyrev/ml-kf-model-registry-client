package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.versions.ModelVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelWithVersionCreateResult {

    @JsonProperty("model")
    private RegisteredModel model;

    @JsonProperty("version")
    private ModelVersion version;
}
