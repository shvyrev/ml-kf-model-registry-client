package io.cx.model_registry.client;

import io.cx.model_registry.exceptions.RestClientException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.client.impl.RestClientRequestContext;

import static io.cx.model_registry.utils.RestClientExceptionUtils.*;

@Slf4j
//@Provider
//public class RestClientExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

public final class RestClientExceptionMapper {

    public static RuntimeException mapException(Response response, ClientRequestContext requestContext) {
        String url = requestContext.getUri().toString();
        String method = requestContext.getMethod();
        String body = extractBody(response);
        String message = extractMessage(response, body);

        RestClientException result = new RestClientException(message)
                .url(url)
                .method(method)
                .status(response.getStatus())
                .headers(response.getHeaders())
                .body(body);

        log.error(logDetailedError(result));
        return result;
    }

    public static RuntimeException mapException(Response response, RestClientRequestContext ctx) {
        String url = ctx.getUri().toString();
        String method = ctx.getHttpMethod();
        String body = extractBody(response);
        String message = extractMessage(response, body);

        RestClientException result = new RestClientException(message)
                .url(url)
                .method(method)
                .status(response.getStatus())
                .headers(response.getHeaders())
                .body(body);

        log.error(logDetailedError(result));
        return result;    }

    public static RuntimeException mapException(Response response, Request request, UriInfo uri) {
        String url = uri.getAbsolutePath().toString();
        String method = request.getMethod();
        String body = extractBody(response);
        String message = extractMessage(response, body);

        RestClientException result = new RestClientException(message)
                .url(url)
                .method(method)
                .status(response.getStatus())
                .headers(response.getHeaders())
                .body(body);

        log.error(logDetailedError(result));
        return result;    }

//    @Context
//    ClientRequestContext requestContext;

//    @Override
//    public RuntimeException toThrowable(Response response) {
//        String url = requestContext.getUri().toString();
//        String method = requestContext.getMethod();
//        String body = extractBody(response);
//        String message = extractMessage(response, body);
//
//        var result = new RestClientException(message)
//                .url(url)
//                .method(method)
//                .status(response.getStatus())
//                .headers(response.getHeaders())
//                .body(body);
//
//        log.error(logDetailedError(result));
//
//        return result;
//    }
}
