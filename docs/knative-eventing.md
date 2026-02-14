# Knative Eventing for Workflow Functions

Manifest: `docs/knative-eventing.yaml`

## What this wiring does
- `KafkaSource` reads from Kafka topics:
  - `model-with-version-events`
  - `deploy-model-version-events`
- Events go to `Broker` `model-registry-broker`.
- Triggers route by CloudEvent `type` to Knative Service `kf-model-registry-client`.
- Quarkus Funqy dispatches by `@CloudEventMapping(trigger = ...)`.

## Required CloudEvent types
- `io.cx.model_registry.model-with-version.requested`
- `io.cx.model_registry.deploy-model-version.requested`

## Example event payloads (`data`)

### model-with-version
```json
{
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
