package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.ClientRequestException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.util.UriUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchService extends ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private static final String ENTITIES_SEARCH_PATH = "entities:search";
    private static final String ENTITIES_SEARCH_RESULT_PATH = "entities:search/result";
    private static final String ASSERTIONS_SEARCH_PATH = "assertions:search";
    private static final String ASSERTIONS_SEARCH_RESULT_PATH = "assertions:search/result";

    public SearchService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions)
            throws ClientRequestException, HttpResponseException, UnexpectedException {

        validateEntitySearchOptions(entitySearchOptions);

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(ENTITIES_SEARCH_PATH)
                .queryParameters(entitySearchOptions.getQueryParameters())
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, HandlerId.class));
    }

    private void validateEntitySearchOptions(EntitySearchOptions entitySearchOptions) {
        if (entitySearchOptions == null
                || (StringUtils.isBlank(entitySearchOptions.getQuery())
                && StringUtils.isBlank(entitySearchOptions.getIds()))) {
            throw new ClientRequestException("Entity search options 'query' or 'ids' are required.");
        }
    }

    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId)
            throws HttpResponseException, UnexpectedException {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(ENTITIES_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, JsonNode.class));
    }

    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions)
            throws ClientRequestException, HttpResponseException, UnexpectedException {

        validateAssertionSearchOptions(assertionSearchOptions);

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(ASSERTIONS_SEARCH_PATH)
                .queryParameters(assertionSearchOptions.getQueryParameters())
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, HandlerId.class));
    }

    private void validateAssertionSearchOptions(AssertionSearchOptions assertionSearchOptions) {
        if (assertionSearchOptions == null
                || (StringUtils.isBlank(assertionSearchOptions.getQuery()))) {
            throw new ClientRequestException("Assertion search option 'query' is required.");
        }
    }

    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId)
            throws HttpResponseException, UnexpectedException {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(ASSERTIONS_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, JsonNode.class));
    }
}
