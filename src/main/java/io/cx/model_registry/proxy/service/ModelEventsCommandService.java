package io.cx.model_registry.proxy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cx.model_registry.proxy.client.EventProducer;
import io.cx.model_registry.proxy.dto.BaseResourceList;
import io.cx.model_registry.proxy.dto.metadata.MetadataValue;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Slf4j
@ApplicationScoped
public class ModelEventsCommandService {

    @Inject
    ModelRegistryMapper mapper;

    private static final TypeReference<Map<String, MetadataValue>> METADATA_MAP = new TypeReference<>() {
    };

    @Inject
    ModelService modelService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    EventProducer eventProducer;

    public Uni<Void> handle(ModelEventsCommand command) {
        log.info("$ handle() called with: command = [{}]", command);

        if (command == null) {
            log.warn("Received null ModelEventsCommand");
            return Uni.createFrom().voidItem();
        }
        return switch (command) {
            case ModelEventsCommand.CreateModelCommand create -> handleCreate(create);
            case ModelEventsCommand.UpdateModelCommand update -> handleUpdate(update);
            case ModelEventsCommand.ListModelsQuery list -> handleList(list);
            case ModelEventsCommand.GetModelQuery get -> handleGet(get);
        };
    }

    private Uni<Void> handleCreate(ModelEventsCommand.CreateModelCommand command) {
        log.info("$ handleCreate() called with: command = [{}]", command);

        RegisteredModelCreate request = mapper.toCreateModelRequest(command);

        return modelService.createModel(request)
                .chain(registeredModel -> sendResponse(registeredModel, info -> ModelEvents.ModelResponse.of(command, info)));
    }

    private Uni<Void> handleUpdate(ModelEventsCommand.UpdateModelCommand command) {
        UpdateModelCommandPayload payload = command.payload();
        log.info("$ !!! " + payload);
        RegisteredModelUpdate request = mapper.toUpdateModelRequest(command);

        log.info("$ SUKA!!! :  {}", request);

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

    private Map<String, MetadataValue> toMetadata(Map<String, Object> customProperties) {
        if (customProperties == null || customProperties.isEmpty()) {
            return null;
        }
        return objectMapper.convertValue(customProperties, METADATA_MAP);
    }

    private Uni<ModelInfo> handleConflict(WebApplicationException exception, String action, String target) {
        log.info("$ handleConflict() called with: exception = [{}], action = [{}], target = [{}]", exception, action, target);
        Response response = exception.getResponse();
        int status = response != null ? response.getStatus() : -1;
        String body = null;
        if (response != null && response.hasEntity()) {
            try {
                body = response.readEntity(String.class);
            } catch (Exception readEx) {
                log.warn("Failed to read error response body for action={} target={}", action, target, readEx);
            }
        }
        log.info("$ resp: status={}, message={}, body={}", status, exception.getMessage(), body);

        if (status == 409) {
            log.warn("Conflict on {}: target={}", action, target);
            return Uni.createFrom().nullItem();
        }
        return Uni.createFrom().failure(exception);
    }
}
