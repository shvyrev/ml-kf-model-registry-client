package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.ExperimentRunClient;
import io.cx.model_registry.proxy.client.ModelArtifactClient;
import io.cx.model_registry.proxy.client.SearchClient;
import io.cx.model_registry.proxy.client.VersionClient;
import io.cx.model_registry.proxy.dto.artifacts.Artifact;
import io.cx.model_registry.proxy.dto.artifacts.ArtifactList;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifact;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactCreate;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactList;
import io.cx.model_registry.proxy.dto.artifacts.ModelArtifactUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ModelArtifactService {

    @Inject
    @RestClient
    ModelArtifactClient modelArtifactClient;

    @Inject
    @RestClient
    SearchClient searchClient;

    @Inject
    @RestClient
    VersionClient versionClient;

    @Inject
    @RestClient
    ExperimentRunClient experimentRunClient;

    public Uni<ModelArtifact> createModelArtifact(ModelArtifactCreate request) {
        return modelArtifactClient.createModelArtifact(request)
                .map(response -> response.readEntity(ModelArtifact.class));
    }

    public Uni<ModelArtifact> updateModelArtifact(String modelArtifactId, ModelArtifactUpdate request) {
        return modelArtifactClient.updateModelArtifact(modelArtifactId, request);
    }

    public Uni<ModelArtifact> getModelArtifactById(String modelArtifactId) {
        return modelArtifactClient.getModelArtifact(modelArtifactId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    public Uni<ModelArtifact> findModelArtifact(String name, String externalId, String parentResourceId) {
        return searchClient.findModelArtifact(name, externalId, parentResourceId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    public Uni<ModelArtifactList> listModelArtifacts(
            String filterQuery,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        return modelArtifactClient.getModelArtifacts(filterQuery, pageSize, orderBy, sortOrder, nextPageToken);
    }

    public Uni<Artifact> upsertModelVersionArtifact(String modelVersionId, Artifact artifact) {
        return versionClient.upsertModelVersionArtifact(modelVersionId, artifact);
    }

    public Uni<ArtifactList> listModelVersionArtifacts(
            String modelVersionId,
            String filterQuery,
            String name,
            String externalId,
            String artifactType,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        return versionClient.getModelVersionArtifacts(
                modelVersionId,
                filterQuery,
                name,
                externalId,
                artifactType,
                pageSize,
                orderBy,
                sortOrder,
                nextPageToken
        );
    }

    public Uni<Artifact> upsertExperimentRunArtifact(String experimentRunId, Artifact artifact) {
        return experimentRunClient.upsertExperimentRunArtifact(experimentRunId, artifact);
    }

    public Uni<ArtifactList> listExperimentRunArtifacts(
            String experimentRunId,
            String filterQuery,
            String name,
            String externalId,
            String artifactType,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        return experimentRunClient.getExperimentRunArtifacts(
                experimentRunId,
                filterQuery,
                name,
                externalId,
                artifactType,
                pageSize,
                orderBy,
                sortOrder,
                nextPageToken
        );
    }
}
