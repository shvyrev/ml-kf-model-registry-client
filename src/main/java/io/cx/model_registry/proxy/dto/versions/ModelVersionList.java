package io.cx.model_registry.proxy.dto.versions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cx.model_registry.proxy.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelVersionList extends BaseResourceList<ModelVersion> {
}