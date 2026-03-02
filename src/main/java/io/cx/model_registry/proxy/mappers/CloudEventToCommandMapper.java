package io.cx.model_registry.proxy.mappers;

import io.cx.model_registry.proxy.mappers.deserializers.CloudEventDeserializerRegistry;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.modelversions.commands.ModelVersionEventsCommand;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CloudEventToCommandMapper {

    @Inject
    CloudEventDeserializerRegistry registry;

    public ModelEventsCommand toModelEventCommand(CloudEvent<JsonObject> event) {
        log.info("$ toModelEventCommand() called with: event = [{}]", event);
        return registry.deserialize(event, ModelEventsCommand.class);
    }

    public ModelVersionEventsCommand toModelVersionEventCommand(CloudEvent<JsonObject> event) {
        log.info("$ toModelVersionEventCommand() called with: event = [{}]", event);
        return registry.deserialize(event, ModelVersionEventsCommand.class);
    }
}

