package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ArtifactList;
import io.cx.model_registry.dto.artifacts.MetricList;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.experimentruns.ExperimentRunCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRunList;
import io.cx.model_registry.dto.experimentruns.ExperimentRunUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/experiment_runs")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ExperimentRunClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentRunList> getExperimentRuns(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<ExperimentRun> createExperimentRun(ExperimentRunCreate experimentRun);

    @GET
    @Path("/metric_history")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<MetricList> getExperimentRunsMetricHistory(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("name") String name,
            @QueryParam("stepIds") String stepIds,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @GET
    @Path("/{experimentrunId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentRun> getExperimentRun(@PathParam("experimentrunId") String experimentRunId);

    @PATCH
    @Path("/{experimentrunId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentRun> updateExperimentRun(
            @PathParam("experimentrunId") String experimentRunId,
            ExperimentRunUpdate experimentRunUpdate
    );

    @GET
    @Path("/{experimentrunId}/artifacts")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ArtifactList> getExperimentRunArtifacts(
            @PathParam("experimentrunId") String experimentRunId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("artifactType") String artifactType,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @POST
    @Path("/{experimentrunId}/artifacts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<Artifact> upsertExperimentRunArtifact(
            @PathParam("experimentrunId") String experimentRunId,
            Artifact artifact
    );

    @GET
    @Path("/{experimentrunId}/metric_history")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<MetricList> getExperimentRunMetricHistory(
            @PathParam("experimentrunId") String experimentRunId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("name") String name,
            @QueryParam("stepIds") String stepIds,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );
}
