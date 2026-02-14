package io.cx.model_registry.dto.metadata;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataBoolValue extends MetadataValue {
    @NotNull(message = "'bool_value' must be provided")
    private Boolean bool_value;

    public MetadataBoolValue(Boolean value) {
        this.metadataType = "MetadataBoolValue";
        this.bool_value = value;
    }
}
