package io.cx.model_registry.dto.metadata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "metadataType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MetadataStringValue.class, name = "MetadataStringValue"),
        @JsonSubTypes.Type(value = MetadataIntValue.class, name = "MetadataIntValue"),
        @JsonSubTypes.Type(value = MetadataDoubleValue.class, name = "MetadataDoubleValue"),
        @JsonSubTypes.Type(value = MetadataBoolValue.class, name = "MetadataBoolValue"),
        @JsonSubTypes.Type(value = MetadataStructValue.class, name = "MetadataStructValue")
})
public abstract class MetadataValue {
    @NotBlank(message = "'metadataType' must be provided")
    protected String metadataType;
}
