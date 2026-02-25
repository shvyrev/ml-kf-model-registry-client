package io.cx.model_registry.proxy.dto.models;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RegisteredModelState {
    @JsonProperty("LIVE") LIVE,
    @JsonProperty("ARCHIVED") ARCHIVED
}