package io.cx.model_registry.proxy.dto.workflows;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smallrye.mutiny.Uni;

import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public interface IdempotencyKey {
    String idempotencyKey();

    @JsonIgnore
    default Uni<String> resolve(Supplier<String> resolver){
        return Uni.createFrom().item(
                ofNullable(idempotencyKey())
                        .filter(not(s -> s.trim().isEmpty()))
                        .orElseGet(resolver));
    }
}
