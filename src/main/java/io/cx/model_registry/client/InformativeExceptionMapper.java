package io.cx.model_registry.client;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Провайдер для информативного маппинга исключений REST клиента (реактивная версия).
 *
 * ВАЖНО: Метод ДОЛЖЕН быть статическим для реактивного клиента!
 *
 * Использование:
 * <pre>
 * {@code
 * @RegisterRestClient(configKey = "kubernetes-api")
 * @RegisterProvider(InformativeExceptionMapper.class)
 * public interface KubernetesApiClient { ... }
 * }
 * </pre>
 */
public class InformativeExceptionMapper {

    private static final Logger LOG = Logger.getLogger(InformativeExceptionMapper.class);
    private static final int MAX_LOG_BODY_LENGTH = 1000;
    private static final int MAX_EXCEPTION_BODY_LENGTH = 200;

    /**
     * СТАТИЧЕСКИЙ метод для маппинга исключений (обязательно для реактивного клиента!)
     */
    @ClientExceptionMapper
    public static RuntimeException toException(Response response) {
        try {
            ExceptionContext context = buildExceptionContext(response);
            logDetailedError(context);
            return createInformativeException(context);

        } catch (Exception e) {
            LOG.error("Error while mapping client exception", e);
            return new RuntimeException("Client error: " + response.getStatus(), e);
        }
    }

    private static ExceptionContext buildExceptionContext(Response response) {
        ExceptionContext context = new ExceptionContext();

        context.setStatusCode(response.getStatus());
        context.setStatusInfo(response.getStatusInfo());
        context.setStatusFamily(response.getStatusInfo().getFamily());
        context.setResponseHeaders(collectHeaders(response.getHeaders()));
        context.setResponseBody(extractResponseBody(response));
        context.setRequestMethod("UNKNOWN"); // Для получения реального метода нужен контекст запроса
        context.setRequestUrl("UNKNOWN");    // Для получения реального URL нужен контекст запроса
        context.setTimestamp(System.currentTimeMillis());
        context.setClientInfo(collectClientInfo());

        return context;
    }

    private static Map<String, List<String>> collectHeaders(Map<String, List<Object>> headers) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (headers != null) {
            headers.forEach((key, values) ->
                    result.put(key, values.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList()))
            );
        }
        return result;
    }

    private static String extractResponseBody(Response response) {
        try {
            if (response.hasEntity()) {
                Object entity = response.getEntity();
                if (entity instanceof String) {
                    return (String) entity;
                } else if (entity instanceof InputStream) {
                    return readInputStream((InputStream) entity);
                } else {
                    return entity != null ? entity.toString() : "No entity body";
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to extract response body", e);
        }
        return null;
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        // Более надежное чтение
        java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> collectClientInfo() {
        Map<String, String> info = new HashMap<>();
        try {
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("osName", System.getProperty("os.name"));
            info.put("hostname", java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            // Игнорируем ошибки сбора информации
        }
        return info;
    }

    private static void logDetailedError(ExceptionContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("  REST CLIENT ERROR DETAILS\n");
        sb.append("=".repeat(70)).append("\n");

        sb.append("Timestamp: ").append(new Date(context.getTimestamp())).append("\n");
        sb.append("HTTP Status: ").append(context.getStatusCode())
                .append(" ").append(context.getStatusInfo())
                .append(" (").append(context.getStatusFamily()).append(")\n\n");

        sb.append("--- Request ---\n");
        sb.append("Method: ").append(context.getRequestMethod()).append("\n");
        sb.append("URL: ").append(context.getRequestUrl()).append("\n\n");

        sb.append("--- Response Headers ---\n");
        if (!context.getResponseHeaders().isEmpty()) {
            context.getResponseHeaders().forEach((key, values) ->
                    sb.append(key).append(": ").append(String.join(", ", values)).append("\n")
            );
        } else {
            sb.append("(no headers)\n");
        }
        sb.append("\n");

        sb.append("--- Response Body ---\n");
        if (context.getResponseBody() != null && !context.getResponseBody().trim().isEmpty()) {
            String body = context.getResponseBody();
            if (body.length() > MAX_LOG_BODY_LENGTH) {
                body = body.substring(0, MAX_LOG_BODY_LENGTH) + "\n... [truncated]";
            }
            sb.append(body).append("\n");
        } else {
            sb.append("(no body)\n");
        }
        sb.append("\n");

        sb.append("--- Client Info ---\n");
        context.getClientInfo().forEach((key, value) ->
                sb.append(key).append(": ").append(value).append("\n")
        );

        sb.append("=".repeat(70)).append("\n");

        LOG.error(sb.toString());
    }

    private static RuntimeException createInformativeException(ExceptionContext context) {
        String message = buildExceptionMessage(context);

        switch (context.getStatusFamily()) {
            case CLIENT_ERROR:
                return new ClientErrorException(message, context.getStatusCode(), context);
            case SERVER_ERROR:
                return new ServerErrorException(message, context.getStatusCode(), context);
            case REDIRECTION:
                return new RedirectionException(message, context.getStatusCode(), context);
            case OTHER:
                // Код 0 обычно означает сетевую ошибку или проблему с SSL
                if (context.getStatusCode() == 0) {
                    return new NetworkException("Network/SSL error - no response received (status 0). " +
                            "Check connectivity, certificates, and firewall settings.", context);
                }
                return new RestClientException(message, context);
            default:
                return new RestClientException(message, context);
        }
    }

    private static String buildExceptionMessage(ExceptionContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("REST client error: ")
                .append(context.getStatusCode())
                .append(" ")
                .append(context.getStatusInfo());

        if (context.getResponseBody() != null && !context.getResponseBody().trim().isEmpty()) {
            String body = context.getResponseBody();
            if (body.length() > MAX_EXCEPTION_BODY_LENGTH) {
                body = body.substring(0, MAX_EXCEPTION_BODY_LENGTH) + "...";
            }
            sb.append(" | Response: ").append(truncateJson(body));
        }

        return sb.toString();
    }

    /**
     * Упрощает длинные JSON-ответы для отображения в сообщении исключения
     */
    private static String truncateJson(String body) {
        if (body == null || body.length() <= MAX_EXCEPTION_BODY_LENGTH) {
            return body;
        }

        // Пытаемся найти конец первого объекта/массива для более читаемого обрезания
        int firstBrace = body.indexOf('{');
        int firstBracket = body.indexOf('[');
        int cutPoint = Math.min(
                firstBrace > 0 ? body.indexOf('}', firstBrace) + 1 : Integer.MAX_VALUE,
                firstBracket > 0 ? body.indexOf(']', firstBracket) + 1 : Integer.MAX_VALUE
        );

        if (cutPoint < MAX_EXCEPTION_BODY_LENGTH && cutPoint > 0) {
            return body.substring(0, cutPoint) + "...";
        }

        return body.substring(0, MAX_EXCEPTION_BODY_LENGTH) + "...";
    }

    // =====================================================================
    // Контекст ошибки (статический вложенный класс)
    // =====================================================================
    public static class ExceptionContext {
        private int statusCode;
        private Response.StatusType statusInfo;
        private Response.Status.Family statusFamily;
        private Map<String, List<String>> responseHeaders = new LinkedHashMap<>();
        private String responseBody;
        private String requestMethod;
        private String requestUrl;
        private long timestamp;
        private Map<String, String> clientInfo = new HashMap<>();

        // Getters/Setters
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

        public Response.StatusType getStatusInfo() { return statusInfo; }
        public void setStatusInfo(Response.StatusType statusInfo) { this.statusInfo = statusInfo; }

        public Response.Status.Family getStatusFamily() { return statusFamily; }
        public void setStatusFamily(Response.Status.Family statusFamily) { this.statusFamily = statusFamily; }

        public Map<String, List<String>> getResponseHeaders() { return responseHeaders; }
        public void setResponseHeaders(Map<String, List<String>> responseHeaders) { this.responseHeaders = responseHeaders; }

        public String getResponseBody() { return responseBody; }
        public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

        public String getRequestMethod() { return requestMethod; }
        public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

        public String getRequestUrl() { return requestUrl; }
        public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public Map<String, String> getClientInfo() { return clientInfo; }
        public void setClientInfo(Map<String, String> clientInfo) { this.clientInfo = clientInfo; }
    }

    // =====================================================================
    // Типизированные исключения
    // =====================================================================
    public static class ClientErrorException extends RuntimeException {
        private final int statusCode;
        private final ExceptionContext context;

        public ClientErrorException(String message, int statusCode, ExceptionContext context) {
            super(message);
            this.statusCode = statusCode;
            this.context = context;
        }

        public int getStatusCode() { return statusCode; }
        public ExceptionContext getContext() { return context; }
    }

    public static class ServerErrorException extends RuntimeException {
        private final int statusCode;
        private final ExceptionContext context;

        public ServerErrorException(String message, int statusCode, ExceptionContext context) {
            super(message);
            this.statusCode = statusCode;
            this.context = context;
        }

        public int getStatusCode() { return statusCode; }
        public ExceptionContext getContext() { return context; }
    }

    public static class RedirectionException extends RuntimeException {
        private final int statusCode;
        private final ExceptionContext context;

        public RedirectionException(String message, int statusCode, ExceptionContext context) {
            super(message);
            this.statusCode = statusCode;
            this.context = context;
        }

        public int getStatusCode() { return statusCode; }
        public ExceptionContext getContext() { return context; }
    }

    public static class NetworkException extends RuntimeException {
        private final ExceptionContext context;

        public NetworkException(String message, ExceptionContext context) {
            super(message);
            this.context = context;
        }

        public ExceptionContext getContext() { return context; }
    }

    public static class RestClientException extends RuntimeException {
        private final ExceptionContext context;

        public RestClientException(String message, ExceptionContext context) {
            super(message);
            this.context = context;
        }

        public ExceptionContext getContext() { return context; }
    }
}