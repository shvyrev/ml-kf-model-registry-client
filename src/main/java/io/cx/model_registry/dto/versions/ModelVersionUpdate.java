package io.cx.model_registry.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVersionUpdate extends BaseResourceUpdate {
    @JsonProperty("state")
    private ModelVersionState state;

    @JsonProperty("author")
    private String author;
}