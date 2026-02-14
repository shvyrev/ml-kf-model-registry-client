package io.cx.model_registry.dto.metadata;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataIntValue extends MetadataValue {
    @NotBlank(message = "'int_value' must be provided")
    private String int_value;

    public MetadataIntValue(Long value) {
        this.metadataType = "MetadataIntValue";
        this.int_value = value != null ? value.toString() : null;
    }
}
