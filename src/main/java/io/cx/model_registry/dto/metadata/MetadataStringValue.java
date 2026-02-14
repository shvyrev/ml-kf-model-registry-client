package io.cx.model_registry.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataStringValue extends MetadataValue {
    private String string_value;

    public MetadataStringValue(String value) {
        this.metadataType = "MetadataStringValue";
        this.string_value = value;
    }
}
