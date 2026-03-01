package io.cx.model_registry.proxy.dto.metadata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class MetadataStringValue extends MetadataValue {
    @JsonProperty("string_value")
    @JsonAlias("stringValue")
    @NotBlank(message = "'string_value' must be provided")
    private String string_value;

    public MetadataStringValue(String value) {
        this.metadataType = "MetadataStringValue";
        this.string_value = value;
    }

    @JsonIgnore
    public static Optional<String> optional(Object value) {
        return ofNullable(value)
                .map(Object::toString);
    }
}
