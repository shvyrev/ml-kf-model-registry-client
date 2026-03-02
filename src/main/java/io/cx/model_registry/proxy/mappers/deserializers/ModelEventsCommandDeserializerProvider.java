package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.models.commands.ModelEventsCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ModelEventsCommandDeserializerProvider extends AbstractSealedCommandDeserializerProvider {

    @Override
    protected Class<?> sealedRoot() {
        return ModelEventsCommand.class;
    }

    @Override
    protected String deserializerPackage() {
        return "io.cx.platform.events.serde.model.commands";
    }
}

