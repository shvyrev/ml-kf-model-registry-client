package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.BaseEvent;
import io.cx.platform.events.serde.CloudEventDeserializer;
import io.cx.platform.events.serde.ExtensionDeserializer;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractSealedCommandDeserializerProvider implements CloudEventDeserializerProvider {

    private static final String DESERIALIZER_SUFFIX = "Deserializer";

    @Inject
    ExtensionDeserializer extensionDeserializer;

    protected abstract Class<?> sealedRoot();

    protected abstract String deserializerPackage();

    @Override
    public Collection<CloudEventDeserializer<? extends BaseEvent>> deserializers() {
        return Arrays.stream(sealedRoot().getPermittedSubclasses())
                .map(Class::getSimpleName)
                .map(this::resolveDeserializer)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<CloudEventDeserializer<? extends BaseEvent>> resolveDeserializer(String simpleName) {
        List<String> candidates = List.of(
                deserializerPackage() + "." + simpleName + DESERIALIZER_SUFFIX,
                deserializerPackage() + "." + simpleName + "Command" + DESERIALIZER_SUFFIX
        );

        for (String className : candidates) {
            Optional<CloudEventDeserializer<? extends BaseEvent>> maybe = newDeserializerInstance(className);
            if (maybe.isPresent()) {
                return maybe;
            }
        }
        log.warn("No matching deserializer class found for command {} in package {}", simpleName, deserializerPackage());
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<CloudEventDeserializer<? extends BaseEvent>> newDeserializerInstance(String className) {
        try {
            Class<?> deserializerClass = Class.forName(className);
            Constructor<?> constructor = deserializerClass.getConstructor(ExtensionDeserializer.class);
            Object value = constructor.newInstance(extensionDeserializer);
            if (value instanceof CloudEventDeserializer<?> deserializer) {
                return Optional.of((CloudEventDeserializer<? extends BaseEvent>) deserializer);
            }
            log.warn("Class {} exists but is not a CloudEventDeserializer", className);
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unable to initialize cloud-event deserializer {}", className, e);
            return Optional.empty();
        }
    }
}
