package io.cx.model_registry.dto.experimentruns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
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
public class ExperimentRunUpdate extends BaseResourceUpdate {

    @JsonProperty("endTimeSinceEpoch")
    private Long endTimeSinceEpoch;

    @JsonProperty("status")
    private ExperimentRunStatus status;

    @JsonProperty("state")
    private ExperimentRunState state;

    @JsonProperty("owner")
    private String owner;
}
