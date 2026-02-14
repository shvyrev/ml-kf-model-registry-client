package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ArtifactType {
    @JsonProperty("model-artifact") MODEL_ARTIFACT,
    @JsonProperty("doc-artifact") DOC_ARTIFACT,
    @JsonProperty("metric") METRIC,
    @JsonProperty("parameter") PARAMETER,
    @JsonProperty("dataset-artifact") DATASET_ARTIFACT,
}
