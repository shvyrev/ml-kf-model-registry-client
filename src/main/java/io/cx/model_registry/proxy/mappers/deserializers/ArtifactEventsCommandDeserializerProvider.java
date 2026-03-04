package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.artifacts.commands.ArtifactEventsCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArtifactEventsCommandDeserializerProvider extends AbstractSealedCommandDeserializerProvider {

    @Override
    protected Class<?> sealedRoot() {
        return ArtifactEventsCommand.class;
    }

    @Override
    protected String deserializerPackage() {
        return "io.cx.platform.events.serde.artifacts.commands";
    }
}
