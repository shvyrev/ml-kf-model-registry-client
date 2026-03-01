package io.cx.model_registry.proxy.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cx.model_registry.proxy.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.proxy.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.proxy.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.proxy.dto.workflows.ModelWithVersionCreateResult;
import io.cx.model_registry.proxy.mappers.CloudEventToCommandMapper;
import io.cx.model_registry.proxy.service.ModelEventsCommandService;
import io.cx.model_registry.proxy.service.ModelRegistryOrchestrationService;
import io.cx.platform.events.models.commands.ModelEventsCommand;
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
    ModelEventsCommandService commandService;

    @Funq("handle-cloud-event-model-registry-command")
    @CloudEventMapping(trigger = "model.events.command")
    public Uni<Void> handleModelRegistryCommand(CloudEvent<JsonObject> event) {
        if (event == null) {
            log.warn("Received null CloudEvent");
            return Uni.createFrom().voidItem();
        }
        ModelEventsCommand command = mapper.toModelEventCommand(event);
        return commandService.handle(command)
//                TODO добавить нормальный обработчик ошибок
                .onFailure().recoverWithUni(Uni.createFrom()::voidItem);
    }


    @Funq("model-with-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.model-with-version.requested",
            responseType = "io.cx.model_registry.model-with-version.completed"
    )
    public Uni<ModelWithVersionCreateResult> handleModelWithVersionRequested(
            CloudEvent<JsonObject> event
    ) {
        ModelWithVersionCreateRequest request = requireValidData(
                event,
                ModelWithVersionCreateRequest.class,
                "Event data must be provided"
        );
        return orchestrationService.createModelWithVersionIdempotent(request);
    }

    @Funq("deploy-model-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.deploy-model-version.requested",
            responseType = "io.cx.model_registry.deploy-model-version.completed"
    )
    public Uni<DeployModelVersionResult> handleDeployModelVersionRequested(
            CloudEvent<JsonObject> event
    ) {
        DeployModelVersionRequest request = requireValidData(
                event,
                DeployModelVersionRequest.class,
                "Event data must be provided"
        );
        return orchestrationService.deployModelVersionIdempotent(request);
    }

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
