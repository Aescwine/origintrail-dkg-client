package io.origintrail.dkg.client.api;

import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

class InfoApi extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoApi.class);

    private static final String INFO_PATH = "info";

    public InfoApi(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<NodeInfo> getInfo() {

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .path(INFO_PATH)
                .build();

        HttpRequest request = createHttpGETRequest(uri);
        return sendAsyncRequest(request, NodeInfo.class);
    }
}
