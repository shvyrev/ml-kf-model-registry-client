package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.EventProducer;
import io.cx.model_registry.proxy.dto.versions.ModelVersion;
import io.cx.model_registry.proxy.dto.versions.ModelVersionCreate;
import io.cx.model_registry.proxy.dto.versions.ModelVersionList;
import io.cx.model_registry.proxy.dto.versions.ModelVersionUpdate;
import io.cx.model_registry.proxy.mappers.ModelRegistryMapper;
import io.cx.platform.events.modelversions.ModelVersionEvents;
import io.cx.platform.events.modelversions.ModelVersionInfo;
import io.cx.platform.events.modelversions.commands.GetModelVersionQueryPayload;
import io.cx.platform.events.modelversions.commands.ListModelVersionsQueryPayload;
import io.cx.platform.events.modelversions.commands.ModelVersionEventsCommand;
import io.cx.platform.events.modelversions.commands.UpdateModelVersionCommandPayload;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Slf4j
@ApplicationScoped
public class ModelVersionCommandService {

    @Inject
    ModelRegistryMapper mapper;

    @Inject
    VersionsService versionsService;

    @Inject
    EventProducer eventProducer;

    public Uni<Void> handle(ModelVersionEventsCommand command) {
        return dispatchNullable(command, cmd -> switch (cmd) {
            case ModelVersionEventsCommand.CreateModelVersionCommand create -> handleCreate(create);
            case ModelVersionEventsCommand.UpdateModelVersionCommand update -> handleUpdate(update);
            case ModelVersionEventsCommand.ListModelVersionsQuery list -> handleList(list);
            case ModelVersionEventsCommand.GetModelVersionQuery get -> handleGet(get);
        });
    }

    private Uni<Void> handleCreate(ModelVersionEventsCommand.CreateModelVersionCommand command) {
        ModelVersionCreate request = mapper.toCreateModelVersionRequest(command);
        return versionsService.createModelVersion(request)
                .chain(modelVersion -> sendResponse(modelVersion, info -> ModelVersionEvents.ModelVersionResponse.of(command, info)));
    }

    private Uni<Void> handleUpdate(ModelVersionEventsCommand.UpdateModelVersionCommand command) {
        UpdateModelVersionCommandPayload payload = command.payload();
        String modelVersionId = requireModelVersionId(payload);
        ModelVersionUpdate request = mapper.toUpdateModelVersionRequest(command);

        return versionsService.updateModelVersion(modelVersionId, request)
                .chain(modelVersion -> sendResponse(modelVersion, info -> ModelVersionEvents.ModelVersionResponse.of(command, info)));
    }

    private Uni<Void> handleList(ModelVersionEventsCommand.ListModelVersionsQuery query) {
        ListModelVersionsQueryPayload payload = query.payload();
        log.info("$ query :  {}", payload);
        return versionsService.listModelVersions(
                        payload.filterQuery(),
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendListResponse(list, query));
    }

    private Uni<Void> handleGet(ModelVersionEventsCommand.GetModelVersionQuery query) {
        GetModelVersionQueryPayload payload = query.payload();
        return versionsService.getModelVersionById(payload.modelVersionId())
                .chain(modelVersion -> sendResponse(modelVersion, info -> toQueryResponse(query, info)));
    }

    private Uni<Void> sendListResponse(
            ModelVersionList modelVersionList,
            ModelVersionEventsCommand.ListModelVersionsQuery query
    ) {
        return ofNullable(modelVersionList)
                .map(mapper::toModelVersionInfoList)
                .map(values -> ModelVersionEvents.ModelVersionListResponse.of(
                        query,
                        values,
                        modelVersionList.nextPageToken(),
                        modelVersionList.pageSize(),
                        modelVersionList.size()
                ))
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private Uni<Void> sendResponse(
            ModelVersion modelVersion,
            Function<ModelVersionInfo, ModelVersionEvents.ModelVersionResponse> responseFactory
    ) {
        return ofNullable(modelVersion)
                .map(mapper::toModelVersionInfo)
                .map(responseFactory)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private ModelVersionEvents.ModelVersionResponse toQueryResponse(
            ModelVersionEventsCommand.GetModelVersionQuery query,
            ModelVersionInfo info
    ) {
        return new ModelVersionEvents.ModelVersionResponse(
                UUID.randomUUID(),
                query.userId(),
                info,
                Instant.now(),
                // platform-events:0.0.2 does not have queryId in ModelVersionResponse.
                // Put query id into correlation id so response can still be correlated downstream.
                query.queryId()
        );
    }

    private String requireModelVersionId(UpdateModelVersionCommandPayload payload) {
        if (payload == null || payload.modelId() == null || payload.modelId().isBlank()) {
            throw new IllegalArgumentException("UpdateModelVersionCommand.modelId must be provided");
        }
        return payload.modelId();
    }

    private <T> Uni<Void> dispatchNullable(T command, Function<T, Uni<Void>> dispatcher) {
        return ofNullable(command)
                .map(dispatcher)
                .orElseGet(Uni.createFrom()::voidItem);
    }
}
