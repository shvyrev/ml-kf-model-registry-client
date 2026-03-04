package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.EventProducer;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifact;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactCreate;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactList;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactUpdate;
import io.cx.model_registry.proxy.mappers.ModelRegistryMapper;
import io.cx.platform.events.artifacts.ArtifactEvents;
import io.cx.platform.events.artifacts.commands.ArtifactEventsCommand;
import io.cx.platform.events.artifacts.commands.FindModelArtifactQueryPayload;
import io.cx.platform.events.artifacts.commands.GetModelArtifactQueryPayload;
import io.cx.platform.events.artifacts.commands.ListModelArtifactsQueryPayload;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class ModelArtifactCommandService {

    @Inject
    ModelRegistryMapper mapper;

    @Inject
    ModelArtifactService modelArtifactService;

    @Inject
    EventProducer eventProducer;

    Uni<Void> handleCreate(ArtifactEventsCommand.CreateModelArtifactCommand command) {
        ModelArtifactCreate request = mapper.toModelArtifactCreate(command.payload());
        return modelArtifactService.createModelArtifact(request)
                .chain(modelArtifact -> sendResponse(modelArtifact, value -> ArtifactEvents.ModelArtifactResponse.of(command, value)));
    }

    Uni<Void> handleUpdate(ArtifactEventsCommand.UpdateModelArtifactCommand command) {
        String modelArtifactId = requireId(command.payload().id(), "UpdateModelArtifactCommand.id must be provided");
        ModelArtifactUpdate request = mapper.toModelArtifactUpdate(command.payload());
        return modelArtifactService.updateModelArtifact(modelArtifactId, request)
                .chain(modelArtifact -> sendResponse(modelArtifact, value -> ArtifactEvents.ModelArtifactResponse.of(command, value)));
    }

    Uni<Void> handleGet(ArtifactEventsCommand.GetModelArtifactQuery query) {
        GetModelArtifactQueryPayload payload = query.payload();
        String modelArtifactId = requireId(payload.id(), "GetModelArtifactQuery.id must be provided");
        return modelArtifactService.getModelArtifactById(modelArtifactId)
                .chain(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)));
    }

    Uni<Void> handleFind(ArtifactEventsCommand.FindModelArtifactQuery query) {
        FindModelArtifactQueryPayload payload = query.payload();
        if (isNotBlank(payload.id())) {
            return modelArtifactService.getModelArtifactById(payload.id())
                    .chain(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)));
        }
        return modelArtifactService.findModelArtifact(payload.name(), payload.externalId(), null)
                .chain(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)));
    }

    Uni<Void> handleList(ArtifactEventsCommand.ListModelArtifactsQuery query) {
        ListModelArtifactsQueryPayload payload = query.payload();
        if (isNotBlank(payload.id())) {
            return modelArtifactService.getModelArtifactById(payload.id())
                    .chain(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)));
        }
        if (isNotBlank(payload.name()) || isNotBlank(payload.externalId())) {
            return modelArtifactService.findModelArtifact(payload.name(), payload.externalId(), null)
                    .chain(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)));
        }

        return modelArtifactService.listModelArtifacts(
                        payload.filterQuery(),
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendListResponse(list, query));
    }

    private Uni<Void> sendListResponse(ModelArtifactList list, ArtifactEventsCommand.ListModelArtifactsQuery query) {
        List<ModelArtifact> values = ofNullable(list)
                .map(ModelArtifactList::items)
                .orElseGet(List::of);

        if (values.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return Multi.createFrom().iterable(values)
                .onItem()
                .transformToUniAndConcatenate(modelArtifact -> sendResponse(modelArtifact, value -> toQueryResponse(query, value)))
                .collect()
                .last()
                .replaceWithVoid();
    }

    private Uni<Void> sendResponse(
            ModelArtifact modelArtifact,
            Function<io.cx.platform.events.artifacts.ModelArtifactPayload, ArtifactEvents.ModelArtifactResponse> responseFactory
    ) {
        return ofNullable(modelArtifact)
                .map(mapper::toModelArtifactPayload)
                .map(responseFactory)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private ArtifactEvents.ModelArtifactResponse toQueryResponse(
            ArtifactEventsCommand.GetModelArtifactQuery query,
            io.cx.platform.events.artifacts.ModelArtifactPayload payload
    ) {
        return new ArtifactEvents.ModelArtifactResponse(
                UUID.randomUUID(),
                query.userId(),
                payload,
                Instant.now(),
                query.queryId()
        );
    }

    private ArtifactEvents.ModelArtifactResponse toQueryResponse(
            ArtifactEventsCommand.FindModelArtifactQuery query,
            io.cx.platform.events.artifacts.ModelArtifactPayload payload
    ) {
        return new ArtifactEvents.ModelArtifactResponse(
                UUID.randomUUID(),
                query.userId(),
                payload,
                Instant.now(),
                query.queryId()
        );
    }

    private ArtifactEvents.ModelArtifactResponse toQueryResponse(
            ArtifactEventsCommand.ListModelArtifactsQuery query,
            io.cx.platform.events.artifacts.ModelArtifactPayload payload
    ) {
        return new ArtifactEvents.ModelArtifactResponse(
                UUID.randomUUID(),
                query.userId(),
                payload,
                Instant.now(),
                query.queryId()
        );
    }

    private String requireId(String id, String message) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
