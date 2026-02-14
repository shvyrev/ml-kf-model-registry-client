package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * Артефакт модели машинного обучения.
 * <p>
 * Представляет физическую модель, сохраненную в хранилище, с метаданными о формате модели, источнике и состоянии.
 * Наследует общие свойства артефакта (идентификатор эксперимента, экспериментального запуска) и базового ресурса.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelArtifact extends Artifact {

    @JsonProperty("modelFormatName")
    private String modelFormatName;

    @JsonProperty("storageKey")
    private String storageKey;

    @JsonProperty("storagePath")
    private String storagePath;

    @JsonProperty("modelFormatVersion")
    private String modelFormatVersion;

    @JsonProperty("serviceAccountName")
    private String serviceAccountName;

    @JsonProperty("modelSourceKind")
    private String modelSourceKind;

    @JsonProperty("modelSourceClass")
    private String modelSourceClass;

    @JsonProperty("modelSourceGroup")
    private String modelSourceGroup;

    @JsonProperty("modelSourceId")
    private String modelSourceId;

    @JsonProperty("modelSourceName")
    private String modelSourceName;

    @JsonProperty("uri")
    private String uri;

    @Override
    public ArtifactType artifactType() {
        return ArtifactType.MODEL_ARTIFACT;
    }
}