package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.cx.model_registry.dto.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Абстрактный базовый класс для всех типов артефактов.
 * <p>
 * Соответствует схеме {@code Artifact} из OpenAPI спецификации Model Registry.
 * Использует полиморфную десериализацию на основе поля {@code artifactType}.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "artifactType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ModelArtifact.class, name = "model-artifact"),
        @JsonSubTypes.Type(value = DocArtifact.class, name = "doc-artifact"),
        @JsonSubTypes.Type(value = DataSet.class, name = "dataset-artifact"),
        @JsonSubTypes.Type(value = Metric.class, name = "metric"),
        @JsonSubTypes.Type(value = Parameter.class, name = "parameter")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Artifact extends BaseResource {

    @JsonProperty("experimentId")
    private String experimentId;

    @JsonProperty("experimentRunId")
    private String experimentRunId;

    @JsonProperty("artifactType")
    private final ArtifactType artifactType = ArtifactType.MODEL_ARTIFACT;

    @JsonProperty("state")
    private ArtifactState state;
}