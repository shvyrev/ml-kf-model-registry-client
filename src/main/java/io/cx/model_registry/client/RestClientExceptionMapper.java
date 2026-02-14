package io.cx.model_registry.client;

import io.cx.model_registry.exceptions.RestClientException;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.cx.model_registry.utils.RestClientExceptionUtils.*;

@NoArgsConstructor
@Slf4j
public final class RestClientExceptionMapper {

    public static final String UNKNOWN = "UNKNOWN";
    public static final String EMPTY_STRING = "";

    @ClientExceptionMapper
    public static RuntimeException toException(Response response) {
        if (response == null) {
            RestClientException exception = new RestClientException("REST client error: empty response")
                    .status(0)
                    .body(EMPTY_STRING);
            log.error("REST client error: status={}, method={}, url={}, body={}",
                    exception.status(), exception.method(), exception.url(), exception.body());
            return exception;
        }

        if (response.getStatus() < 400) {
            return null;
        }

        String body = extractBody(response);
        RestClientException exception = new RestClientException(extractMessage(response, body))
                .status(response.getStatus())
                .headers(response.getHeaders())
                .body(body);

        log.error("REST client error: status={}, reason={}, method={}, url={}, body={}",
                response.getStatus(),
                extractReason(response),
                exception.method(),
                exception.url(),
                body);

        return exception;
    }
}
