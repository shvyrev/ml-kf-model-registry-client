package io.cx.model_registry.proxy.mappers.deserializers;

import io.cx.platform.events.BaseEvent;
import io.cx.platform.events.serde.CloudEventDeserializer;
import io.quarkus.arc.All;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.cx.model_registry.proxy.Const.Common.DELIMITER;

@Slf4j
@ApplicationScoped
public class CloudEventDeserializerRegistry {

    @Inject
    @All
    List<CloudEventDeserializerProvider> providers;

    private final Map<String, CloudEventDeserializer<? extends BaseEvent>> cache = new ConcurrentHashMap<>();
    private List<CloudEventDeserializer<? extends BaseEvent>> deserializers = List.of();

    @PostConstruct
    void init() {
        deserializers = providers.stream()
                .flatMap(provider -> provider.deserializers().stream())
                .toList();
        log.info("CloudEventDeserializerRegistry initialized with {} deserializers from {} providers",
                deserializers.size(), providers.size());
    }

    public <T extends BaseEvent> T deserialize(CloudEvent<JsonObject> event, Class<T> rootType) {
        if (event == null || rootType == null) {
            return null;
        }

        String key = cacheKey(rootType, event.type(), event.subject());
        CloudEventDeserializer<? extends BaseEvent> cached = cache.get(key);
        if (cached != null) {
            return rootType.cast(cached.deserialize(event));
        }

        CloudEventDeserializer<? extends BaseEvent> resolved = deserializers.stream()
                .filter(deserializer -> rootType.isAssignableFrom(deserializer.eventClass()))
                .filter(deserializer -> deserializer.supports(event))
                .findFirst()
                .orElse(null);

        if (resolved == null) {
            log.debug("No deserializer found for rootType={}, type={}, subject={}",
                    rootType.getSimpleName(), event.type(), event.subject());
            return null;
        }

        cache.putIfAbsent(key, resolved);
        return rootType.cast(resolved.deserialize(event));
    }

    private String cacheKey(Class<?> rootType, String type, String subject) {
        return String.join(DELIMITER, rootType.getName(), type, subject);
    }
}

