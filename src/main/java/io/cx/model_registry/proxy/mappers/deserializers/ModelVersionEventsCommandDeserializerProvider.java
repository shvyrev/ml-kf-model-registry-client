package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.modelversions.commands.ModelVersionEventsCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ModelVersionEventsCommandDeserializerProvider extends AbstractSealedCommandDeserializerProvider {

    @Override
    protected Class<?> sealedRoot() {
        return ModelVersionEventsCommand.class;
    }

    @Override
    protected String deserializerPackage() {
        return "io.cx.platform.events.serde.modelversions.commands";
    }
}

