package io.cx.model_registry.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import lombok.*;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVersion extends BaseResource {

    @JsonProperty("registeredModelId")
    private String registeredModelId;

    @JsonProperty("author")
    private String author;

    @JsonProperty("state")
    private ModelVersionState state;
}