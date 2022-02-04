package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.model.response.HandlerId;
import io.origintrail.dkg.client.model.response.ResolveResult;
import io.origintrail.dkg.client.util.UriUtil;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ResolveService {

    private static final String RESOLVE_PATH = "resolve";
    private static final String RESOLVE_RESULT_PATH = "resolve/result";

    private final ApiRequestService apiRequestService;

    public ResolveService(ApiRequestService apiRequestService) {
       this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<HandlerId> resolve(List<String> assertionIds) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(RESOLVE_PATH)
                .queryParameters("ids", assertionIds)
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request).thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    public CompletableFuture<ResolveResult> getResolveResult(String handlerId) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(RESOLVE_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);

        return apiRequestService.sendAsyncRequest(request).thenApply(body -> apiRequestService.transformBody(body, ResolveResult.class));
    }
}
