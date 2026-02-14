package io.cx.model_registry.client;

import io.cx.model_registry.exceptions.RestClientException;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;

import static io.cx.model_registry.utils.RestClientExceptionUtils.*;

@NoArgsConstructor
@Slf4j
public final class RestClientExceptionMapper {

    public static final String UNKNOWN = "UNKNOWN";
    public static final String EMPTY_STRING = "";

    @ClientExceptionMapper
    public static RuntimeException toException(Response response, Method invokedMethod, URI uri) {
        String method = extractHttpMethod(invokedMethod);
        String url = uri != null ? uri.toString() : UNKNOWN;

        if (response == null) {
            RestClientException exception = new RestClientException("REST client error: empty response")
                    .status(0)
                    .method(method)
                    .url(url)
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
                .method(method)
                .url(url)
                .body(body);

        log.error("REST client error: status={}, reason={}, method={}, url={}, body={}",
                response.getStatus(),
                extractReason(response),
                exception.method(),
                exception.url(),
                body);

        return exception;
    }

    private static String extractHttpMethod(Method invokedMethod) {
        if (invokedMethod == null) {
            return UNKNOWN;
        }

        for (Annotation annotation : invokedMethod.getAnnotations()) {
            HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
            if (httpMethod != null && httpMethod.value() != null && !httpMethod.value().isBlank()) {
                return httpMethod.value();
            }
        }
        return invokedMethod.getName();
    }
}
