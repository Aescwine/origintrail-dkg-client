package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.http.HttpMediaType;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import lombok.Getter;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract class containing common methods for sending and managing HTTP requests to the DKG.
 */
@Getter
abstract class ApiRequestService {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final HttpUrlOptions httpUrlOptions;
    private final Logger logger;

    public ApiRequestService(HttpClient httpClient, HttpUrlOptions httpUrlOptions, Logger logger) {
        this.httpClient = httpClient;
        this.httpUrlOptions = httpUrlOptions;
        this.logger = logger;
    }

    HttpRequest createHttpGETRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
    }

    HttpRequest createMultiPartFormRequest(URI uri, MultiPartBody.MultiPartBodyBuilder bodyPublisher) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", HttpMediaType.MULTIPART_FORM_DATA.value() + "; boundary=" + bodyPublisher.getBoundary())
                .POST(bodyPublisher.build())
                .build();
    }

    public <T> CompletableFuture<T> sendAsyncRequest(HttpRequest request, Class<T> contentClass)
            throws UnexpectedException, HttpResponseException {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (isSuccessResponse(r) && r.body() != null) {
                        try {
                            return transformBody(r, contentClass);
                        } catch (JsonProcessingException e) {
                            logger.error("Exception parsing response body content {}", e.getMessage());
                            throw new UnexpectedException("Exception parsing response body content", e.getCause());
                        }
                    }
                    throw new HttpResponseException(r.statusCode(), r.body());
                })
                .exceptionally(ex -> {
                    logger.error("Unexpected error sending http request: {}", ex.getMessage());
                    throw new UnexpectedException(ex.getMessage(), ex.getCause());
                });
    }

    private boolean isSuccessResponse(HttpResponse<String> httpResponse) {
        return httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299;
    }

    @SuppressWarnings("unchecked")
    private <T> T transformBody(HttpResponse<String> response, Class<T> contentClass) throws JsonProcessingException {
        if (contentClass.isInstance(response.body())) {
            return (T) response.body();
        }
        return OBJECT_MAPPER.readValue(response.body(), contentClass);
    }
}
