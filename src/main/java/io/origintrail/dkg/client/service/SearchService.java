package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.response.HandlerId;
import io.origintrail.dkg.client.util.UriUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private static final String ENTITIES_SEARCH_PATH = "entities:search";
    private static final String ENTITIES_SEARCH_RESULT_PATH = "entities:search/result";
    private static final String ASSERTIONS_SEARCH_PATH = "assertions:search";
    private static final String ASSERTIONS_SEARCH_RESULT_PATH = "assertions:search/result";

    private final ApiRequestService apiRequestService;

    public SearchService(ApiRequestService apiRequestService) {
       this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions)
            throws CompletionException, RequestValidationException {

        validateEntitySearchOptions(entitySearchOptions);

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(ENTITIES_SEARCH_PATH)
                .queryParameters(entitySearchOptions.getQueryParameters())
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private void validateEntitySearchOptions(EntitySearchOptions entitySearchOptions) {
        if (entitySearchOptions == null
                || (StringUtils.isBlank(entitySearchOptions.getQuery())
                && StringUtils.isBlank(entitySearchOptions.getIds()))) {
            LOGGER.error(String.format("Entity search options 'query' or 'ids' are required. EntitySearchOptions: %s", entitySearchOptions));
            throw new RequestValidationException("Entity search options 'query' or 'ids' are required.");
        }
    }

    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId)
            throws CompletionException {

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(ENTITIES_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, JsonNode.class));
    }

    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions)
            throws CompletionException, RequestValidationException {

        validateAssertionSearchOptions(assertionSearchOptions);

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(ASSERTIONS_SEARCH_PATH)
                .queryParameters(assertionSearchOptions.getQueryParameters())
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private void validateAssertionSearchOptions(AssertionSearchOptions assertionSearchOptions) {
        if (assertionSearchOptions == null
                || (StringUtils.isBlank(assertionSearchOptions.getQuery()))) {
            LOGGER.error(String.format("Assertion search option 'query' is required. AssertionSearchOptions: %s", assertionSearchOptions));
            throw new RequestValidationException("Assertion search option 'query' is required.");
        }
    }

    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId)
            throws CompletionException {

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(ASSERTIONS_SEARCH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, JsonNode.class));
    }
}
