package io.cx.model_registry.proxy.client;

import io.cx.platform.events.models.ModelEvents;
import io.cx.platform.events.producer.AbstractEventProducer;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;

@Slf4j
@ApplicationScoped
public class EventProducer extends AbstractEventProducer {

    @Inject
    @Channel("model-events-out")
    MutinyEmitter<byte[]> modelEventsEmitter;

    public Uni<Void> publish(ModelEvents.ModelResponse ev){
        return publish(modelEventsEmitter, ev);
    }
}
