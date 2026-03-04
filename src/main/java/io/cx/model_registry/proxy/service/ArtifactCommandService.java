package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.EventProducer;
import io.cx.model_registry.proxy.dto.artifacts.Artifact;
import io.cx.model_registry.proxy.dto.artifacts.ArtifactList;
import io.cx.model_registry.proxy.mappers.ModelRegistryMapper;
import io.cx.platform.events.artifacts.ArtifactEvents;
import io.cx.platform.events.artifacts.ArtifactPayload;
import io.cx.platform.events.artifacts.commands.*;
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
public class ArtifactCommandService {

    @Inject
    ModelRegistryMapper mapper;

    @Inject
    ArtifactService artifactService;

    @Inject
    ModelArtifactService modelArtifactService;

    @Inject
    ModelArtifactCommandService modelArtifactCommandService;

    @Inject
    EventProducer eventProducer;

    public Uni<Void> handle(ArtifactEventsCommand command) {
        return dispatchNullable(command, cmd -> switch (cmd) {
            case ArtifactEventsCommand.CreateArtifactCommand create -> handleCreate(create);
            case ArtifactEventsCommand.UpdateArtifactCommand update -> handleUpdate(update);
            case ArtifactEventsCommand.GetArtifactQuery get -> handleGet(get);
            case ArtifactEventsCommand.FindArtifactQuery find -> handleFind(find);
            case ArtifactEventsCommand.ListArtifactsQuery list -> handleList(list);
            case ArtifactEventsCommand.CreateModelArtifactCommand createModel -> modelArtifactCommandService.handleCreate(createModel);
            case ArtifactEventsCommand.UpdateModelArtifactCommand updateModel -> modelArtifactCommandService.handleUpdate(updateModel);
            case ArtifactEventsCommand.GetModelArtifactQuery getModel -> modelArtifactCommandService.handleGet(getModel);
            case ArtifactEventsCommand.FindModelArtifactQuery findModel -> modelArtifactCommandService.handleFind(findModel);
            case ArtifactEventsCommand.ListModelArtifactsQuery listModel -> modelArtifactCommandService.handleList(listModel);
            case ArtifactEventsCommand.CreateModelVersionArtifactCommand createModelVersion -> handleCreateModelVersion(createModelVersion);
            case ArtifactEventsCommand.UpdateModelVersionArtifactCommand updateModelVersion -> handleUpdateModelVersion(updateModelVersion);
            case ArtifactEventsCommand.UpsertModelVersionArtifactCommand upsertModelVersion -> handleUpsertModelVersion(upsertModelVersion);
            case ArtifactEventsCommand.UpsertExperimentRunArtifactCommand upsertExperimentRun -> handleUpsertExperimentRun(upsertExperimentRun);
            case ArtifactEventsCommand.ListModelVersionArtifactsQuery listModelVersion -> handleListModelVersionArtifacts(listModelVersion);
            case ArtifactEventsCommand.ListExperimentRunArtifactsQuery listExperimentRun -> handleListExperimentRunArtifacts(listExperimentRun);
            default -> Uni.createFrom().voidItem();
        });
    }

    private Uni<Void> handleCreate(ArtifactEventsCommand.CreateArtifactCommand command) {
        Artifact request = mapper.toArtifact(command.payload());
        return artifactService.createArtifact(request)
                .chain(artifact -> sendResponse(artifact, value -> ArtifactEvents.ArtifactResponse.of(command, value)));
    }

    private Uni<Void> handleUpdate(ArtifactEventsCommand.UpdateArtifactCommand command) {
        ArtifactPayload payload = command.payload();
        String artifactId = requireId(payload.id(), "UpdateArtifactCommand.id must be provided");
        Artifact request = mapper.toArtifact(payload);
        return artifactService.updateArtifact(artifactId, request)
                .chain(artifact -> sendResponse(artifact, value -> ArtifactEvents.ArtifactResponse.of(command, value)));
    }

    private Uni<Void> handleGet(ArtifactEventsCommand.GetArtifactQuery query) {
        GetArtifactQueryPayload payload = query.payload();
        String artifactId = requireId(payload.id(), "GetArtifactQuery.id must be provided");
        return artifactService.getArtifactById(artifactId)
                .chain(artifact -> sendResponse(artifact, value -> toQueryResponse(query, value)));
    }

    private Uni<Void> handleFind(ArtifactEventsCommand.FindArtifactQuery query) {
        FindArtifactQueryPayload payload = query.payload();
        if (isNotBlank(payload.id())) {
            return artifactService.getArtifactById(payload.id())
                    .chain(artifact -> sendResponse(artifact, value -> toQueryResponse(query, value)));
        }
        if (isNotBlank(payload.filterQuery()) || payload.artifactType() != null) {
            return artifactService.listArtifacts(
                            payload.filterQuery(),
                            payload.artifactType() != null ? mapper.toArtifactTypeQueryValue(payload.artifactType()) : null,
                            payload.pageSize(),
                            payload.orderBy(),
                            payload.sortOrder(),
                            payload.nextPageToken()
                    )
                    .chain(list -> sendFirstFromList(list, query));
        }
        return artifactService.findArtifact(payload.name(), payload.externalId(), null)
                .chain(artifact -> sendResponse(artifact, value -> toQueryResponse(query, value)));
    }

    private Uni<Void> handleList(ArtifactEventsCommand.ListArtifactsQuery query) {
        ListArtifactsQueryPayload payload = query.payload();
        if (isNotBlank(payload.id())) {
            return artifactService.getArtifactById(payload.id())
                    .chain(artifact -> sendResponse(artifact, value -> toQueryResponse(query, value)));
        }
        if (isNotBlank(payload.name()) || isNotBlank(payload.externalId())) {
            return artifactService.findArtifact(payload.name(), payload.externalId(), null)
                    .chain(artifact -> sendResponse(artifact, value -> toQueryResponse(query, value)));
        }
        return artifactService.listArtifacts(
                        payload.filterQuery(),
                        payload.artifactType() != null ? mapper.toArtifactTypeQueryValue(payload.artifactType()) : null,
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendListResponse(list, query));
    }

    private Uni<Void> handleUpsertModelVersion(ArtifactEventsCommand.UpsertModelVersionArtifactCommand command) {
        UpsertModelVersionArtifactCommandPayload payload = command.payload();
        String modelVersionId = firstNonBlank(payload.modelVersionId(), payload.parentResourceId());
        if (modelVersionId == null) {
            throw new IllegalArgumentException("UpsertModelVersionArtifactCommand.modelVersionId must be provided");
        }
        Artifact artifact = mapper.toArtifact(payload);
        return modelArtifactService.upsertModelVersionArtifact(modelVersionId, artifact)
                .chain(value -> sendModelArtifactResponse(value, payloadValue -> ArtifactEvents.ModelArtifactResponse.of(command, payloadValue)));
    }

    private Uni<Void> handleCreateModelVersion(ArtifactEventsCommand.CreateModelVersionArtifactCommand command) {
        CreateModelVersionArtifactCommandPayload payload = command.payload();
        String modelVersionId = requireId(payload.modelVersionId(), "CreateModelVersionArtifactCommand.modelVersionId must be provided");
        Artifact artifact = mapper.toArtifact(payload);
        return modelArtifactService.upsertModelVersionArtifact(modelVersionId, artifact)
                .chain(value -> sendModelArtifactResponse(value, payloadValue -> ArtifactEvents.ModelArtifactResponse.of(command, payloadValue)));
    }

    private Uni<Void> handleUpdateModelVersion(ArtifactEventsCommand.UpdateModelVersionArtifactCommand command) {
        UpdateModelVersionArtifactCommandPayload payload = command.payload();
        requireId(payload.id(), "UpdateModelVersionArtifactCommand.id must be provided");
        String modelVersionId = requireId(payload.modelVersionId(), "UpdateModelVersionArtifactCommand.modelVersionId must be provided");
        Artifact artifact = mapper.toArtifact(payload);
        return modelArtifactService.upsertModelVersionArtifact(modelVersionId, artifact)
                .chain(value -> sendModelArtifactResponse(value, payloadValue -> ArtifactEvents.ModelArtifactResponse.of(command, payloadValue)));
    }

    private Uni<Void> handleUpsertExperimentRun(ArtifactEventsCommand.UpsertExperimentRunArtifactCommand command) {
        UpsertExperimentRunArtifactCommandPayload payload = command.payload();
        String experimentRunId = firstNonBlank(payload.experimentRunId(), payload.parentResourceId());
        if (experimentRunId == null) {
            throw new IllegalArgumentException("UpsertExperimentRunArtifactCommand.experimentRunId must be provided");
        }
        Artifact artifact = mapper.toArtifact(payload);
        return modelArtifactService.upsertExperimentRunArtifact(experimentRunId, artifact)
                .chain(value -> sendModelArtifactResponse(value, payloadValue -> ArtifactEvents.ModelArtifactResponse.of(command, payloadValue)));
    }

    private Uni<Void> handleListModelVersionArtifacts(ArtifactEventsCommand.ListModelVersionArtifactsQuery query) {
        ListModelVersionArtifactsQueryPayload payload = query.payload();
        String modelVersionId = firstNonBlank(payload.modelVersionId(), payload.parentResourceId());
        if (modelVersionId == null) {
            throw new IllegalArgumentException("ListModelVersionArtifactsQuery.modelVersionId must be provided");
        }
        return modelArtifactService.listModelVersionArtifacts(
                        modelVersionId,
                        payload.filterQuery(),
                        payload.name(),
                        payload.externalId(),
                        payload.artifactType() != null ? mapper.toArtifactTypeQueryValue(payload.artifactType()) : null,
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendListResponse(list, query));
    }

    private Uni<Void> handleListExperimentRunArtifacts(ArtifactEventsCommand.ListExperimentRunArtifactsQuery query) {
        ListExperimentRunArtifactsQueryPayload payload = query.payload();
        String experimentRunId = firstNonBlank(payload.experimentRunId(), payload.parentResourceId());
        if (experimentRunId == null) {
            throw new IllegalArgumentException("ListExperimentRunArtifactsQuery.experimentRunId must be provided");
        }
        return modelArtifactService.listExperimentRunArtifacts(
                        experimentRunId,
                        payload.filterQuery(),
                        payload.name(),
                        payload.externalId(),
                        payload.artifactType() != null ? mapper.toArtifactTypeQueryValue(payload.artifactType()) : null,
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendListResponse(list, query));
    }

    private Uni<Void> sendFirstFromList(ArtifactList list, ArtifactEventsCommand.FindArtifactQuery query) {
        List<Artifact> items = ofNullable(list).map(ArtifactList::items).orElseGet(List::of);
        if (items.isEmpty()) {
            return Uni.createFrom().voidItem();
        }
        Artifact first = items.getFirst();
        return sendResponse(first, value -> toQueryResponse(query, value));
    }

    private Uni<Void> sendListResponse(ArtifactList list, ArtifactEventsCommand.ListArtifactsQuery query) {
        return sendListArtifacts(ofNullable(list).map(ArtifactList::items).orElseGet(List::of), value -> toQueryResponse(query, value));
    }

    private Uni<Void> sendListResponse(ArtifactList list, ArtifactEventsCommand.ListModelVersionArtifactsQuery query) {
        return sendListArtifacts(ofNullable(list).map(ArtifactList::items).orElseGet(List::of), value -> toQueryResponse(query, value));
    }

    private Uni<Void> sendListResponse(ArtifactList list, ArtifactEventsCommand.ListExperimentRunArtifactsQuery query) {
        return sendListArtifacts(ofNullable(list).map(ArtifactList::items).orElseGet(List::of), value -> toQueryResponse(query, value));
    }

    private Uni<Void> sendListArtifacts(
            List<Artifact> artifacts,
            Function<ArtifactPayload, ArtifactEvents.ArtifactResponse> responseFactory
    ) {
        if (artifacts == null || artifacts.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return Multi.createFrom().iterable(artifacts)
                .onItem()
                .transformToUniAndConcatenate(artifact -> sendResponse(artifact, responseFactory))
                .collect()
                .last()
                .replaceWithVoid();
    }

    private Uni<Void> sendResponse(
            Artifact artifact,
            Function<ArtifactPayload, ArtifactEvents.ArtifactResponse> responseFactory
    ) {
        return ofNullable(artifact)
                .map(mapper::toArtifactPayload)
                .map(responseFactory)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private Uni<Void> sendModelArtifactResponse(
            Artifact artifact,
            Function<io.cx.platform.events.artifacts.ModelArtifactPayload, ArtifactEvents.ModelArtifactResponse> responseFactory
    ) {
        return ofNullable(artifact)
                .map(mapper::toModelArtifactPayload)
                .map(responseFactory)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private ArtifactEvents.ArtifactResponse toQueryResponse(
            ArtifactEventsCommand.GetArtifactQuery query,
            ArtifactPayload payload
    ) {
        return new ArtifactEvents.ArtifactResponse(UUID.randomUUID(), query.userId(), payload, Instant.now(), query.queryId());
    }

    private ArtifactEvents.ArtifactResponse toQueryResponse(
            ArtifactEventsCommand.FindArtifactQuery query,
            ArtifactPayload payload
    ) {
        return new ArtifactEvents.ArtifactResponse(UUID.randomUUID(), query.userId(), payload, Instant.now(), query.queryId());
    }

    private ArtifactEvents.ArtifactResponse toQueryResponse(
            ArtifactEventsCommand.ListArtifactsQuery query,
            ArtifactPayload payload
    ) {
        return new ArtifactEvents.ArtifactResponse(UUID.randomUUID(), query.userId(), payload, Instant.now(), query.queryId());
    }

    private ArtifactEvents.ArtifactResponse toQueryResponse(
            ArtifactEventsCommand.ListModelVersionArtifactsQuery query,
            ArtifactPayload payload
    ) {
        return new ArtifactEvents.ArtifactResponse(UUID.randomUUID(), query.userId(), payload, Instant.now(), query.queryId());
    }

    private ArtifactEvents.ArtifactResponse toQueryResponse(
            ArtifactEventsCommand.ListExperimentRunArtifactsQuery query,
            ArtifactPayload payload
    ) {
        return new ArtifactEvents.ArtifactResponse(UUID.randomUUID(), query.userId(), payload, Instant.now(), query.queryId());
    }

    private String requireId(String id, String message) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }

    private String firstNonBlank(String first, String second) {
        if (isNotBlank(first)) {
            return first;
        }
        if (isNotBlank(second)) {
            return second;
        }
        return null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private <T> Uni<Void> dispatchNullable(T command, Function<T, Uni<Void>> dispatcher) {
        return ofNullable(command)
                .map(dispatcher)
                .orElseGet(Uni.createFrom()::voidItem);
    }
}
