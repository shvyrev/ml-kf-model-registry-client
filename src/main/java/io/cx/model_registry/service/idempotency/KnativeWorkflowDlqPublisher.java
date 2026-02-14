package io.cx.model_registry.service.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cx.model_registry.client.KnativeDlqBrokerClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class KnativeWorkflowDlqPublisher implements WorkflowDlqPublisher {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @RestClient
    KnativeDlqBrokerClient brokerClient;

    @ConfigProperty(name = "orchestration.dlq.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "orchestration.dlq.event-source", defaultValue = "io.cx.model_registry.orchestrator")
    String eventSource;

    @ConfigProperty(name = "orchestration.dlq.event-type", defaultValue = "io.cx.model_registry.workflow.failed")
    String eventType;

    @Override
    public Uni<Void> publish(WorkflowDlqEvent event) {
        String payload = serialize(event);
        if (!enabled) {
            log.error("DLQ disabled. Event: {}", payload);
            return Uni.createFrom().voidItem();
        }

        return brokerClient.publish(
                        payload,
                        UUID.randomUUID().toString(),
                        eventSource,
                        eventType,
                        "1.0",
                        Instant.now().toString(),
                        "application/json"
                )
                .invoke(() -> log.error("Workflow DLQ event published to Knative Broker: {}", payload))
                .onFailure().invoke(throwable ->
                        log.error("Failed to publish DLQ event to Knative Broker. Fallback log: {}", payload, throwable));
    }

    private String serialize(WorkflowDlqEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return "{\"key\":\"" + safe(event.key()) + "\",\"operation\":\"" + safe(event.operation())
                    + "\",\"errorType\":\"" + safe(event.errorType()) + "\",\"errorMessage\":\""
                    + safe(event.errorMessage()) + "\"}";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }
}

