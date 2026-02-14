package io.cx.model_registry.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient(configKey = "dlq-broker")
public interface KnativeDlqBrokerClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Void> publish(
            String data,
            @HeaderParam("ce-id") String eventId,
            @HeaderParam("ce-source") String source,
            @HeaderParam("ce-type") String type,
            @HeaderParam("ce-specversion") String specVersion,
            @HeaderParam("ce-time") String eventTime,
            @HeaderParam("Content-Type") String contentType
    );
}

