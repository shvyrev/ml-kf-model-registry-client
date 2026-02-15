package io.cx.model_registry.service.idempotency;

import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@ApplicationScoped
public class IdempotencyKeyResolver {

    public static final String DOWN_DASH = "_";
    public static final String DELIMITER = ":";
    public static final String NA = "na";
    public static final String NON_ALPHANUMERIC_CHARS = "[^a-zA-Z0-9]";

    public Uni<String> resolve(ModelWithVersionCreateRequest request) {
        return Uni.createFrom().deferred(() -> request.resolve(() -> {
            String modelStable = firstNonBlank(
                    request.model().externalId(),
                    request.model().name());
            String versionStable = firstNonBlank(
                    request.version().externalId(),
                    request.version().name());
            return toKey(request, modelStable, versionStable);
        }));
    }

    public Uni<String> resolve(DeployModelVersionRequest request) {
        return Uni.createFrom().deferred(() -> request.resolve(() -> {
            String envStable = firstNonBlank(
                    request.servingEnvironment().externalId(),
                    request.servingEnvironment().name()
            );
            String inferenceStable = firstNonBlank(
                    request.inferenceService().externalId(),
                    request.inferenceService().name()
            );
            String serveStable = firstNonBlank(
                    request.serve().externalId(),
                    request.serve().name(),
                    request.serve().modelVersionId()
            );
            return toKey(request, envStable, inferenceStable, serveStable);
        }));
    }

    private static <V> String toKey(V value, String... parts) {
        var keyBuilder =
                Stream.of(parts)
                        .filter(not(String::isEmpty))
                        // Replace all non-alphanumeric characters with underscores.
                        .map(s -> s.replaceAll(NON_ALPHANUMERIC_CHARS, DOWN_DASH))
                        .collect(Collectors.joining(DELIMITER));

        return Stream.of(value.getClass().getSimpleName(), keyBuilder)
                .map(String::toLowerCase)
                .collect(Collectors.joining(DELIMITER));
    }

    private String firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(not(s -> s == null || s.trim().isEmpty()))
                .findFirst()
                .orElse(NA);
    }

}
