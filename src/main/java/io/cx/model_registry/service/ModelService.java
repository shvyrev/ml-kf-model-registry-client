package io.cx.model_registry.service;

import io.cx.model_registry.client.ModelClient;
import io.cx.model_registry.client.VersionClient;
import io.cx.model_registry.client.SearchClient;
import io.cx.model_registry.dto.metadata.MetadataValue;
import io.cx.model_registry.dto.models.*;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class ModelService {

    @Inject
    @RestClient
    ModelClient modelClient;

    @Inject
    @RestClient
    VersionClient modelVersionClient;

    @Inject
    @RestClient
    SearchClient search;

    /**
     * Создание новой модели
     */
    public Uni<RegisteredModel> createModel(RegisteredModelCreate request) {
        return modelClient.createRegisteredModel(request)
                .map(response -> response.readEntity(RegisteredModel.class));
    }

    /**
     * Поиск модели по имени
     */
    public Uni<RegisteredModel> findModelByName(String name) {
        log.info("Finding model by name: {}", name);
        return modelClient.findRegisteredModel(name, null);
    }

    /**
     * Поиск модели по externalId
     */
    public Uni<RegisteredModel> findModelByExternalId(String externalId) {
        log.info("Finding model by externalId: {}", externalId);
        return modelClient.findRegisteredModel(null, externalId);
    }

    /**
     * Получение модели по ID
     */
    public Uni<RegisteredModel> getModelById(String modelId) {
        return modelClient.getRegisteredModel(modelId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    /**
     * Получение списка моделей с пагинацией
     */
    public Uni<RegisteredModelList> listModels(
            String filterQuery,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        log.info("Listing models with filter: {}", filterQuery);
        return modelClient.getRegisteredModels(filterQuery, pageSize, orderBy, sortOrder, nextPageToken);
    }

    /**
     * Обновление модели
     */
    public Uni<RegisteredModel> updateModel(String modelId, RegisteredModelUpdate update) {
        return modelClient.updateRegisteredModel(modelId, update);
    }

    /**
     * Архивация модели
     */
    public Uni<RegisteredModel> archiveModel(String modelId) {
        return Uni.createFrom().deferred(() -> Uni.createFrom().item(modelId)
                        .map(v -> new RegisteredModelUpdate()
                                .state(RegisteredModelState.ARCHIVED)))
                .chain(update -> updateModel(modelId, update));
    }

    /**
     * Восстановление модели из архива
     */
    public Uni<RegisteredModel> restoreModel(String modelId) {
        return Uni.createFrom().deferred(() -> Uni.createFrom().item(modelId)
                        .map(v -> new RegisteredModelUpdate()
                                .state(RegisteredModelState.LIVE)))
                .chain(update -> updateModel(modelId, update));
    }

    /**
     * Добавление кастомного свойства
     */
    public Uni<RegisteredModel> addCustomProperty(
            String modelId,
            String key,
            MetadataValue value
    ) {

        return getModelById(modelId)
                .map(model -> {
                    Map<String, MetadataValue> customProperties =
                            model.customProperties() != null ? new HashMap<>(model.customProperties()) : new HashMap<>();
                    customProperties.put(key, value);
                    return new RegisteredModelUpdate()
                            .customProperties(customProperties);
                })
                .map(RegisteredModelUpdate.class::cast)
                .chain(update -> this.updateModel(modelId, update));
    }

    /**
     * Получение версий модели
     */
    public Uni<Response> getModelVersions(
            String modelId,
            String filterQuery,
            Integer pageSize,
            String nextPageToken
    ) {
        log.info("Getting versions for model: {}", modelId);
        return modelClient.getRegisteredModelVersions(
                modelId, null, null, filterQuery,
                pageSize, "ID", "ASC", nextPageToken
        );
    }

}