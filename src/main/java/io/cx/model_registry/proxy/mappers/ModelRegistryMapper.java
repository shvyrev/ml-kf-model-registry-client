package io.cx.model_registry.proxy.mappers;

import io.cx.model_registry.proxy.dto.metadata.MetadataStringValue;
import io.cx.model_registry.proxy.dto.metadata.MetadataValue;
import io.cx.model_registry.proxy.dto.models.RegisteredModel;
import io.cx.model_registry.proxy.dto.models.RegisteredModelCreate;
import io.cx.model_registry.proxy.dto.models.RegisteredModelState;
import io.cx.model_registry.proxy.dto.models.RegisteredModelUpdate;
import io.cx.platform.events.models.ModelInfo;
import io.cx.platform.events.models.commands.CreateModelCommandPayload;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.models.commands.UpdateModelCommandPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ModelRegistryMapper {

    public static final String DISPLAY_NAME = "display_name";

    public ModelInfo toModelInfo(RegisteredModel registeredModel){
        MetadataStringValue modelName = (MetadataStringValue) registeredModel.customProperties()
                .getOrDefault(DISPLAY_NAME, new MetadataStringValue());

        return new ModelInfo(
                registeredModel.id(),
                modelName.string_value(),
                registeredModel.description(),
                registeredModel.state().toString(),
                registeredModel.createTimeSinceEpoch(),
                registeredModel.lastUpdateTimeSinceEpoch());
    }

    public RegisteredModelUpdate toUpdateModelRequest(ModelEventsCommand.UpdateModelCommand command ) {
        UpdateModelCommandPayload payload = command.payload();

        return (RegisteredModelUpdate) new RegisteredModelUpdate()
//                FIXME добавить id
                .owner(payload.owner())
                .state(parseState(payload.state()))
                .description(payload.description())
                .externalId(payload.externalId())
//                FIXME т.к. добавляю display name в метаданные - не нужно его обновлять.
//                 Но нужно проверить, чтобы оно не пропало после обновления.
                .customProperties(null);
    }

    public RegisteredModelCreate toCreateModelRequest(ModelEventsCommand.CreateModelCommand command) {
        CreateModelCommandPayload payload = command.payload();

        var modelName = UUID.randomUUID().toString();

        HashMap<String, @Valid MetadataValue> props = new HashMap<>();
        props.put(DISPLAY_NAME, new MetadataStringValue(command.payload().name()));

        return (RegisteredModelCreate) new RegisteredModelCreate()
                .owner(command.userId())
                .state(RegisteredModelState.LIVE)
                .name(modelName)
                .description(payload.description())
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
