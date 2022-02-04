package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.util.UriUtil;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class InfoService{

    private static final String INFO_PATH = "info";

    private final ApiRequestService apiRequestService;

    public InfoService(ApiRequestService apiRequestService) {
        this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<NodeInfo> getNodeInfo() throws CompletionException {

        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(INFO_PATH)
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);
        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, NodeInfo.class));
    }
}
