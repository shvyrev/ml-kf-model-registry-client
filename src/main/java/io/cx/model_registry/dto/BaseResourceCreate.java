package io.cx.model_registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true, fluent = true)
public class BaseResourceCreate {
    @JsonProperty("name")
    private String name;

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("customProperties")
    private Map<String, @jakarta.validation.Valid MetadataValue> customProperties;
}
