package io.cx.model_registry.dto.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.servemodel.ServeModelCreate;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentCreate;
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
public class DeployModelVersionRequest implements IdempotencyKey{

    @JsonProperty("idempotencyKey")
    private String idempotencyKey;

    @JsonProperty("servingEnvironment")
    @NotNull(message = "'servingEnvironment' must be provided")
    @Valid
    private ServingEnvironmentCreate servingEnvironment;

    @JsonProperty("inferenceService")
    @NotNull(message = "'inferenceService' must be provided")
    @Valid
    private InferenceServiceCreate inferenceService;

    @JsonProperty("serve")
    @NotNull(message = "'serve' must be provided")
    @Valid
    private ServeModelCreate serve;

    @AssertTrue(message = "'servingEnvironment.name' must be provided")
    @JsonIgnore
    public boolean isServingEnvironmentNameValid() {
        return servingEnvironment != null
                && servingEnvironment.name() != null
                && !servingEnvironment.name().isBlank();
    }

    @AssertTrue(message = "'inferenceService.registeredModelId' must be provided")
    @JsonIgnore
    public boolean isInferenceServiceRegisteredModelIdValid() {
        return inferenceService != null
                && inferenceService.registeredModelId() != null
                && !inferenceService.registeredModelId().isBlank();
    }

    @AssertTrue(message = "'serve.modelVersionId' must be provided")
    @JsonIgnore
    public boolean isServeModelVersionIdValid() {
        return serve != null && serve.modelVersionId() != null && !serve.modelVersionId().isBlank();
    }
}
