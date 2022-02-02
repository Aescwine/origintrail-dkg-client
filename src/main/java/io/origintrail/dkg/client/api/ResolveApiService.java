package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class ResolveApiService extends ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveApiService.class);

    private static final String RESOLVE_PATH = "resolve";
    private static final String RESOLVE_RESULT_PATH = "resolve/result";

    public ResolveApiService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> resolve(List<String> assertionIds) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(RESOLVE_PATH)
                .queryParameters("ids", assertionIds)
                .build();

        return resolve(uri);
    }

    private CompletableFuture<HandlerId> resolve(URI uri) {
        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, HandlerId.class);
    }

    public CompletableFuture<JsonNode> getResolveResult(String handlerId) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(RESOLVE_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, JsonNode.class);
    }
}
