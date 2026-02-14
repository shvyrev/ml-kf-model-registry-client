package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import io.cx.model_registry.dto.metadata.MetadataValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DTO для создания артефакта модели машинного обучения.
 * <p>
 * Содержит обязательные и опциональные поля, необходимые для создания нового артефакта модели.
 * Включает информацию о формате модели, хранилище, источнике модели и состоянии.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelArtifactCreate extends BaseResourceCreate {

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