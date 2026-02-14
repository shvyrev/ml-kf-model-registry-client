package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для обновления артефакта модели машинного обучения.
 * <p>
 * Содержит поля, которые можно изменить у существующего артефакта модели.
 * Включает метаданные формата модели, информацию о хранилище, источнике модели и состоянии.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelArtifactUpdate {

    @JsonProperty("customProperties")
    private Map<String, MetadataValue> customProperties;

    @JsonProperty("description")
    private String description;

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("artifactType")
    private final String artifactType = "model-artifact";

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

    @JsonProperty("state")
    private ArtifactState state;
}