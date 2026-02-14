package io.cx.model_registry.service.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApplicationScoped
public class WorkflowIdempotencyService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WorkflowDlqPublisher dlqPublisher;

    @ConfigProperty(name = "orchestration.idempotency.ttl", defaultValue = "PT24H")
    Duration entryTtl;

    @ConfigProperty(name = "orchestration.idempotency.max-entries", defaultValue = "50000")
    int maxEntries;

    private final Map<String, IdempotencyEntry> entries = new ConcurrentHashMap<>();

    public <T> Uni<T> execute(
            String operation,
            String idempotencyKey,
            Object requestPayload,
            Class<T> resultType,
            Supplier<Uni<T>> action
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        IdempotencyEntry existing = entries.get(idempotencyKey);
        if (existing != null) {
            return resolveExisting(existing, resultType);
        }

        IdempotencyEntry inProgress = IdempotencyEntry.inProgress(idempotencyKey, operation, now);
        IdempotencyEntry raced = entries.putIfAbsent(idempotencyKey, inProgress);
        if (raced != null) {
            return resolveExisting(raced, resultType);
        }

        return action.get()
                .invoke(result -> markSucceeded(idempotencyKey, operation, result))
                .onFailure().call(throwable -> markFailedAndPublishDlq(
                        idempotencyKey, operation, requestPayload, throwable));
    }

    private <T> Uni<T> resolveExisting(IdempotencyEntry existing, Class<T> resultType) {
        if (existing.state() == WorkflowExecutionState.IN_PROGRESS) {
            return Uni.createFrom().failure(new IllegalStateException(
                    "Workflow with idempotency key '" + existing.key() + "' is already in progress"));
        }
        if (existing.state() == WorkflowExecutionState.FAILED) {
            return Uni.createFrom().failure(new IllegalStateException(
                    "Workflow with idempotency key '" + existing.key() + "' has already failed: "
                            + existing.errorMessage()));
        }
        if (existing.responseJson() == null) {
            return Uni.createFrom().failure(new IllegalStateException(
                    "Workflow with idempotency key '" + existing.key() + "' is marked SUCCEEDED but has no response"));
        }
        return Uni.createFrom().item(deserialize(existing.responseJson(), resultType));
    }

    private void markSucceeded(String key, String operation, Object result) {
        String responseJson = serialize(result);
        entries.put(key, IdempotencyEntry.succeeded(key, operation, responseJson, Instant.now()));
    }

    private Uni<Void> markFailedAndPublishDlq(String key, String operation, Object requestPayload, Throwable throwable) {
        String errorMessage = throwable == null ? "unknown error" : throwable.getMessage();
        entries.put(key, IdempotencyEntry.failed(key, operation, errorMessage, Instant.now()));

        WorkflowDlqEvent dlqEvent = new WorkflowDlqEvent(
                key,
                operation,
                throwable == null ? "UnknownException" : throwable.getClass().getSimpleName(),
                errorMessage,
                serialize(requestPayload),
                Instant.now()
        );
        return dlqPublisher.publish(dlqEvent)
                .onFailure().recoverWithNull()
                .replaceWithVoid();
    }

    private void cleanupExpired(Instant now) {
        if (entries.isEmpty()) {
            return;
        }
        Instant threshold = now.minus(entryTtl);
        entries.entrySet().removeIf(entry -> entry.getValue().updatedAt().isBefore(threshold));

        if (entries.size() > maxEntries) {
            entries.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((left, right) -> left.updatedAt().compareTo(right.updatedAt())))
                    .limit(entries.size() - maxEntries)
                    .map(Map.Entry::getKey)
                    .toList()
                    .forEach(entries::remove);
        }
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize cached workflow result for type " + type.getSimpleName(), e);
        }
    }
}
