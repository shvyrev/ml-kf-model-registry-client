package io.cx.model_registry.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataDoubleValue extends MetadataValue {
    private Double double_value;

    public MetadataDoubleValue(Double value) {
        this.metadataType = "MetadataDoubleValue";
        this.double_value = value;
    }
}
