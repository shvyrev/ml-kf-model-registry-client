package io.cx.model_registry.resource;

import io.cx.model_registry.dto.metadata.MetadataValue;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
import io.cx.model_registry.dto.versions.ModelVersionList;
import io.cx.model_registry.dto.versions.ModelVersionUpdate;
import io.cx.model_registry.service.ModelService;
import io.cx.model_registry.service.VersionsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/v1/model-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelVersionResource {

    @Inject
    ModelService modelService;

    @Inject
    VersionsService versionsService;

    @POST
    public Uni<ModelVersion> createModelVersion(
            @Valid @NotNull(message = "Request body must be provided") ModelVersionCreate request
    ) {
        return versionsService.createModelVersion(request);
    }

    @GET
    @Path("/search")
    public Uni<ModelVersion> findModelVersion(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    ) {
        return versionsService.findModelVersion(name, externalId, parentResourceId);
    }

    @GET
    @Path("/{modelVersionId}")
    public Uni<ModelVersion> getModelVersion(@PathParam("modelVersionId") String modelVersionId) {
        return versionsService.getModelVersionById(modelVersionId);
    }

    @GET
    public Uni<ModelVersionList> listModelVersions(
            @QueryParam("filter") String filter,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    ) {
        return versionsService.listModelVersions(
                filter, pageSize, orderBy, sortOrder, nextPageToken
        );
    }

    @PATCH
    @Path("/{modelVersionId}")
    public Uni<ModelVersion> updateModelVersion(
            @PathParam("modelVersionId") String modelVersionId,
            @Valid @NotNull(message = "Request body must be provided") ModelVersionUpdate update
    ) {
        return versionsService.updateModelVersion(modelVersionId, update);
    }

    @POST
    @Path("/{modelVersionId}/archive")
    public Uni<ModelVersion> archiveModelVersion(@PathParam("modelVersionId") String modelVersionId) {
        return versionsService.archiveModelVersion(modelVersionId);
    }

    @POST
    @Path("/{modelVersionId}/restore")
    public Uni<ModelVersion> restoreModelVersion(@PathParam("modelVersionId") String modelVersionId) {
        return versionsService.restoreModelVersion(modelVersionId);
    }

    @POST
    @Path("/{modelVersionId}/custom-properties")
    public Uni<ModelVersion> addCustomProperty(
            @PathParam("modelVersionId") String modelVersionId,
            @QueryParam("key") @NotBlank(message = "'key' query parameter must be provided") String key,
            @Valid @NotNull(message = "Request body must be provided") MetadataValue value
    ) {
        return versionsService.addCustomPropertyToModelVersion(modelVersionId, key, value);
    }

}
