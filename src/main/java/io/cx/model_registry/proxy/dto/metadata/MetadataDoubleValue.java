package io.cx.model_registry.proxy.dto.metadata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MetadataDoubleValue extends MetadataValue {
    @JsonProperty("double_value")
    @JsonAlias("doubleValue")
    @NotNull(message = "'double_value' must be provided")
    private Double double_value;

    public MetadataDoubleValue(Double value) {
        this.metadataType = "MetadataDoubleValue";
        this.double_value = value;
    }

    public static Optional<Double> optional(Double d) {
        return ofNullable(d)
                .map(Double.class::cast);
    }
}
