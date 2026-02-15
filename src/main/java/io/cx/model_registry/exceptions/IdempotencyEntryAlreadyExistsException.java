package io.cx.model_registry.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class IdempotencyEntryAlreadyExistsException extends WebApplicationException {

    public IdempotencyEntryAlreadyExistsException(String idempotencyKey) {
        super("Workflow with idempotency key '" + idempotencyKey + "' is already in progress",
                Response.Status.CONFLICT);
    }
}
