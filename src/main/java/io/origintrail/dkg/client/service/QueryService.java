package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.SparqlQueryType;
import io.origintrail.dkg.client.model.response.HandlerId;
import io.origintrail.dkg.client.model.response.ProofsResult;
import io.origintrail.dkg.client.model.response.QueryResult;
import io.origintrail.dkg.client.util.UriUtil;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static io.origintrail.dkg.client.service.ApiRequestService.OBJECT_MAPPER;

public class QueryService {

    private static final String QUERY_PATH = "query";
    private static final String QUERY_RESULT_PATH = "query/result";
    private static final String PROOFS_PATH = "proofs:get";
    private static final String PROOFS_RESULT_PATH = "proofs:get/result";

    private final ApiRequestService apiRequestService;

    public QueryService(ApiRequestService apiRequestService) {
       this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<HandlerId> query(SparqlQueryType type, String query) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(QUERY_PATH)
                .queryParameters(Collections.singletonMap("type", type.getValue()))
                .build();

        String sparqlQuery = createSparqlRequestBody(query);
        HttpRequest request = apiRequestService.createHttpPOSTRequest(uri, sparqlQuery);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private String createSparqlRequestBody(String query) {
        ObjectNode sparqlQuery = OBJECT_MAPPER.createObjectNode();
        return sparqlQuery.put("query", query).toString();
    }

    public CompletableFuture<QueryResult> getQueryResult(String handlerId) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(QUERY_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, QueryResult.class));
    }

    public CompletableFuture<HandlerId> proofs(List<NQuad> nQuads, List<String> assertionIds)
            throws CompletionException {

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(PROOFS_PATH)
                .queryParameters("assertions", assertionIds)
                .build();

        String nQuadsQuery = createNQuadsQuery(nQuads);
        HttpRequest request = apiRequestService.createHttpPOSTRequest(uri, nQuadsQuery);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private String createNQuadsQuery(List<NQuad> nQuads) {
        ObjectNode nQuadsObject = OBJECT_MAPPER.createObjectNode();
        return nQuadsObject.putPOJO("nquads", nQuads).toString();
    }

    public CompletableFuture<ProofsResult> getProofsResult(String handlerId) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(PROOFS_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, ProofsResult.class));
    }
}
