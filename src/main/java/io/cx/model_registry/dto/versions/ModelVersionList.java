package io.cx.model_registry.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVersionList extends BaseResourceList<ModelVersion> {
}