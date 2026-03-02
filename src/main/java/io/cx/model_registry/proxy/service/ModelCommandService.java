package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.EventProducer;
import io.cx.model_registry.proxy.dto.models.RegisteredModel;
import io.cx.model_registry.proxy.dto.models.RegisteredModelCreate;
import io.cx.model_registry.proxy.dto.models.RegisteredModelList;
import io.cx.model_registry.proxy.dto.models.RegisteredModelUpdate;
import io.cx.model_registry.proxy.mappers.ModelRegistryMapper;
import io.cx.platform.events.models.ModelEvents;
import io.cx.platform.events.models.ModelInfo;
import io.cx.platform.events.models.commands.GetModelQueryPayload;
import io.cx.platform.events.models.commands.ListModelsQueryPayload;
import io.cx.platform.events.models.commands.ModelEventsCommand;
import io.cx.platform.events.models.commands.UpdateModelCommandPayload;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class ModelCommandService {

    @Inject
    ModelRegistryMapper mapper;

    @Inject
    ModelService modelService;

    @Inject
    EventProducer eventProducer;

    public Uni<Void> handle(ModelEventsCommand command) {
        return dispatchNullable(command, cmd -> switch (cmd) {
            case ModelEventsCommand.CreateModelCommand create -> handleCreate(create);
            case ModelEventsCommand.UpdateModelCommand update -> handleUpdate(update);
            case ModelEventsCommand.ListModelsQuery list -> handleList(list);
            case ModelEventsCommand.GetModelQuery get -> handleGet(get);
        });
    }

    private Uni<Void> handleCreate(ModelEventsCommand.CreateModelCommand command) {
        RegisteredModelCreate request = mapper.toCreateModelRequest(command);

        return modelService.createModel(request)
                .chain(registeredModel -> sendResponse(registeredModel, info -> ModelEvents.ModelResponse.of(command, info)));
    }

    private Uni<Void> handleUpdate(ModelEventsCommand.UpdateModelCommand command) {
        UpdateModelCommandPayload payload = command.payload();
        RegisteredModelUpdate request = mapper.toUpdateModelRequest(command);

        return modelService.updateModel(payload.modelId(), request)
                .chain(registeredModel -> sendResponse(registeredModel, info -> ModelEvents.ModelResponse.of(command, info)));
    }

    private Uni<Void> handleGet(ModelEventsCommand.GetModelQuery query) {
        GetModelQueryPayload payload = query.payload();
        return modelService.getModelById(payload.modelId())
                .chain(registeredModel -> sendResponse(registeredModel, info -> ModelEvents.ModelResponse.of(query, info)));
    }

    private Uni<Void> handleList(ModelEventsCommand.ListModelsQuery query) {
        ListModelsQueryPayload payload = query.payload();
        return modelService.listModels(
                        payload.filterQuery(),
                        payload.pageSize(),
                        payload.orderBy(),
                        payload.sortOrder(),
                        payload.nextPageToken()
                )
                .chain(list -> sendResponse(list, values -> ModelEvents.ModelListResponse.of(
                        query, values, list.nextPageToken(), list.pageSize(), list.size())));
    }

    private Uni<Void> sendResponse(RegisteredModelList registeredModelList, Function<List<ModelInfo>, ModelEvents.ModelListResponse> func) {
        return ofNullable(registeredModelList)
                .map(mapper::toModelInfoList)
                .map(func)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private Uni<Void> sendResponse(RegisteredModel registeredModel, Function<ModelInfo, ModelEvents.ModelResponse> func) {
        return ofNullable(registeredModel)
                .map(mapper::toModelInfo)
                .map(func)
                .map(eventProducer::publish)
                .orElseGet(Uni.createFrom()::voidItem);
    }

    private <T> Uni<Void> dispatchNullable(T command, Function<T, Uni<Void>> dispatcher) {
        return ofNullable(command)
                .map(dispatcher)
                .orElseGet(Uni.createFrom()::voidItem);
    }
}
