package io.cx.model_registry.client;

import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
import io.cx.model_registry.dto.versions.ModelVersionList;
import io.cx.model_registry.dto.versions.ModelVersionUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/model_versions")
@RegisterRestClient(configKey = "model-registry")
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface VersionClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<ModelVersion> createModelVersion(ModelVersionCreate versionCreate);

    @GET
    @Path("/${registered_model_id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersionList> getModelVersions(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @GET
    @Path("/{modelversionId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersion> getModelVersion(@PathParam("modelversionId") String versionId);

    @PATCH
    @Path("/{modelversionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersion> updateModelVersion(@PathParam("modelversionId") String versionId, ModelVersionUpdate versionUpdate);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersionList> list();

//    @ClientExceptionMapper
//    static RuntimeException mapException(Response response, @Context RestClientRequestContext requestContext) {
//        String url = requestContext.getUri().toString();
//        String method = requestContext.getHttpMethod();
//        String body = extractBody(response);
//        String message = extractMessage(response, body);
//
//        return new RestClientException(message)
//                .url(url)
//                .method(method)
//                .status(response.getStatus())
//                .headers(response.getHeaders())
//                .body(body);
//    }
}