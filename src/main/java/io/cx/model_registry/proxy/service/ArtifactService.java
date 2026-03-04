package io.cx.model_registry.proxy.service;

import io.cx.model_registry.proxy.client.ArtifactClient;
import io.cx.model_registry.proxy.client.SearchClient;
import io.cx.model_registry.proxy.dto.artifacts.Artifact;
import io.cx.model_registry.proxy.dto.artifacts.ArtifactList;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ArtifactService {

    @Inject
    @RestClient
    ArtifactClient artifactClient;

    @Inject
    @RestClient
    SearchClient searchClient;

    public Uni<Artifact> createArtifact(Artifact artifact) {
        return artifactClient.createArtifact(artifact)
                .map(response -> response.readEntity(Artifact.class));
    }

    public Uni<Artifact> updateArtifact(String artifactId, Artifact artifact) {
        return artifactClient.updateArtifact(artifactId, artifact);
    }

    public Uni<Artifact> getArtifactById(String artifactId) {
        return artifactClient.getArtifact(artifactId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    public Uni<Artifact> findArtifact(String name, String externalId, String parentResourceId) {
        return searchClient.findArtifact(name, externalId, parentResourceId)
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    public Uni<ArtifactList> listArtifacts(
            String filterQuery,
            String artifactType,
            Integer pageSize,
            String orderBy,
            String sortOrder,
            String nextPageToken
    ) {
        return artifactClient.getArtifacts(filterQuery, artifactType, pageSize, orderBy, sortOrder, nextPageToken);
    }
}
