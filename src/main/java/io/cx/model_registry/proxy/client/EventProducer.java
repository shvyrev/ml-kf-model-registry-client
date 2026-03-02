package io.cx.model_registry.proxy.client;

import io.cx.platform.events.models.ModelEvents;
import io.cx.platform.events.modelversions.ModelVersionEvents;
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

    @Inject
    @Channel("model-version-events-out")
    MutinyEmitter<byte[]> modelVersionEventsEmitter;

    public Uni<Void> publish(ModelEvents ev){
        return publish(modelEventsEmitter, ev);
    }

    public Uni<Void> publish(ModelVersionEvents ev){
        return publish(modelVersionEventsEmitter, ev);
    }
}
