package io.cx.model_registry.service.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cx.model_registry.exceptions.IdempotencyEntryAlreadyExistsException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@ApplicationScoped
public class WorkflowIdempotencyService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    RemoteCacheManager remoteCacheManager;

    @ConfigProperty(name = "orchestration.idempotency.enabled", defaultValue = "true")
    boolean idempotencyEnabled;

    @ConfigProperty(name = "orchestration.idempotency.cache-name", defaultValue = "workflow-idempotency")
    String cacheName;

    @ConfigProperty(name = "orchestration.idempotency.ttl", defaultValue = "PT24H")
    Duration entryTtl;

    private volatile RemoteCache<String, IdempotencyEntry> cache;

    public <T> Uni<T> execute(
            String operation,
            String idempotencyKey,
            Class<T> resultType,
            Supplier<Uni<T>> action
    ) {
        if (!idempotencyEnabled || idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        return getEntryAsync(idempotencyKey)
                .onItem().ifNotNull().transformToUni(idempotencyEntry -> resolveExisting(idempotencyEntry, resultType))
                .onItem().ifNull().switchTo(() -> {
                    IdempotencyEntry inProgress = IdempotencyEntry.inProgress(idempotencyKey, operation, Instant.now());
                    return putIfAbsentAsync(idempotencyKey, inProgress)
                            .chain(raced -> {
                                if (raced != null && raced.state() != WorkflowExecutionState.IN_PROGRESS) {
                                    return resolveExisting(raced, resultType);
                                }
                                if (raced != null) {
                                    return Uni.createFrom().failure(new IdempotencyEntryAlreadyExistsException(idempotencyKey));
                                }

                                return action.get()
                                        .chain(result -> markSucceededAsync(idempotencyKey, operation, result).replaceWith(result))
                                        .onFailure().call(throwable -> markFailedAsync(idempotencyKey, operation, throwable));
                            });
                });
    }


    private <T> Uni<T> resolveExisting(IdempotencyEntry existing, Class<T> resultType) {
        if (existing.state() == WorkflowExecutionState.IN_PROGRESS) {
            return Uni.createFrom().failure(new IdempotencyEntryAlreadyExistsException(existing.key()));
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

    private Uni<Void> markSucceededAsync(String key, String operation, Object result) {
        String responseJson = serialize(result);
        IdempotencyEntry entry = IdempotencyEntry.succeeded(key, operation, responseJson, Instant.now());
        return putEntryAsync(key, entry);
    }

    private Uni<Void> markFailedAsync(String key, String operation, Throwable throwable) {
        String errorMessage = throwable == null ? "unknown error" : throwable.getMessage();
        IdempotencyEntry entry = IdempotencyEntry.failed(key, operation, errorMessage, Instant.now());
        return putEntryAsync(key, entry);
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

    private Uni<IdempotencyEntry> getEntryAsync(String key) {
        return Uni.createFrom().completionStage(() -> getCache().getAsync(key));
    }

    private Uni<IdempotencyEntry> putIfAbsentAsync(String key, IdempotencyEntry entry) {
        return Uni.createFrom().completionStage(() -> getCache().putIfAbsentAsync(
                key,
                entry,
                entryTtl.toMillis(),
                TimeUnit.MILLISECONDS
        ));
    }

    private Uni<Void> putEntryAsync(String key, IdempotencyEntry entry) {
        return Uni.createFrom().completionStage(() -> getCache().putAsync(
                key,
                entry,
                entryTtl.toMillis(),
                TimeUnit.MILLISECONDS
        )).replaceWithVoid();
    }

    private RemoteCache<String, IdempotencyEntry> getCache() {
        RemoteCache<String, IdempotencyEntry> local = cache;
        if (local == null) {
            synchronized (this) {
                local = cache;
                if (local == null) {
                    local = remoteCacheManager.getCache(cacheName);
                    if (local == null) {
                        throw new IllegalStateException(
                                "Infinispan cache '" + cacheName + "' is not available. Create it or configure auto-create.");
                    }
                    cache = local;
                }
            }
        }
        return local;
    }
}
