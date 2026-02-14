package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.servemodel.ServeModel;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeployModelVersionResult {

    @JsonProperty("servingEnvironment")
    private ServingEnvironment servingEnvironment;

    @JsonProperty("inferenceService")
    private InferenceService inferenceService;

    @JsonProperty("serve")
    private ServeModel serve;
}
