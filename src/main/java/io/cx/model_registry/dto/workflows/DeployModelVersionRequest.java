package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.servemodel.ServeModelCreate;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentCreate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeployModelVersionRequest {

    @JsonProperty("servingEnvironment")
    private ServingEnvironmentCreate servingEnvironment;

    @JsonProperty("inferenceService")
    private InferenceServiceCreate inferenceService;

    @JsonProperty("serve")
    private ServeModelCreate serve;
}
