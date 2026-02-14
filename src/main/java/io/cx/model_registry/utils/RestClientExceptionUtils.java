package io.cx.model_registry.utils;

import io.cx.model_registry.exceptions.RestClientException;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.Scanner;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public class RestClientExceptionUtils {
    private static String MESSAGE_TEMPLATE = "REST client error: HTTP %d %s%nBody: %s";
    private static String A = "\\A";
    private static String EMPTY_STRING = "";
    private static String UNKNOWN_EXCEPTION = """
            Unknown cause; possible server connection issue or unsupported protocol version.
            Please check that the service is running on port specified in 'server.port' property.\r
            Possible SSL/Network error detected\s
            Check SSL certificates and network connectivity
            Verify Keycloak SSL configuration
            """;

    public static String extractMessage(Response response, String body) {
        String reason = Optional.of(response.getStatus())
                .filter(not(v -> v == 0))
                .map(unused -> extractReason(response))
                .orElse(UNKNOWN_EXCEPTION);

        return String.format(MESSAGE_TEMPLATE,
                response.getStatus(),
                reason,
                body);
    }

    public static String extractReason(Response response) {
        return ofNullable(response.getStatusInfo())
                .map(Response.StatusType::getReasonPhrase)
                .orElse(EMPTY_STRING);
    }

    public static String extractBody(Response response) {
        return ofNullable(response)
                .filter(Response::hasEntity)
                .map(r -> {
                    try (ByteArrayInputStream stream = new ByteArrayInputStream(r.readEntity(byte[].class))) {
                        Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8).useDelimiter(A);
                        return scanner.hasNext() ? scanner.next() : EMPTY_STRING;
                    } catch (IOException e) {
                        return EMPTY_STRING;
                    }
                })
                .orElse(EMPTY_STRING);
    }

    public static String logDetailedError(RestClientException exception) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n=== REST CLIENT ERROR DETAILS ===\n");
        sb.append("Timestamp: ").append(new Date(exception.timestamp())).append("\n");
        sb.append("Status Code: ").append(exception.status()).append(")\n");

        sb.append("\n--- Request Information ---\n");
        sb.append("Method: ").append(exception.method()).append("\n");
        sb.append("URL: ").append(exception.url()).append("\n");

        sb.append("\n--- Response Headers ---\n");
        exception.headers().forEach((key, values) ->
                sb.append(key).append(": ").append(String.join(", ", (CharSequence) values)).append("\n")
        );

        sb.append("\n--- Response Body ---\n");
        if (exception.body() != null) {
            sb.append(exception.body()).append("\n");
        } else {
            sb.append("No response body available\n");
        }

        sb.append("=== END ERROR DETAILS ===\n");
        return sb.toString();
    }

}
