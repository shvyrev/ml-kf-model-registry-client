# Decision Log

This file records architectural and implementation decisions using a list format.
2026-02-11 08:06:47 - Log of updates made.
2026-02-11 08:27:12 - Decision on ModelVersion client implementation approach.

*

## Decision

2026-02-11 08:06:47 - Initialize Memory Bank for project documentation.

## Rationale

*   The project lacked a centralized knowledge repository, making it difficult to maintain context across development sessions.
*   Memory Bank provides a structured way to capture project goals, architecture, patterns, decisions, and progress.
*   This aligns with the user's custom instructions to maintain project context and enable cross‑mode awareness.

## Implementation Details

*   Created `memory‑bank/` directory with five core files: `productContext.md`, `activeContext.md`, `systemPatterns.md`, `decisionLog.md`, `progress.md`.
*   Populated files with information extracted from existing project artifacts (README.md, pom.xml, source code structure).
*   Used initial content templates from user instructions, adding project‑specific details.
*   Timestamp format: YYYY‑MM‑DD HH:MM:SS (local Moscow time, UTC+3).

## Decision

2026-02-11 08:27:12 - Implement ModelVersion client as separate REST client interface following existing patterns.

## Rationale

*   The project already has a `ModelRegistryClient` for RegisteredModel operations, establishing a clear pattern.
*   ModelVersion operations are distinct enough to warrant a separate client interface for better separation of concerns.
*   OpenAPI specification defines separate endpoints for ModelVersion management under `/model_version` and `/model_versions` paths.
*   This approach maintains consistency with the existing architecture and makes the codebase more maintainable.

## Implementation Details

*   Create `ModelVersionClient` interface in `io.cx.model_registry.client` package.
*   Use same annotations as `ModelRegistryClient`: `@RegisterRestClient`, `@RegisterClientHeaders`, `@Path("/model_version")`.
*   Define methods for all ModelVersion operations from OpenAPI spec:
    *   `findModelVersion` (search by name/externalId/parentResourceId)
    *   `getModelVersions` (list with pagination/filtering)
    *   `createModelVersion` (POST to `/model_versions`)
    *   `getModelVersion` (GET by ID)
    *   `updateModelVersion` (PATCH by ID)
*   Create corresponding DTOs: `ModelVersion`, `ModelVersionCreate`, `ModelVersionUpdate`, `ModelVersionList`, `ModelVersionState`.
*   Extend `ModelRegistryService` or create separate service layer to expose business logic.
*   Create `ModelVersionResource` REST resource for external API exposure.

## Decision

2026-02-11 10:07:09 - Implementation of ModelVersion client completed successfully.

## Rationale

*   All planned components have been implemented and the project compiles without errors.
*   The implementation follows the established patterns of the existing `ModelRegistryClient` for consistency.
*   The REST resource provides a complete API for ModelVersion operations as specified in the OpenAPI spec.

## Implementation Details

*   Created DTO classes with Lombok annotations matching OpenAPI schema definitions.
*   Implemented `ModelVersionClient` with 5 REST methods using MicroProfile REST Client.
*   Extended `ModelRegistryService` with 8 business methods for ModelVersion operations (CRUD, archiving, custom properties).
*   Created `ModelVersionResource` with 8 endpoints following the same error handling pattern as `RegisteredModelResource`.
*   Added configuration for `ModelVersionClient` in `application.properties` with appropriate timeouts.
*   Verified compilation with `mvn compile` (exit code 0).
*   All open questions resolved: used existing service layer, relative path `/model_version`, DTO fields from OpenAPI.

## Decision

2026-02-11 10:45:00 - Implement comprehensive debug endpoint for Kubeflow Model Registry integration.

## Rationale

*   The existing `DebugResource` only provided basic connectivity tests and lacked coverage for all client operations.
*   A unified debug endpoint is needed to verify the integration with the remote Model Registry API, log request/response details, and facilitate troubleshooting.
*   Each public method of `ModelRegistryClient` and `ModelVersionClient` should have a corresponding GET endpoint that invokes the method with valid dummy parameters and returns a structured JSON trace.

## Implementation Details

*   Extended `DebugResource` with injection of `ModelVersionClient`.
*   Added inner DTO classes (`DebugResponse`, `RequestDetails`, `ResponseDetails`, `ErrorDetails`) for consistent JSON output.
*   Created helper methods to generate dummy data for `RegisteredModelCreate`, `RegisteredModelUpdate`, `ModelVersionCreate`, `ModelVersionUpdate`.
*   Implemented 12 new GET endpoints:
    *   For `ModelRegistryClient`: `/debug/find-registered-model`, `/debug/get-registered-models`, `/debug/create-registered-model`, `/debug/get-registered-model`, `/debug/update-registered-model`, `/debug/get-registered-model-versions`, `/debug/create-registered-model-version`
    *   For `ModelVersionClient`: `/debug/find-model-version`, `/debug/get-model-versions`, `/debug/create-model-version`, `/debug/get-model-version`, `/debug/update-model-version`
*   Each endpoint logs the invocation, parameters, and outcome; builds a detailed request/response trace; and returns a unified `DebugResponse` JSON.
*   The implementation follows consistent error handling: exceptions are captured and included in the response without breaking HTTP 200 (errors are represented in the `error` field).
*   The project compiles successfully (`mvn compile` exit code 0) with no breaking changes.

## Decision

2026-02-13 09:55:45 - Implement DTO classes and HTTP client for InferenceService entity.

## Rationale

*   The OpenAPI specification defines InferenceService as a core entity representing a deployed ModelVersion in a ServingEnvironment.
*   The project already has similar DTO and client patterns for other entities (RegisteredModel, ModelVersion, Experiment, Artifact).
*   Completing the InferenceService client enables full integration with Model Registry's inference capabilities.
*   The implementation follows the established project patterns: Lombok @Data/@Builder, Jackson annotations, MicroProfile REST client, Russian language comments.

## Implementation Details

*   Created 5 DTO classes in package `io.cx.model_registry.dto.inferenceservices`:
    *   `InferenceServiceState` – enum with DEPLOYED/UNDEPLOYED values.
    *   `InferenceServiceCreate` – DTO for creation, includes mandatory `registeredModelId` and `servingEnvironmentId`.
    *   `InferenceServiceUpdate` – DTO for PATCH updates, includes optional `modelVersionId`, `runtime`, `desiredState`.
    *   `InferenceService` – main entity DTO, extends BaseResource fields plus InferenceService-specific fields.
    *   `InferenceServiceList` – paginated list with `items`, `nextPageToken`, `pageSize`, `size`.
*   All DTOs use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` and Jackson `@JsonInclude(NON_NULL)`.
*   Fields are annotated with `@JsonProperty` and documented with Russian comments describing their purpose.
*   Created `InferenceServiceClient` interface in `io.cx.model_registry.client` package:
    *   Implements 6 REST operations matching OpenAPI paths:
        *   `findInferenceService` – GET `/inference_service` with query parameters (name, externalId, parentResourceId).
        *   `getInferenceServices` – GET `/inference_services` with pagination/filtering.
        *   `createInferenceService` – POST `/inference_services` with `InferenceServiceCreate` body.
        *   `getInferenceService` – GET `/{inferenceserviceId}`.
        *   `updateInferenceService` – PATCH `/{inferenceserviceId}` with `InferenceServiceUpdate` body.
        *   `getInferenceServiceModel` – GET `/{inferenceserviceId}/model` (returns RegisteredModel).
        *   `getInferenceServiceVersion` – GET `/{inferenceserviceId}/version` (returns ModelVersion).
    *   Uses `@RegisterRestClient(configKey = "model-registry")`, `@RegisterClientHeaders`, `@Path("/inference_services")`.
    *   Returns `Uni<T>` reactive types consistent with other clients.
*   The implementation does not include ServeModel endpoints (separate entity) but provides the necessary foundation.