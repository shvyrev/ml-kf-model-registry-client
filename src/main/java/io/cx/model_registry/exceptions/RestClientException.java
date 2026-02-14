package io.cx.model_registry.exceptions;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
public class RestClientException extends RuntimeException {
    private int status;
    private MultivaluedMap<String, Object> headers;
    private String body;
    private String url;
    private String method;
    private Long timestamp = System.currentTimeMillis();
    private Map<String, String> info;

    public RestClientException(String message) {
        super(message);
        info = collectInfo();

    }

    private Map<String, String> collectInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("hostname", getHostname());
        return info;
    }

    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
