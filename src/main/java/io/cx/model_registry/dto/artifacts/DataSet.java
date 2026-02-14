package io.cx.model_registry.dto.artifacts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Артефакт набора данных.
 * <p>
 * Представляет тренировочные или тестовые данные.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSet extends Artifact {

    @JsonProperty("digest")
    private String digest;

    @JsonProperty("sourceType")
    private String sourceType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("schema")
    private String schema;

    @JsonProperty("profile")
    private String profile;

    @JsonProperty("uri")
    private String uri;

    @Override
    public ArtifactType artifactType() {
        return ArtifactType.DATASET_ARTIFACT;
    }
}
