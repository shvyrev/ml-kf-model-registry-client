package io.cx.model_registry.dto.experimentruns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentRunCreate extends BaseResourceCreate {

    @JsonProperty("experimentId")
    @NotBlank(message = "'experimentId' must be provided")
    private String experimentId;

    @JsonProperty("startTimeSinceEpoch")
    private Long startTimeSinceEpoch;

    @JsonProperty("endTimeSinceEpoch")
    private Long endTimeSinceEpoch;

    @JsonProperty("status")
    private ExperimentRunStatus status;

    @JsonProperty("state")
    private ExperimentRunState state;

    @JsonProperty("owner")
    private String owner;
}
