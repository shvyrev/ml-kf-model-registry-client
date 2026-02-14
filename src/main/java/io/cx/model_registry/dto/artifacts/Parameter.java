package io.cx.model_registry.dto.artifacts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Артефакт параметра.
 * <p>
 * Представляет конфигурационный параметр, использованный при обучении или выполнении модели.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter extends Artifact {

    @JsonProperty("value")
    private String value;

    @JsonProperty("parameterType")
    private String parameterType;

    @Override
    public ArtifactType artifactType() {
        return ArtifactType.DOC_ARTIFACT;
    }
}