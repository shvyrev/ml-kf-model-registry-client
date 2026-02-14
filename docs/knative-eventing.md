# Knative Eventing for Workflow Functions

Manifest: `docs/knative-eventing.yaml`

## What this wiring does
- `KafkaSource` reads from Kafka topics:
  - `model-with-version-events`
  - `deploy-model-version-events`
- Events go to `Broker` `model-registry-broker`.
- Triggers route by CloudEvent `type` to Knative Service `kf-model-registry-client`.
- Quarkus Funqy dispatches by `@CloudEventMapping(trigger = ...)`.
- On terminal workflow failure, service publishes `io.cx.model_registry.workflow.failed` to the same Broker.
- DLQ trigger routes failed workflow events to Kafka topic `model-registry-workflow-dlq` via `KafkaSink`.

## Required CloudEvent types
- `io.cx.model_registry.model-with-version.requested`
- `io.cx.model_registry.deploy-model-version.requested`
- `io.cx.model_registry.workflow.failed` (emitted by service to DLQ route)

## Example event payloads (`data`)

### model-with-version
```json
{
  "idempotencyKey": "evt-9c6b5f95-8d35-45b8-8bd2-6e8f879f9f0a",
  "model": {
    "name": "my-model",
    "externalId": "my-model-ext",
    "description": "created from event"
  },
  "version": {
    "name": "v1",
    "externalId": "my-model-v1-ext",
    "description": "created from event"
  }
}
```

### deploy-model-version
```json
{
  "idempotencyKey": "evt-9f7444fb-3d2a-4ea5-8bd9-6dd32a2a7f65",
  "servingEnvironment": {
    "name": "prod-env",
    "externalId": "prod-env-ext",
    "description": "created from event"
  },
  "inferenceService": {
    "name": "my-model-is",
    "externalId": "my-model-is-ext",
    "description": "created from event",
    "registeredModelId": "<MODEL_ID>",
    "modelVersionId": "<MODEL_VERSION_ID>",
    "runtime": "kserve"
  },
  "serve": {
    "name": "serve-action",
    "externalId": "serve-action-ext",
    "description": "created from event",
    "modelVersionId": "<MODEL_VERSION_ID>"
  }
}
```

## Apply
```bash
kubectl apply -f docs/knative-eventing.yaml
```

## Notes
- Producer must send **valid CloudEvents** to Kafka records (structured or binary).
- Update image, namespace and Kafka bootstrap server in YAML.
- `idempotencyKey` is strongly recommended for at-least-once delivery.
- This service keeps a dedup store with TTL and publishes final failed executions to Knative Broker as CloudEvents.
