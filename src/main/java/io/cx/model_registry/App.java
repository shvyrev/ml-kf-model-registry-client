package io.cx.model_registry;

import io.cx.model_registry.client.ModelClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class App {
    @Inject
    @RestClient
    ModelClient modelClient;

    public void onStart(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) StartupEvent startupEvent) {
        modelClient.getRegisteredModels("", 10, null, null, null)
                .subscribe().with(
                        models -> log.info("{}", models),
                        error -> log.error(error.getMessage())
                );
    }
}
