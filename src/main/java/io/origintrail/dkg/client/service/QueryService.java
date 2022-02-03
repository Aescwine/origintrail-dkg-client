package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.origintrail.dkg.client.exception.ClientRequestException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.SparqlQueryType;
import io.origintrail.dkg.client.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QueryService extends ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private static final String QUERY_PATH = "query";
    private static final String QUERY_RESULT_PATH = "query/result";
    private static final String PROOFS_PATH = "proofs:get";
    private static final String PROOFS_RESULT_PATH = "proofs:get/result";

    public QueryService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> query(SparqlQueryType type, String query) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(QUERY_PATH)
                .queryParameters(Collections.singletonMap("type", type.getValue()))
                .build();

        String sparqlQuery = createSparqlRequestBody(query);
        HttpRequest request = createHttpPOSTRequest(uri, sparqlQuery);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, HandlerId.class));
    }

    private String createSparqlRequestBody(String query) {
        ObjectNode sparqlQuery = OBJECT_MAPPER.createObjectNode();
        return sparqlQuery.put("query", query).toString();
    }

    public CompletableFuture<JsonNode> getQueryResult(String handlerId) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(QUERY_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, JsonNode.class));
    }

    public CompletableFuture<HandlerId> proofs(List<NQuad> nQuads, List<String> assertionIds)
            throws ClientRequestException, HttpResponseException, UnexpectedException {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(PROOFS_PATH)
                .queryParameters("assertions", assertionIds)
                .build();

        String nQuadsQuery = createNQuadsQuery(nQuads);
        HttpRequest request = createHttpPOSTRequest(uri, nQuadsQuery);

        return sendAsyncRequest(request)
                .thenApply(body -> transformBody(body, HandlerId.class));
    }

    private String createNQuadsQuery(List<NQuad> nQuads) {
        ObjectNode nQuadsObject = OBJECT_MAPPER.createObjectNode();
        return nQuadsObject.putPOJO("nquads", nQuads).toString();
    }

    public CompletableFuture<JsonNode> getProofsResult(String handlerId) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(PROOFS_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = createHttpGETRequest(uri);

        return sendAsyncRequest(request).thenApply(body -> transformBody(body, JsonNode.class));
    }
}
