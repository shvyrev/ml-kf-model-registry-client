package io.cx.model_registry.dto.workflows;

import io.smallrye.mutiny.Uni;

import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public interface IdempotencyKey {
    String idempotencyKey();

    default Uni<String> resolve(Supplier<String> resolver){
        return Uni.createFrom().item(
                ofNullable(idempotencyKey())
                        .filter(not(s -> s.trim().isEmpty()))
                        .orElseGet(resolver));
    }
}
