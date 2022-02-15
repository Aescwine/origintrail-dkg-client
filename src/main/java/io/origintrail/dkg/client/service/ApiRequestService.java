package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.origintrail.dkg.client.exception.DkgClientException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.ResponseBodyException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.http.HttpMediaType;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Class containing common methods for sending and managing HTTP requests to the DKG.
 */
@Getter
public class ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestService.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(10);
    private final HttpClient httpClient;
    private final HttpUrlOptions httpUrlOptions;

    public ApiRequestService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        this.httpClient = httpClient;
        this.httpUrlOptions = httpUrlOptions;
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
            throws CompletionException {
        LOGGER.debug("Sending async request: {}", request.uri().toString());
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (!isSuccessResponse(r)) {
                        LOGGER.warn("Unsuccessful response status: {}, {}", r.statusCode(), r.body());
                        throw new HttpResponseException(r.statusCode(), r.body());
                    }
                    return r.body();
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof DkgClientException) {
                        throw new CompletionException(ex.getCause());
                    }
                    LOGGER.error("Unexpected error sending http request: {}", ex.getMessage());
                    throw new CompletionException(new UnexpectedException(ex.getMessage(), ex.getCause()));
                });
    }

    private boolean isSuccessResponse(HttpResponse<String> httpResponse) {
        return httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299;
    }

    @SuppressWarnings("unchecked")
    protected <T> T transformBody(String body, Class<T> contentClass) throws ResponseBodyException {
        try {
            return OBJECT_MAPPER.readValue(body, contentClass);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception parsing response body content: {}", e.getMessage());
            throw new ResponseBodyException("Exception parsing response body content.", e.getCause());
        }
    }
}
