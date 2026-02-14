package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Артефакт метрики.
 * <p>
 * Представляет численное измерение, полученное в ходе обучения или оценки модели.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric extends Artifact {

    @JsonProperty("value")
    private Double value;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("step")
    private Long step;

    @Override
    public ArtifactType artifactType() {
        return ArtifactType.METRIC;
    }
}