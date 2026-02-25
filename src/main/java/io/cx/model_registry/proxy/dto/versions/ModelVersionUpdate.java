package io.cx.model_registry.proxy.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.proxy.dto.BaseResourceUpdate;
import lombok.*;
import lombok.experimental.Accessors;

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