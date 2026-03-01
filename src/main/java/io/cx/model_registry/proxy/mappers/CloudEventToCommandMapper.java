package io.cx.model_registry.proxy.mappers;

import io.cx.platform.events.BaseEvent;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.serde.CloudEventDeserializer;
import io.cx.platform.events.serde.ExtensionDeserializer;
import io.cx.platform.events.serde.model.commands.CreateModelCommandDeserializer;
import io.cx.platform.events.serde.model.commands.GetModelQueryCommandDeserializer;
import io.cx.platform.events.serde.model.commands.ListModelsQueryCommandDeserializer;
import io.cx.platform.events.serde.model.commands.UpdateModelCommandDeserializer;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.cx.model_registry.proxy.utils.NamingUtils.toDotCase;

@Slf4j
@ApplicationScoped
public class CloudEventToCommandMapper {

    public static final Map<String, Class<?>> EVENT_TO_COMMAND_MAP = new ConcurrentHashMap<>();
    public static final String DELIMITER = ":";
    public Map<Class<?>, Function<ExtensionDeserializer, CloudEventDeserializer<? extends BaseEvent>>> eventDeserializers = new ConcurrentHashMap<>();

    @Inject
    ExtensionDeserializer extensionDeserializer;

    @PostConstruct
    void init(){
        eventDeserializers.put(ModelEventsCommand.CreateModelCommand.class, CreateModelCommandDeserializer::new);
        eventDeserializers.put(ModelEventsCommand.UpdateModelCommand.class, UpdateModelCommandDeserializer::new);
        eventDeserializers.put(ModelEventsCommand.ListModelsQuery.class, ListModelsQueryCommandDeserializer::new);
        eventDeserializers.put(ModelEventsCommand.GetModelQuery.class, GetModelQueryCommandDeserializer::new);
    }

    public ModelEventsCommand toModelEventCommand(CloudEvent<JsonObject> event) {
        return getOptionalSealedClass(event, ModelEventsCommand.class)
                .map(eventDeserializers::get)
                .map(func -> func.apply(extensionDeserializer))
                .map(deserializer -> deserializer.deserialize(event))
                .map(ModelEventsCommand.class::cast)
                .orElse(null);
    }

    private <V> Optional<Class<?>> getOptionalSealedClass(CloudEvent<JsonObject> event, Class<V> sealedClass) {
        return getOptionalSealedClass(event.type(), event.subject(), sealedClass);
    }

    private <V> Optional<Class<?>> getOptionalSealedClass(String type, String subject, Class<V> sealedClass) {

        String key = String.join(DELIMITER, type, subject, sealedClass.getSimpleName());

        if (EVENT_TO_COMMAND_MAP.containsKey(key)) {
            return Optional.of(EVENT_TO_COMMAND_MAP.get(key));
        }

        return findOptionalClass(subject, sealedClass)
                .map(value -> {
                    EVENT_TO_COMMAND_MAP.put(key, value);
                    return value;
                });
    }

    private <V> Optional<Class<?>> findOptionalClass(String subject, Class<V> sealedClass) {
        return Stream.of(sealedClass.getPermittedSubclasses())
                .filter(c -> toDotCase(c).equals(subject))
                .findFirst();
    }
}
