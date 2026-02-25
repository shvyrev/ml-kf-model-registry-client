package io.cx.model_registry.proxy.dto.versions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ModelVersionState {
    @JsonProperty("LIVE")
    LIVE,
    @JsonProperty("ARCHIVED")
    ARCHIVED
}