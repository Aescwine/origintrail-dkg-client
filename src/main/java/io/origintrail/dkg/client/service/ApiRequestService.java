package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.origintrail.dkg.client.exception.DkgClientException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.ResponseBodyException;
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
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract class containing common methods for sending and managing HTTP requests to the DKG.
 */
@Getter
class ApiRequestService {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(10);
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
                .timeout(DEFAULT_TIMEOUT_DURATION)
                .uri(uri)
                .GET()
                .build();
    }

    HttpRequest createHttpPOSTRequest(URI uri, String body) {
        return HttpRequest.newBuilder()
                .timeout(DEFAULT_TIMEOUT_DURATION)
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", HttpMediaType.APPLICATION_JSON.value())
                .build();
    }

    HttpRequest createMultiPartFormRequest(URI uri, MultiPartBody.MultiPartBodyBuilder bodyPublisher) {
        return HttpRequest.newBuilder()
                .timeout(DEFAULT_TIMEOUT_DURATION)
                .uri(uri)
                .header("Content-Type", HttpMediaType.MULTIPART_FORM_DATA.value() + "; boundary=" + bodyPublisher.getBoundary())
                .POST(bodyPublisher.build())
                .build();
    }

    public CompletableFuture<String> sendAsyncRequest(HttpRequest request)
            throws UnexpectedException, HttpResponseException {

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (!isSuccessResponse(r)) {
                        logger.warn("Unsuccessful response status: {}, {}", r.statusCode(), r.body());
                        throw new HttpResponseException(r.statusCode(), r.body());
                    }
                    return r.body();
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof DkgClientException) {
                        throw (DkgClientException) ex.getCause();
                    }
                    logger.error("Unexpected error sending http request: {}", ex.getMessage());
                    throw new UnexpectedException(ex.getMessage(), ex.getCause());
                });
    }

    private boolean isSuccessResponse(HttpResponse<String> httpResponse) {
        return httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299;
    }

    @SuppressWarnings("unchecked")
    protected <T> T transformBody(String body, Class<T> contentClass) throws UnexpectedException {
        try {
            if (contentClass.isInstance(body)) {
                return (T) body;
            }
            return OBJECT_MAPPER.readValue(body, contentClass);
        } catch (JsonProcessingException e) {
            logger.error("Exception parsing response body content: {}", e.getMessage());
            throw new ResponseBodyException("Exception parsing response body content.", e.getCause());
        }
    }
}
