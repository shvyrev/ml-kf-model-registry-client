package io.cx.model_registry.proxy.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cx.model_registry.proxy.mappers.CloudEventToCommandMapper;
import io.cx.model_registry.proxy.service.ModelCommandService;
import io.cx.model_registry.proxy.service.ModelVersionCommandService;
import io.cx.model_registry.proxy.service.ModelRegistryOrchestrationService;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventMapping;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static java.util.Optional.ofNullable;

@Slf4j
public class ModelRegistryEventFunctions {

    @Inject
    ModelRegistryOrchestrationService orchestrationService;

    @Inject
    Validator validator;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CloudEventToCommandMapper mapper;

    @Inject
    ModelCommandService commandService;

    @Inject
    ModelVersionCommandService modelVersionCommandService;

    @Funq("handle-cloud-event-model-registry-command")
    @CloudEventMapping(trigger = "model.events.command")
    public Uni<Void> handleModelRegistryCommand(CloudEvent<JsonObject> event) {
        return ofNullable(event)
                .map(mapper::toModelEventCommand)
                .map(commandService::handle)
                .map(v -> v.onFailure().recoverWithNull())
                .orElseGet(Uni.createFrom()::voidItem);
    }

    @Funq("handle-cloud-event-model-version-registry-command")
    @CloudEventMapping(trigger = "model.version.events.command")
    public Uni<Void> handleModelVersionRegistryCommand(CloudEvent<JsonObject> event) {
        log.info("$ handleModelVersionRegistryCommand() called with: event = [{}]", event);
        return ofNullable(event)
                .map(mapper::toModelVersionEventCommand)
                .map(modelVersionCommandService::handle)
                .map(v -> v.onFailure().recoverWithNull())
                .orElseGet(Uni.createFrom()::voidItem);
    }

//    @Funq("model-with-version-workflow")
//    @CloudEventMapping(
//            trigger = "io.cx.model_registry.model-with-version.requested",
//            responseType = "io.cx.model_registry.model-with-version.completed"
//    )
//    public Uni<ModelWithVersionCreateResult> handleModelWithVersionRequested(
//            CloudEvent<JsonObject> event
//    ) {
//        ModelWithVersionCreateRequest request = requireValidData(
//                event,
//                ModelWithVersionCreateRequest.class,
//                "Event data must be provided"
//        );
//        return orchestrationService.createModelWithVersionIdempotent(request);
//    }
//
//    @Funq("deploy-model-version-workflow")
//    @CloudEventMapping(
//            trigger = "io.cx.model_registry.deploy-model-version.requested",
//            responseType = "io.cx.model_registry.deploy-model-version.completed"
//    )
//    public Uni<DeployModelVersionResult> handleDeployModelVersionRequested(
//            CloudEvent<JsonObject> event
//    ) {
//        DeployModelVersionRequest request = requireValidData(
//                event,
//                DeployModelVersionRequest.class,
//                "Event data must be provided"
//        );
//        return orchestrationService.deployModelVersionIdempotent(request);
//    }

    private <T> T requireValidData(CloudEvent<JsonObject> event, Class<T> type, String nullMessage) {
        if (event == null || event.data() == null) {
            throw new ConstraintViolationException(nullMessage, Set.of());
        }
        T data = mapData(event.data(), type);
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return data;
    }

    private <T> T mapData(Object data, Class<T> type) {
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        if (data instanceof JsonObject jsonObject) {
            return objectMapper.convertValue(jsonObject.getMap(), type);
        }
        return objectMapper.convertValue(data, type);
    }
}
