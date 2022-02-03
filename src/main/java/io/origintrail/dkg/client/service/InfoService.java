package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class InfoService extends ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoService.class);

    private static final String INFO_PATH = "info";

    public InfoService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<NodeInfo> getInfo() throws HttpResponseException, UnexpectedException {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(INFO_PATH)
                .build();

        HttpRequest request = createHttpGETRequest(uri);
        return sendAsyncRequest(request)
                .thenApply(body -> transformBody(body, NodeInfo.class));
    }
}
