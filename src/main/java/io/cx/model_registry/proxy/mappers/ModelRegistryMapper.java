package io.cx.model_registry.proxy.mappers;

import io.cx.model_registry.proxy.dto.BaseResourceList;
import io.cx.model_registry.proxy.dto.metadata.MetadataStringValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataValue;
import io.cx.model_registry.proxy.dto.models.*;
import io.cx.platform.events.models.ModelInfo;
import io.cx.platform.events.models.commands.CreateModelCommandPayload;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.models.commands.UpdateModelCommandPayload;
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

}
