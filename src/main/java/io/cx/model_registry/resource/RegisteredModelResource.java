package io.cx.model_registry.resource;

import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.models.RegisteredModelList;
import io.cx.model_registry.dto.models.RegisteredModelUpdate;
import io.cx.model_registry.service.ModelService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/v1/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegisteredModelResource {

    @Inject
    ModelService modelService;

    @POST
    public Uni<RegisteredModel> createModel(
            @Valid @NotNull(message = "Request body must be provided") RegisteredModelCreate request
    ) {
        return modelService.createModel(request);
    }

    @GET
    @Path("/search")
    public Uni<RegisteredModel> findModel(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId
    ) {
        if (externalId != null && !externalId.isBlank()) {
            return modelService.findModelByExternalId(externalId);
        }
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Either 'name' or 'externalId' must be provided");
        }
        return modelService.findModelByName(name);
    }

    @GET
    @Path("/{modelId}")
    public Uni<RegisteredModel> getModel(@PathParam("modelId") String modelId) {
        return modelService.getModelById(modelId);
    }

    @GET
    public Uni<RegisteredModelList> listModels(
            @QueryParam("filter") String filter,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    ) {
        return modelService.listModels(filter, pageSize, orderBy, sortOrder, nextPageToken);
    }

    @PATCH
    @Path("/{modelId}")
    public Uni<RegisteredModel> updateModel(
            @PathParam("modelId") String modelId,
            @Valid @NotNull(message = "Request body must be provided") RegisteredModelUpdate update
    ) {
        return modelService.updateModel(modelId, update);
    }

    @POST
    @Path("/{modelId}/archive")
    public Uni<RegisteredModel> archiveModel(@PathParam("modelId") String modelId) {
        return modelService.archiveModel(modelId);
    }

    @POST
    @Path("/{modelId}/restore")
    public Uni<RegisteredModel> restoreModel(@PathParam("modelId") String modelId) {
        return modelService.restoreModel(modelId);
    }

    @GET
    @Path("/{modelId}/versions")
    public Uni<Response> getModelVersions(
            @PathParam("modelId") String modelId,
            @QueryParam("filter") String filter,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("nextPageToken") String nextPageToken
    ) {
        return modelService.getModelVersions(modelId, filter, pageSize, nextPageToken);
    }
}
