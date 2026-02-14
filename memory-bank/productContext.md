# Product Context

This file provides a high-level overview of the project and the expected product that will be created. Initially it is based upon projectBrief.md (if provided) and all other available project-related information in the working directory. This file is intended to be updated as the project evolves, and should be used to inform all other modes of the project's goals and context.
2026-02-11 08:06:47 - Log of updates made will be appended as footnotes to the end of this file.

*

## Project Goal

*   Develop a Quarkus-based client for a model registry (likely Kubeflow Model Registry).
*   Provide RESTful API endpoints to manage registered models and model versions.
*   Integrate with Knative Events via Funqy for event-driven architecture.
*   Serve as a lightweight Java microservice that can be deployed as a native executable.

## Key Features

*   Quarkus framework for fast startup and low memory footprint.
*   REST API with OpenAPI documentation and Swagger UI.
*   Integration with Knative Events for cloud‑native event handling.
*   Support for model versioning, artifacts, and metadata.
*   Docker and native image packaging options.
*   Lombok for reduced boilerplate code.
*   Jackson for JSON serialization.

## Overall Architecture

*   **Technology Stack:** Java 21, Quarkus 3.31.2, Maven.
*   **API Layer:** JAX‑RS (RESTEasy) with Jackson serialization.
*   **Eventing:** Funqy Knative Events Binding.
*   **Clients:** REST Client for outgoing calls (e.g., to a model‑registry backend).
*   **Build:** Maven with Quarkus plugin; supports JVM, uber‑jar, and native executables.
*   **Deployment:** Docker images for JVM, native, and native‑micro.
*   **Code Structure:**  
    *   `src/main/java/io/cx/model_registry/client/` – HTTP client classes.  
    *   `src/main/java/io/cx/model_registry/dto/` – DTOs for models, versions, artifacts, metadata.  
    *   `src/main/java/io/cx/model_registry/resource/` – REST resources (endpoints).  
    *   `src/main/java/io/cx/model_registry/service/` – business logic.
*   **Configuration:** `application.properties` for Quarkus settings.

## Dependencies (from pom.xml)

*   quarkus‑smallrye‑openapi, quarkus‑swagger‑ui – API documentation.
*   lombok – boilerplate reduction.
*   jackson‑databind, jackson‑datatype‑jsr310 – JSON handling.
*   quarkus‑funqy‑knative‑events – event‑driven capabilities.
*   quarkus‑rest‑jackson, quarkus‑rest‑client‑jackson – REST server and client.
*   quarkus‑arc – dependency injection.
*   quarkus‑junit5 – testing.