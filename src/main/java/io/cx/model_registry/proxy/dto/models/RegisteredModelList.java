package io.cx.model_registry.proxy.dto.models;

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
public class RegisteredModelList extends BaseResourceList<RegisteredModel> {
}