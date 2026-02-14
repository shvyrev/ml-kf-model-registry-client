package io.cx.model_registry.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataStructValue extends MetadataValue {
    private String struct_value;

    public MetadataStructValue(String value) {
        this.metadataType = "MetadataStructValue";
        this.struct_value = value;
    }
}
