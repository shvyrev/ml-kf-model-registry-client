package io.cx.model_registry.dto.artifacts;

import io.cx.model_registry.dto.BaseResourceList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class MetricList extends BaseResourceList<Metric> {
}
