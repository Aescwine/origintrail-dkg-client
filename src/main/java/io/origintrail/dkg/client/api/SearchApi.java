package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
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

class SearchApi extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchApi.class);

    private static final String ENTITIES_SEARCH_PATH = "entities:search";
    private static final String ENTITIES_SEARCH_RESULT_PATH = "entities:search/result";
    private static final String ASSERTIONS_SEARCH_PATH = "assertions:search";
    private static final String ASSERTIONS_SEARCH_RESULT_PATH = "assertions:search/result";

    public SearchApi(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions) {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(ENTITIES_SEARCH_PATH)
                .queryParameters(entitySearchOptions.getQueryParameters())
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, HandlerId.class);
    }

    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId) {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(ENTITIES_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request, JsonNode.class);
    }

    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions) {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(ASSERTIONS_SEARCH_PATH)
                .queryParameters(assertionSearchOptions.getQueryParameters())
                .build();

        HttpRequest request = createHttpGETRequest(uri);
        return sendAsyncRequest(request, HandlerId.class);
    }

    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId) {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(ASSERTIONS_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);
        return sendAsyncRequest(request, JsonNode.class);
    }
}
