package io.cx.model_registry.proxy.service.events;

import io.cx.model_registry.proxy.dto.workflows.ModelWithVersionCreateRequest;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
public class ModelRegistryEventPublisher {

    public static final String MODEL_WITH_VERSION_CHANNEL = "model-with-version-events";
    public static final String MODEL_WITH_VERSION_TYPE = "io.cx.model_registry.model-with-version.requested";
    public static final URI EVENT_SOURCE = URI.create("io.cx.model_registry.kf-model-registry-client");

    @Inject
    @Channel(MODEL_WITH_VERSION_CHANNEL)
    MutinyEmitter<ModelWithVersionCreateRequest> modelWithVersionEmitter;

    public Uni<Void> publishModelWithVersionRequested(ModelWithVersionCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String key = resolveKey(request);
        OutgoingCloudEventMetadata<String> cloudEvent = OutgoingCloudEventMetadata.<String>builder()
                .withId(UUID.randomUUID().toString())
                .withType(MODEL_WITH_VERSION_TYPE)
                .withSource(EVENT_SOURCE)
                .withTimestamp(ZonedDateTime.now())
                .withSubject("model-with-version")
                .withDataContentType(APPLICATION_JSON)
                .build();

        OutgoingKafkaRecordMetadata<String> kafkaMetadata = OutgoingKafkaRecordMetadata.<String>builder()
                .withKey(key)
                .build();

        Message<ModelWithVersionCreateRequest> message = Message.of(request)
                .addMetadata(cloudEvent)
                .addMetadata(kafkaMetadata);

        return modelWithVersionEmitter.sendMessage(message).replaceWithVoid();
    }

    private String resolveKey(ModelWithVersionCreateRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            return request.idempotencyKey();
        }
        if (request.model() != null && request.model().externalId() != null && !request.model().externalId().isBlank()) {
            return request.model().externalId();
        }
        if (request.model() != null && request.model().name() != null && !request.model().name().isBlank()) {
            return request.model().name();
        }
        return UUID.randomUUID().toString();
    }
}
