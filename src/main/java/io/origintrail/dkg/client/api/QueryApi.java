package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class QueryApi extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryApi.class);

    private static final String QUERY_PATH = "query";
    private static final String QUERY_RESULT_PATH = "query/result";
    private static final String PROOFS_PATH = "proofs:get";
    private static final String PROOFS_RESULT_PATH = "proofs:get/result";

    public QueryApi(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> query(String type, String query) {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(QUERY_PATH)
                .queryParameters(Collections.singletonMap("type", type))
                .build();

        MultiPartBody.MultiPartBodyBuilder bodyPublisher = MultiPartBody
                .builder()
                .addPart("query", query);

        HttpRequest request = createMultiPartFormRequest(uri, bodyPublisher);

        return sendAsyncRequest(request, HandlerId.class);
    }

    public CompletableFuture<JsonNode> getQueryResult(String handlerId) {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(QUERY_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, JsonNode.class);
    }

    public CompletableFuture<HandlerId> proofs(String requestBody, String assertions) {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(PROOFS_PATH)
                .queryParameters(Collections.singletonMap("assertions", assertions))
                .build();

        MultiPartBody.MultiPartBodyBuilder bodyPublisher = MultiPartBody
                .builder()
                .addPart("nquads", requestBody);

        HttpRequest request = createMultiPartFormRequest(uri, bodyPublisher);

        return sendAsyncRequest(request, HandlerId.class);
    }

    public CompletableFuture<JsonNode> getProofsResult(String handlerId)  {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(PROOFS_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, JsonNode.class);
    }
}
