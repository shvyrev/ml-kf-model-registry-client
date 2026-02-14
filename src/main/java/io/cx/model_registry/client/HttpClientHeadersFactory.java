package io.cx.model_registry.client;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class HttpClientHeadersFactory implements ClientHeadersFactory {

    @Override
    public MultivaluedMap<String, String> update(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders
    ) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();

        log.debug("Creating headers for Model Registry request");

        String token = ConfigProvider.getConfig()
                .getOptionalValue("model.registry.token", String.class)
                .orElse(null);

        String username = ConfigProvider.getConfig()
                .getOptionalValue("model.registry.username", String.class)
                .orElse(null);

        String password = ConfigProvider.getConfig()
                .getOptionalValue("model.registry.password", String.class)
                .orElse(null);

        if (token != null && !token.isBlank()) {
            result.add("Authorization", "Bearer " + token);
        } else if (username != null && !username.isBlank() && password != null) {
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            result.add("Authorization", "Basic " + encodedCredentials);
        }

        result.add("Accept", "application/json");

        log.debug("Headers prepared for Model Registry request");

        return result;
    }
}
