package io.cx.model_registry.service;

import io.cx.model_registry.client.VersionClient;
import io.cx.model_registry.client.SearchClient;
import io.cx.model_registry.dto.metadata.MetadataValue;
import io.cx.model_registry.dto.versions.*;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class VersionsService {

    @Inject
    @RestClient
    VersionClient versionClient;

    @Inject
    @RestClient
    SearchClient search;

    /**
     * Создание новой версии модели
     */
    public Uni<ModelVersion> createModelVersion(ModelVersionCreate request) {
        return versionClient.createModelVersion(request);
    }

    /**
     * Поиск версии модели по имени, externalId или parentResourceId
     */
    public Uni<ModelVersion> findModelVersion(String name, String externalId, String parentResourceId) {
        return search.findModelVersion(name, externalId, parentResourceId);
    }

    /**
     * Получение списка всех версий моделей с пагинацией
     */
    public Uni<ModelVersionList> listModelVersions(
            String filterQuery,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        log.info("Listing model versions with filter: {}", filterQuery);
        return versionClient.getModelVersions(filterQuery, pageSize, orderBy, sortOrder, nextPageToken);
    }

    /**
     * Получение версии модели по ID
     */
    public Uni<ModelVersion> getModelVersionById(String versionId) {
        log.info("Getting model version by ID: {}", versionId);
        return versionClient.getModelVersion(versionId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    /**
     * Обновление версии модели
     */
    public Uni<ModelVersion> updateModelVersion(String modelVersionId, ModelVersionUpdate update) {
        log.info("Updating model version: {}", modelVersionId);
        return versionClient.updateModelVersion(modelVersionId, update);
    }

    /**
     * Архивация версии модели
     */
    public Uni<ModelVersion> archiveModelVersion(String versionId) {
        return Uni.createFrom().deferred(() -> Uni.createFrom().item(versionId)
                        .map(id -> new ModelVersionUpdate()
                                .state(ModelVersionState.ARCHIVED)))
                .chain(update -> updateModelVersion(versionId, update));
    }

    /**
     * Восстановление версии модели из архива
     */
    public Uni<ModelVersion> restoreModelVersion(String versionId) {
        return Uni.createFrom().deferred(() -> Uni.createFrom().item(versionId)
                        .map(id -> new ModelVersionUpdate()
                                .state(ModelVersionState.LIVE)))
                .chain(update -> updateModelVersion(versionId, update));
    }

    /**
     * Добавление кастомного свойства к версии модели
     */
    public Uni<ModelVersion> addCustomPropertyToModelVersion(
            String versionId,
            String key,
            MetadataValue value
    ) {

        return getModelVersionById(versionId)
                .map(version -> {
                    Map<String, MetadataValue> customProperties =
                            version.customProperties() != null ? new HashMap<>(version.customProperties()) : new HashMap<>();
                    customProperties.put(key, value);

                    return new ModelVersionUpdate()
                            .customProperties(customProperties);
                })
                .map(ModelVersionUpdate.class::cast)
                .chain(update -> updateModelVersion(versionId, update));
    }
}
