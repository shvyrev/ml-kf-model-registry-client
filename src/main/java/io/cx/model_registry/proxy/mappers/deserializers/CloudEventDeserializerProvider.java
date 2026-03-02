package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.BaseEvent;
import io.cx.platform.events.serde.CloudEventDeserializer;

import java.util.Collection;

public interface CloudEventDeserializerProvider {
    Collection<CloudEventDeserializer<? extends BaseEvent>> deserializers();
}

