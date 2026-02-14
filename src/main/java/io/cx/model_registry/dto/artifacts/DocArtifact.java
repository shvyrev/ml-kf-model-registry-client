package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Документационный артефакт.
 * <p>
 * Представляет документ, связанный с экспериментом или моделью.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocArtifact extends Artifact {

    @JsonProperty("uri")
    private String uri;

    @Override
    public ArtifactType artifactType() {
        return ArtifactType.DOC_ARTIFACT;
    }
}