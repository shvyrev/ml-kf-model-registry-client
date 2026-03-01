package io.cx.model_registry.proxy.dto.metadata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataStructValue extends MetadataValue {
    @JsonProperty("struct_value")
    @JsonAlias("structValue")
    @NotBlank(message = "'struct_value' must be provided")
    private String struct_value;

    public MetadataStructValue(String value) {
        this.metadataType = "MetadataStructValue";
        this.struct_value = value;
    }
}
