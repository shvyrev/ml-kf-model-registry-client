package io.cx.model_registry.client;

import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.models.RegisteredModelList;
import io.cx.model_registry.dto.models.RegisteredModelUpdate;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ModelClient {


    // Поиск RegisteredModel по имени или externalId
    @GET
    @Path("/registered_model")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RegisteredModel> findRegisteredModel(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId
    );

    // Получение списка всех RegisteredModel
    @GET
    @Path("/registered_models")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RegisteredModelList> getRegisteredModels(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    // Создание нового RegisteredModel
    @POST
    @Path("/registered_models")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<Response> createRegisteredModel(RegisteredModelCreate registeredModel);

    // Получение RegisteredModel по ID
    @GET
    @Path("/registered_models/{registeredmodelId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RegisteredModel> getRegisteredModel(@PathParam("registeredmodelId") String registeredModelId);

    // Обновление RegisteredModel
    @PATCH
    @Path("/registered_models/{registeredmodelId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RegisteredModel> updateRegisteredModel(
            @PathParam("registeredmodelId") String registeredModelId,
            RegisteredModelUpdate registeredModelUpdate
    );

    // Получение версий модели
    @GET
    @Path("/registered_models/{registeredmodelId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getRegisteredModelVersions(
            @PathParam("registeredmodelId") String registeredModelId,
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @POST
    @Path("/registered_models/{registeredmodelId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersion> createRegisteredModelVersion(
            @PathParam("registeredmodelId") String modelId,
            ModelVersion modelVersion
    );
//
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
