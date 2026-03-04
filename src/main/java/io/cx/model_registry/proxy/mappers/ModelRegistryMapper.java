package io.cx.model_registry.proxy.mappers;

import io.cx.model_registry.proxy.dto.BaseResourceList;
import io.cx.model_registry.proxy.dto.artifacts.*;
import io.cx.model_registry.proxy.dto.metadata.MetadataBoolValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataDoubleValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataIntValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataStringValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataStructValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataValue;
import io.cx.model_registry.proxy.dto.models.*;
import io.cx.model_registry.proxy.dto.versions.ModelVersion;
import io.cx.model_registry.proxy.dto.versions.ModelVersionCreate;
import io.cx.model_registry.proxy.dto.versions.ModelVersionList;
import io.cx.model_registry.proxy.dto.versions.ModelVersionState;
import io.cx.model_registry.proxy.dto.versions.ModelVersionUpdate;
import io.cx.platform.events.models.ModelInfo;
import io.cx.platform.events.models.commands.CreateModelCommandPayload;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.models.commands.UpdateModelCommandPayload;
import io.cx.platform.events.artifacts.ArtifactPayload;
import io.cx.platform.events.artifacts.commands.CreateModelArtifactCommandPayload;
import io.cx.platform.events.artifacts.commands.CreateModelVersionArtifactCommandPayload;
import io.cx.platform.events.artifacts.commands.UpdateModelArtifactCommandPayload;
import io.cx.platform.events.artifacts.commands.UpdateModelVersionArtifactCommandPayload;
import io.cx.platform.events.artifacts.commands.UpsertExperimentRunArtifactCommandPayload;
import io.cx.platform.events.artifacts.commands.UpsertModelVersionArtifactCommandPayload;
import io.cx.platform.events.modelversions.ModelVersionInfo;
import io.cx.platform.events.modelversions.commands.CreateModelVersionCommandPayload;
import io.cx.platform.events.modelversions.commands.ModelVersionEventsCommand;
import io.cx.platform.events.modelversions.commands.UpdateModelVersionCommandPayload;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static io.cx.model_registry.proxy.Const.Common.DELIMITER;
import static io.cx.model_registry.proxy.Const.ModelRegistryMapper.DISPLAY_NAME_CUSTOM_PROPERTIES_KEY;
import static io.cx.model_registry.proxy.Const.ModelRegistryMapper.LABELS_CUSTOM_PROPERTIES_KEY;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

@Slf4j
@ApplicationScoped
public class ModelRegistryMapper {


    public ModelInfo toModelInfo(RegisteredModel registeredModel) {
        log.info("$ toModelInfo() called with: registeredModel = [{}]", JsonObject.mapFrom(registeredModel).encodePrettily());

        Objects.requireNonNull(registeredModel, "registeredModel must not be null");
        var customProperties = registeredModel.customProperties() != null
                ? registeredModel.customProperties()
                : new HashMap<String, MetadataValue>();

        MetadataStringValue modelName = (MetadataStringValue) customProperties
                .getOrDefault(DISPLAY_NAME_CUSTOM_PROPERTIES_KEY, new MetadataStringValue());

        MetadataStringValue labelCustomProperty = (MetadataStringValue) customProperties.
                getOrDefault(LABELS_CUSTOM_PROPERTIES_KEY, new MetadataStringValue());

        List<String> labels = parseLabels(labelCustomProperty);
        return new ModelInfo(
                registeredModel.id(),
                modelName.string_value(),
                registeredModel.description(),
                labels,
                registeredModel.state().toString(),
                registeredModel.createTimeSinceEpoch(),
                registeredModel.lastUpdateTimeSinceEpoch());
    }


    private List<String> parseLabels(MetadataStringValue value) {
        return ofNullable(value.string_value())
                .map(s -> s.split(DELIMITER))
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    public List<ModelInfo> toModelInfoList(RegisteredModelList values) {
        return ofNullable(values)
                .map(BaseResourceList::items)
                .filter(not(List::isEmpty))
                .map(items -> items.stream()
                        .filter(Objects::nonNull)
                        .map(this::toModelInfo)
                        .toList())
                .orElseGet(Collections::emptyList);
    }

    public ModelVersionInfo toModelVersionInfo(ModelVersion modelVersion) {
        Objects.requireNonNull(modelVersion, "modelVersion must not be null");

        String displayName = metadataString(modelVersion.customProperties(), DISPLAY_NAME_CUSTOM_PROPERTIES_KEY)
                .orElse(modelVersion.name());

        return new ModelVersionInfo(
                modelVersion.id(),
                modelVersion.registeredModelId(),
                displayName,
                modelVersion.description(),
                modelVersion.author(),
                ofNullable(modelVersion.state()).map(Enum::name).orElse(null),
                modelVersion.createTimeSinceEpoch(),
                modelVersion.lastUpdateTimeSinceEpoch()
        );
    }

    public List<ModelVersionInfo> toModelVersionInfoList(ModelVersionList values) {
        return ofNullable(values)
                .map(BaseResourceList::items)
                .filter(not(List::isEmpty))
                .map(items -> items.stream()
                        .filter(Objects::nonNull)
                        .map(this::toModelVersionInfo)
                        .toList())
                .orElseGet(Collections::emptyList);
    }

    public RegisteredModelCreate toCreateModelRequest(ModelEventsCommand.CreateModelCommand command) {
        CreateModelCommandPayload payload = command.payload();

        var modelName = UUID.randomUUID().toString();

        HashMap<String, @Valid MetadataValue> props = new HashMap<>();
        props.put(DISPLAY_NAME_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(command.payload().name()));

        ofNullable(command.payload())
                .map(CreateModelCommandPayload::labels)
                .map(values -> String.join(DELIMITER, values))
                .ifPresent(s -> props.put(LABELS_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(s)));

        return (RegisteredModelCreate) new RegisteredModelCreate()
                .owner(command.userId())
                .state(RegisteredModelState.LIVE)
                .name(modelName)
                .description(payload.description())
                .customProperties(props);
    }

    public RegisteredModelUpdate toUpdateModelRequest(ModelEventsCommand.UpdateModelCommand command) {
        log.info("$ toUpdateModelRequest() called with: command = [{}]", command);

        UpdateModelCommandPayload payload = command.payload();
        log.info("name : " + payload.name());

        HashMap<String, @Valid MetadataValue> props = new HashMap<>();

        ofNullable(payload)
                .map(UpdateModelCommandPayload::name)
                .ifPresent(s -> props.put(DISPLAY_NAME_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(s)));

        ofNullable(payload)
                .map(UpdateModelCommandPayload::labels)
                .map(values -> String.join(DELIMITER, values))
                .ifPresent(s -> props.put(LABELS_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(s)));

        log.info("$ props: {}", props);

        if (payload == null) {
            throw new IllegalArgumentException("update request is empty");
        }

        return (RegisteredModelUpdate) new RegisteredModelUpdate()
                .owner(payload.owner())
                .state(parseState(payload.state()))
                .description(payload.description())
                .externalId(payload.externalId())
                .customProperties(props);
    }

    public ModelVersionCreate toCreateModelVersionRequest(ModelVersionEventsCommand.CreateModelVersionCommand command) {
        CreateModelVersionCommandPayload payload = command.payload();

        HashMap<String, @Valid MetadataValue> props = new HashMap<>();
        ofNullable(payload)
                .map(CreateModelVersionCommandPayload::name)
                .ifPresent(s -> props.put(DISPLAY_NAME_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(s)));

        ofNullable(payload)
                .map(CreateModelVersionCommandPayload::labels)
                .map(values -> String.join(DELIMITER, values))
                .ifPresent(s -> props.put(LABELS_CUSTOM_PROPERTIES_KEY, new MetadataStringValue(s)));

        ModelVersionCreate request = new ModelVersionCreate();
        request.registeredModelId(payload.modelId());
        request.name(UUID.randomUUID().toString());
        request.description(payload.description());
        request.customProperties(props);
        request.author(payload.author());
        return request;
    }

    public ModelVersionUpdate toUpdateModelVersionRequest(ModelVersionEventsCommand.UpdateModelVersionCommand command) {
        UpdateModelVersionCommandPayload payload = command.payload();
        if (payload == null) {
            throw new IllegalArgumentException("update model version request is empty");
        }

        return (ModelVersionUpdate) new ModelVersionUpdate()
                .author(payload.author())
                .state(parseModelVersionState(payload.state()))
                .description(payload.description());
    }

    public ArtifactPayload toArtifactPayload(Artifact artifact) {
        Objects.requireNonNull(artifact, "artifact must not be null");

        String uri = null;
        String digest = null;
        String sourceType = null;
        String source = null;
        String schema = null;
        String profile = null;
        Object value = null;
        Long timestamp = null;
        Long step = null;
        String parameterType = null;

        if (artifact instanceof ModelArtifact modelArtifact) {
            uri = modelArtifact.uri();
        } else if (artifact instanceof DocArtifact docArtifact) {
            uri = docArtifact.uri();
        } else if (artifact instanceof DataSet dataSet) {
            uri = dataSet.uri();
            digest = dataSet.digest();
            sourceType = dataSet.sourceType();
            source = dataSet.source();
            schema = dataSet.schema();
            profile = dataSet.profile();
        } else if (artifact instanceof Metric metric) {
            value = metric.value();
            timestamp = metric.timestamp();
            step = metric.step();
        } else if (artifact instanceof Parameter parameter) {
            value = parameter.value();
            parameterType = parameter.parameterType();
        }

        return new ArtifactPayload(
                artifact.id(),
                artifact.name(),
                artifact.externalId(),
                artifact.description(),
                toEventArtifactType(artifact.artifactType()),
                toEventArtifactState(artifact.state()),
                toEventCustomProperties(artifact.customProperties()),
                artifact.experimentId(),
                artifact.experimentRunId(),
                uri,
                digest,
                sourceType,
                source,
                schema,
                profile,
                value,
                timestamp,
                step,
                parameterType,
                artifact.createTimeSinceEpoch(),
                artifact.lastUpdateTimeSinceEpoch()
        );
    }

    public io.cx.platform.events.artifacts.ModelArtifactPayload toModelArtifactPayload(ModelArtifact artifact) {
        Objects.requireNonNull(artifact, "model artifact must not be null");

        return new io.cx.platform.events.artifacts.ModelArtifactPayload(
                artifact.id(),
                artifact.name(),
                artifact.externalId(),
                artifact.description(),
                toEventArtifactType(artifact.artifactType()),
                toEventArtifactState(artifact.state()),
                toEventCustomProperties(artifact.customProperties()),
                artifact.experimentId(),
                artifact.experimentRunId(),
                artifact.uri(),
                artifact.modelFormatName(),
                artifact.modelFormatVersion(),
                artifact.storageKey(),
                artifact.storagePath(),
                artifact.serviceAccountName(),
                artifact.modelSourceKind(),
                artifact.modelSourceClass(),
                artifact.modelSourceGroup(),
                artifact.modelSourceId(),
                artifact.modelSourceName(),
                artifact.createTimeSinceEpoch(),
                artifact.lastUpdateTimeSinceEpoch()
        );
    }

    public io.cx.platform.events.artifacts.ModelArtifactPayload toModelArtifactPayload(Artifact artifact) {
        return toModelArtifactPayload(toModelArtifact(artifact));
    }

    public Artifact toArtifact(ArtifactPayload payload) {
        Objects.requireNonNull(payload, "artifact payload must not be null");
        Artifact artifact = createArtifactByType(toArtifactType(payload.artifactType()));
        artifact.id(payload.id());
        artifact.name(payload.name());
        artifact.externalId(payload.externalId());
        artifact.description(payload.description());
        artifact.customProperties(toMetadataCustomProperties(payload.customProperties()));
        artifact.experimentId(payload.experimentId());
        artifact.experimentRunId(payload.experimentRunId());
        artifact.state(toArtifactState(payload.state()));
        artifact.createTimeSinceEpoch(payload.createdAt());
        artifact.lastUpdateTimeSinceEpoch(payload.updatedAt());
        fillArtifactSpecificFields(
                artifact,
                payload.uri(),
                payload.digest(),
                payload.sourceType(),
                payload.source(),
                payload.schema(),
                payload.profile(),
                payload.value(),
                payload.timestamp(),
                payload.step(),
                payload.parameterType()
        );
        return artifact;
    }

    public Artifact toArtifact(CreateModelVersionArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "create model version artifact payload must not be null");
        Artifact artifact = createArtifactByType(toArtifactType(payload.artifactType()));
        artifact.name(payload.name());
        artifact.externalId(payload.externalId());
        artifact.description(payload.description());
        artifact.customProperties(toMetadataCustomProperties(payload.customProperties()));
        artifact.experimentId(payload.experimentId());
        artifact.experimentRunId(payload.experimentRunId());
        artifact.state(toArtifactState(payload.state()));
        fillArtifactSpecificFields(
                artifact,
                payload.uri(),
                payload.digest(),
                payload.sourceType(),
                payload.source(),
                payload.schema(),
                payload.profile(),
                payload.value(),
                payload.timestamp(),
                payload.step(),
                payload.parameterType()
        );
        return artifact;
    }

    public Artifact toArtifact(UpdateModelVersionArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "update model version artifact payload must not be null");
        Artifact artifact = createArtifactByType(toArtifactType(payload.artifactType()));
        artifact.id(payload.id());
        artifact.name(payload.name());
        artifact.externalId(payload.externalId());
        artifact.description(payload.description());
        artifact.customProperties(toMetadataCustomProperties(payload.customProperties()));
        artifact.experimentId(payload.experimentId());
        artifact.experimentRunId(payload.experimentRunId());
        artifact.state(toArtifactState(payload.state()));
        fillArtifactSpecificFields(
                artifact,
                payload.uri(),
                payload.digest(),
                payload.sourceType(),
                payload.source(),
                payload.schema(),
                payload.profile(),
                payload.value(),
                payload.timestamp(),
                payload.step(),
                payload.parameterType()
        );
        return artifact;
    }

    public Artifact toArtifact(UpsertModelVersionArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "upsert model version artifact payload must not be null");
        Artifact artifact = createArtifactByType(toArtifactType(payload.artifactType()));
        artifact.id(payload.id());
        artifact.name(payload.name());
        artifact.externalId(payload.externalId());
        artifact.description(payload.description());
        artifact.customProperties(toMetadataCustomProperties(payload.customProperties()));
        artifact.experimentId(payload.experimentId());
        artifact.experimentRunId(payload.experimentRunId());
        artifact.state(toArtifactState(payload.state()));
        fillArtifactSpecificFields(
                artifact,
                payload.uri(),
                payload.digest(),
                payload.sourceType(),
                payload.source(),
                payload.schema(),
                payload.profile(),
                payload.value(),
                payload.timestamp(),
                payload.step(),
                payload.parameterType()
        );
        return artifact;
    }

    public Artifact toArtifact(UpsertExperimentRunArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "upsert experiment run artifact payload must not be null");
        Artifact artifact = createArtifactByType(toArtifactType(payload.artifactType()));
        artifact.id(payload.id());
        artifact.name(payload.name());
        artifact.externalId(payload.externalId());
        artifact.description(payload.description());
        artifact.customProperties(toMetadataCustomProperties(payload.customProperties()));
        artifact.experimentId(payload.experimentId());
        artifact.experimentRunId(payload.experimentRunId());
        artifact.state(toArtifactState(payload.state()));
        fillArtifactSpecificFields(
                artifact,
                payload.uri(),
                payload.digest(),
                payload.sourceType(),
                payload.source(),
                payload.schema(),
                payload.profile(),
                payload.value(),
                payload.timestamp(),
                payload.step(),
                payload.parameterType()
        );
        return artifact;
    }

    public ModelArtifactCreate toModelArtifactCreate(io.cx.platform.events.artifacts.ModelArtifactPayload payload) {
        Objects.requireNonNull(payload, "model artifact payload must not be null");

        ModelArtifactCreate request = new ModelArtifactCreate();
        request.name(payload.name());
        request.externalId(payload.externalId());
        request.description(payload.description());
        request.customProperties(toMetadataCustomProperties(payload.customProperties()));
        request.uri(payload.uri());
        request.modelFormatName(payload.modelFormatName());
        request.modelFormatVersion(payload.modelFormatVersion());
        request.storageKey(payload.storageKey());
        request.storagePath(payload.storagePath());
        request.serviceAccountName(payload.serviceAccountName());
        request.modelSourceKind(payload.modelSourceKind());
        request.modelSourceClass(payload.modelSourceClass());
        request.modelSourceGroup(payload.modelSourceGroup());
        request.modelSourceId(payload.modelSourceId());
        request.modelSourceName(payload.modelSourceName());
        request.state(toArtifactState(payload.state()));
        return request;
    }

    public ModelArtifactCreate toModelArtifactCreate(CreateModelArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "create model artifact payload must not be null");

        ModelArtifactCreate request = new ModelArtifactCreate();
        request.name(payload.name());
        request.externalId(payload.externalId());
        request.description(payload.description());
        request.customProperties(toMetadataCustomProperties(payload.customProperties()));
        request.uri(payload.uri());
        request.modelFormatName(payload.modelFormatName());
        request.modelFormatVersion(payload.modelFormatVersion());
        request.storageKey(payload.storageKey());
        request.storagePath(payload.storagePath());
        request.serviceAccountName(payload.serviceAccountName());
        request.modelSourceKind(payload.modelSourceKind());
        request.modelSourceClass(payload.modelSourceClass());
        request.modelSourceGroup(payload.modelSourceGroup());
        request.modelSourceId(payload.modelSourceId());
        request.modelSourceName(payload.modelSourceName());
        request.state(toArtifactState(payload.state()));
        return request;
    }

    public ModelArtifactUpdate toModelArtifactUpdate(io.cx.platform.events.artifacts.ModelArtifactPayload payload) {
        Objects.requireNonNull(payload, "model artifact payload must not be null");

        ModelArtifactUpdate request = new ModelArtifactUpdate();
        request.setExternalId(payload.externalId());
        request.setDescription(payload.description());
        request.setCustomProperties(toMetadataCustomProperties(payload.customProperties()));
        request.setUri(payload.uri());
        request.setModelFormatName(payload.modelFormatName());
        request.setModelFormatVersion(payload.modelFormatVersion());
        request.setStorageKey(payload.storageKey());
        request.setStoragePath(payload.storagePath());
        request.setServiceAccountName(payload.serviceAccountName());
        request.setModelSourceKind(payload.modelSourceKind());
        request.setModelSourceClass(payload.modelSourceClass());
        request.setModelSourceGroup(payload.modelSourceGroup());
        request.setModelSourceId(payload.modelSourceId());
        request.setModelSourceName(payload.modelSourceName());
        request.setState(toArtifactState(payload.state()));
        return request;
    }

    public ModelArtifactUpdate toModelArtifactUpdate(UpdateModelArtifactCommandPayload payload) {
        Objects.requireNonNull(payload, "update model artifact payload must not be null");

        ModelArtifactUpdate request = new ModelArtifactUpdate();
        request.setExternalId(payload.externalId());
        request.setDescription(payload.description());
        request.setCustomProperties(toMetadataCustomProperties(payload.customProperties()));
        request.setUri(payload.uri());
        request.setModelFormatName(payload.modelFormatName());
        request.setModelFormatVersion(payload.modelFormatVersion());
        request.setStorageKey(payload.storageKey());
        request.setStoragePath(payload.storagePath());
        request.setServiceAccountName(payload.serviceAccountName());
        request.setModelSourceKind(payload.modelSourceKind());
        request.setModelSourceClass(payload.modelSourceClass());
        request.setModelSourceGroup(payload.modelSourceGroup());
        request.setModelSourceId(payload.modelSourceId());
        request.setModelSourceName(payload.modelSourceName());
        request.setState(toArtifactState(payload.state()));
        return request;
    }

    public String toArtifactTypeQueryValue(io.cx.platform.events.artifacts.ArtifactType type) {
        return switch (type) {
            case MODEL_ARTIFACT -> "model-artifact";
            case DOC_ARTIFACT -> "doc-artifact";
            case DATASET_ARTIFACT -> "dataset-artifact";
            case METRIC -> "metric";
            case PARAMETER -> "parameter";
        };
    }

    private RegisteredModelState parseState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }
        try {
            return RegisteredModelState.valueOf(state.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown model state '{}', ignoring", state);
            return null;
        }
    }

    private ModelVersionState parseModelVersionState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }
        try {
            return ModelVersionState.valueOf(state.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown model version state '{}', ignoring", state);
            return null;
        }
    }

    private Optional<String> metadataString(Map<String, MetadataValue> values, String key) {
        return ofNullable(values)
                .map(map -> map.get(key))
                .filter(MetadataStringValue.class::isInstance)
                .map(MetadataStringValue.class::cast)
                .map(MetadataStringValue::string_value)
                .filter(not(String::isBlank));
    }

    private ModelArtifact toModelArtifact(Artifact artifact) {
        Objects.requireNonNull(artifact, "artifact must not be null");
        if (artifact instanceof ModelArtifact modelArtifact) {
            return modelArtifact;
        }

        ModelArtifact modelArtifact = new ModelArtifact();
        modelArtifact.id(artifact.id());
        modelArtifact.name(artifact.name());
        modelArtifact.externalId(artifact.externalId());
        modelArtifact.description(artifact.description());
        modelArtifact.customProperties(artifact.customProperties());
        modelArtifact.experimentId(artifact.experimentId());
        modelArtifact.experimentRunId(artifact.experimentRunId());
        modelArtifact.state(artifact.state());
        modelArtifact.createTimeSinceEpoch(artifact.createTimeSinceEpoch());
        modelArtifact.lastUpdateTimeSinceEpoch(artifact.lastUpdateTimeSinceEpoch());

        if (artifact instanceof DocArtifact docArtifact) {
            modelArtifact.uri(docArtifact.uri());
        }
        if (artifact instanceof DataSet dataSet) {
            modelArtifact.uri(dataSet.uri());
        }
        return modelArtifact;
    }

    private Artifact createArtifactByType(ArtifactType artifactType) {
        return switch (artifactType) {
            case MODEL_ARTIFACT -> new ModelArtifact();
            case DOC_ARTIFACT -> new DocArtifact();
            case DATASET_ARTIFACT -> new DataSet();
            case METRIC -> new Metric();
            case PARAMETER -> new Parameter();
        };
    }

    private void fillArtifactSpecificFields(
            Artifact artifact,
            String uri,
            String digest,
            String sourceType,
            String source,
            String schema,
            String profile,
            Object value,
            Long timestamp,
            Long step,
            String parameterType
    ) {
        if (artifact instanceof ModelArtifact modelArtifact) {
            modelArtifact.uri(uri);
            return;
        }
        if (artifact instanceof DocArtifact docArtifact) {
            docArtifact.uri(uri);
            return;
        }
        if (artifact instanceof DataSet dataSet) {
            dataSet.uri(uri);
            dataSet.digest(digest);
            dataSet.sourceType(sourceType);
            dataSet.source(source);
            dataSet.schema(schema);
            dataSet.profile(profile);
            return;
        }
        if (artifact instanceof Metric metric) {
            metric.value(asDouble(value));
            metric.timestamp(timestamp);
            metric.step(step);
            return;
        }
        if (artifact instanceof Parameter parameter) {
            parameter.value(value != null ? value.toString() : null);
            parameter.parameterType(parameterType);
        }
    }

    private ArtifactType toArtifactType(io.cx.platform.events.artifacts.ArtifactType artifactType) {
        if (artifactType == null) {
            return ArtifactType.MODEL_ARTIFACT;
        }
        return ArtifactType.valueOf(artifactType.name());
    }

    private io.cx.platform.events.artifacts.ArtifactType toEventArtifactType(ArtifactType artifactType) {
        if (artifactType == null) {
            return io.cx.platform.events.artifacts.ArtifactType.MODEL_ARTIFACT;
        }
        return io.cx.platform.events.artifacts.ArtifactType.valueOf(artifactType.name());
    }

    private ArtifactState toArtifactState(io.cx.platform.events.artifacts.ArtifactState state) {
        if (state == null) {
            return null;
        }
        return ArtifactState.valueOf(state.name());
    }

    private io.cx.platform.events.artifacts.ArtifactState toEventArtifactState(ArtifactState state) {
        if (state == null) {
            return null;
        }
        return io.cx.platform.events.artifacts.ArtifactState.valueOf(state.name());
    }

    private Map<String, MetadataValue> toMetadataCustomProperties(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, MetadataValue> result = new HashMap<>();
        values.forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }
            result.put(key, toMetadataValue(value));
        });
        return result;
    }

    private Map<String, Object> toEventCustomProperties(Map<String, MetadataValue> values) {
        if (values == null || values.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        values.forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }
            result.put(key, toMetadataObject(value));
        });
        return result;
    }

    private MetadataValue toMetadataValue(Object value) {
        if (value instanceof Boolean bool) {
            return new MetadataBoolValue(bool);
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return new MetadataIntValue(((Number) value).longValue());
        }
        if (value instanceof Float || value instanceof Double) {
            return new MetadataDoubleValue(((Number) value).doubleValue());
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return new MetadataStructValue(Json.encode(value));
        }
        return new MetadataStringValue(value.toString());
    }

    private Object toMetadataObject(MetadataValue value) {
        if (value instanceof MetadataStringValue stringValue) {
            return stringValue.string_value();
        }
        if (value instanceof MetadataIntValue intValue) {
            return parseLong(intValue.int_value());
        }
        if (value instanceof MetadataDoubleValue doubleValue) {
            return doubleValue.double_value();
        }
        if (value instanceof MetadataBoolValue boolValue) {
            return boolValue.bool_value();
        }
        if (value instanceof MetadataStructValue structValue) {
            return structValue.struct_value();
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
