package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.HttpRequestException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.query.Query;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code DkgClient} provides a client interface for interacting with the OriginTrail Decentralized Knowledge Graph API.
 * See https://origintrail.io/ and https://app.swaggerhub.com/apis/TraceLabs/ot-node-v6/
 */
public class DkgClient {

    private final InfoApi infoApi;
    private final PublishApi publishApi;
    private final ResolveApi resolveApi;
    private final SearchApi searchApi;
    private final QueryApi queryApi;

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8900;

    private static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(10);

    public DkgClient() {
        this(DEFAULT_HOST, DEFAULT_PORT, false);
    }

    public DkgClient(String host, int port) {
        this(host, port, false);
    }

    public DkgClient(String host, int port, boolean sshEnabled) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT_DURATION)
                .version(HttpClient.Version.HTTP_2)
                .build();

        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(host, port, sshEnabled ? "https" : "http");

        infoApi = new InfoApi(httpClient, httpUrlOptions);
        publishApi = new PublishApi(httpClient, httpUrlOptions);
        resolveApi = new ResolveApi(httpClient, httpUrlOptions);
        searchApi = new SearchApi(httpClient, httpUrlOptions);
        queryApi = new QueryApi(httpClient, httpUrlOptions);
    }

    public CompletableFuture<NodeInfo> getInfo() {
        return infoApi.getInfo();
    }

    public CompletableFuture<HandlerId> publish(String fileName, byte[] fileData, PublishOptions publishOptions) {
        return publishApi.publish(fileName, fileData, publishOptions);
    }

    public CompletableFuture<HandlerId> publish(String filePath, PublishOptions publishOptions) {
        try {
            Path publishFilePath = Paths.get(filePath);
            Path publishFileName = publishFilePath.getFileName();

            byte[] publishData = Files.readAllBytes(publishFilePath);

            return publishApi.publish(publishFileName.toString(), publishData, publishOptions);
        } catch (IOException e) {
           throw new HttpRequestException(String.format("Exception reading publish file: %s", filePath), e.getCause());
        }
    }

    public CompletableFuture<JsonNode> getPublishResult(String handlerId) {
        return publishApi.getPublishResult(handlerId);
    }

    public CompletableFuture<HandlerId> resolve(String assertionIds) {
        return resolveApi.resolve(assertionIds);
    }

    public CompletableFuture<JsonNode> getResolveResult(String handlerId) {
        return resolveApi.getResolveResult(handlerId);
    }

    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions) {
        return searchApi.entitiesSearch(entitySearchOptions);
    }

    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId) {
        return searchApi.getEntitiesSearchResult(handlerId);
    }

    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions) {
        return searchApi.assertionsSearch(assertionSearchOptions);
    }

    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId) {
        return searchApi.getAssertionsSearchResult(handlerId);
    }

    public CompletableFuture<HandlerId> query(String type, String requestBody) {
        return queryApi.query(type, requestBody);
    }

    public CompletableFuture<HandlerId> query(String type, AbstractQueryBuilder<?> queryBuilder) {
        Query query = queryBuilder.build();

        return queryApi.query(type, query.toString());
    }

    public CompletableFuture<JsonNode> getQueryResult(String handlerId) {
        return queryApi.getQueryResult(handlerId);
    }

    public CompletableFuture<HandlerId> proofs(String nquads, String assertion) {
        return queryApi.proofs(nquads, assertion);
    }

    public CompletableFuture<JsonNode> getProofsResult(String handlerId) {
        return queryApi.getProofsResult(handlerId);
    }
}
