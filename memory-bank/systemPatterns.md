# System Patterns *Optional*

This file documents recurring patterns and standards used in the project.
It is optional, but recommended to be updated as the project evolves.
2026-02-11 08:06:47 - Log of updates made.

*

## Coding Patterns

*   **DTOs with Lombok:** All data transfer objects use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` for concise code.
*   **Package by Feature:** Structure follows `io.cx.model_registry` with sub‑packages `client`, `dto`, `resource`, `service`.
*   **REST Resources:** JAX‑RS endpoints are placed in `resource` package, using `@Path`, `@GET`/`@POST`/etc., and returning DTOs.
*   **Client Abstraction:** HTTP client logic is separated into `client` package (e.g., `ModelRegistryClient`, `ModelVersionClient`).
*   **Jackson Serialization:** JSON serialization uses Jackson with Java 8 time support (`jackson‑datatype‑jsr310`).
*   **Error Handling:** REST resources may define custom error response classes (e.g., `RegisteredModelResource.ErrorResponse`, `ModelVersionResource.ErrorResponse`).
*   **MicroProfile REST Client Pattern:** REST client interfaces use `@RegisterRestClient`, `@RegisterClientHeaders`, `@Path` with relative endpoints; configuration via `application.properties`.
*   **Service Layer Aggregation:** Business logic centralized in `ModelRegistryService` that orchestrates multiple REST clients (RegisteredModel and ModelVersion).
*   **Consistent API Design:** REST resources follow uniform URL patterns: `/api/v1/{resource}` for external API, while client interfaces map to backend paths (`/model_version`).

## Architectural Patterns

*   **Layered Architecture:** Presentation (resource) → Business (service) → Data/Client (client) → DTOs.
*   **Event‑Driven Extension:** Funqy Knative Events binding allows the service to react to cloud events.
*   **Configuration‑Driven:** Quarkus `application.properties` controls runtime behavior (ports, logging, etc.).
*   **Native‑First:** Build supports GraalVM native images for fast startup and small footprint.
*   **Container‑Ready:** Dockerfiles provided for JVM, native, and native‑micro deployments.

## Testing Patterns

*   **QuarkusTest:** Integration tests likely use `@QuarkusTest` (based on quarkus‑junit5 dependency).
*   **REST Assured:** Possibly used for endpoint testing (not yet visible in current files).
*   **Unit vs Integration:** Separation between unit tests (plain JUnit) and integration tests (QuarkusTest) may exist.

## Build & Deployment Patterns

*   **Maven Multi‑Stage Build:** Standard Maven lifecycle with Quarkus plugin (`quarkus:dev`, `quarkus:build`).
*   **Profile‑Based Packaging:** `native` profile triggers native executable build.
*   **Docker Layering:** Multiple Dockerfile variants (jvm, legacy‑jar, native, native‑micro) optimize for different runtime characteristics.