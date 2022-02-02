package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.ClientRequestException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.model.SparqlQueryType;
import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.query.Query;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code DkgClient} provides a client interface for interacting with the OriginTrail Decentralized Knowledge Graph API.
 * See https://origintrail.io/ and https://app.swaggerhub.com/apis/TraceLabs/ot-node-v6/
 */
public class DkgClient {

    private final InfoApiService infoApiService;
    private final PublishApiService publishApiService;
    private final ResolveApiService resolveApiService;
    private final SearchApiService searchApiService;
    private final QueryApiService queryApiService;

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8900;

    private static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(10);

    /**
     * Creates a DkgClient with default URL options: schema=http, HOST=localhost, POST=8900.
     */
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

        infoApiService = new InfoApiService(httpClient, httpUrlOptions);
        publishApiService = new PublishApiService(httpClient, httpUrlOptions);
        resolveApiService = new ResolveApiService(httpClient, httpUrlOptions);
        searchApiService = new SearchApiService(httpClient, httpUrlOptions);
        queryApiService = new QueryApiService(httpClient, httpUrlOptions);
    }

    /**
     * Get DKG node information.
     *
     * @return {@code NodeInfo} representing the response body containing node information.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<NodeInfo> getNodeInfo() throws HttpResponseException, UnexpectedException {
        return infoApiService.getInfo();
    }

    /**
     * Publish an Assertion on the DKG.
     *
     * @param fileName       The file name for the data being published. Must have file extension {@code .json}.
     * @param fileData       {@code byte[]} of the file data being published.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the published DKG Assertion.
     * @throws ClientRequestException if {@code fileName} does not have file extension '.json',
     *                                or {@code fileData} is not valid json, or {@code publishOptions} null.
     * @throws HttpResponseException  if the call to the DKG API returns an error status code.
     * @throws UnexpectedException    if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> publish(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws ClientRequestException, HttpResponseException, UnexpectedException {
        return publishApiService.publish(fileName, fileData, publishOptions);
    }

    /**
     * Publish an Assertion on the DKG.
     *
     * @param filePath       A {@code String} containing the path to the assertion data to publish.
     *                       Must have file extension {@code .json}.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the published DKG Assertion.
     * @throws ClientRequestException if {@code fileName} does not have file extension '.json',
     *                                or {@code fileData} is not valid json, or {@code publishOptions} null.
     * @throws HttpResponseException  if the call to the DKG API returns an error status code.
     * @throws UnexpectedException    if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> publish(String filePath, PublishOptions publishOptions)
            throws ClientRequestException, HttpResponseException, UnexpectedException {
        try {
            Path publishFilePath = Paths.get(filePath);
            Path publishFileName = publishFilePath.getFileName();

            byte[] publishData = Files.readAllBytes(publishFilePath);

            return publishApiService.publish(publishFileName.toString(), publishData, publishOptions);
        } catch (IOException e) {
            throw new ClientRequestException(String.format("Exception reading publish file: %s", filePath), e.getCause());
        }
    }

    /**
     * Get the result of a previous publish request.
     *
     * @param handlerId The {@code handler_id} returned in the publish response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getPublishResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return publishApiService.getPublishResult(handlerId);
    }

    /**
     * Resolve assertions on the DKG
     *
     * @param assertionIds {@code List<String>} of assertion ids to resolve.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the resolved DKG assertions.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> resolve(List<String> assertionIds)
            throws HttpResponseException, UnexpectedException {
        return resolveApiService.resolve(assertionIds);
    }

    /**
     * Get the result of a previous resolve request.
     *
     * @param handlerId The {@code handler_id} returned in the resolve response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getResolveResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return resolveApiService.getResolveResult(handlerId);
    }

    /**
     * Search for entities on the DKG.
     *
     * @param entitySearchOptions {@link EntitySearchOptions} containing query parameters required for search.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG entities search.
     * @throws ClientRequestException if {@code EntitySearchOptions} is not valid. Either the {@code query} or {@code ids} parameter is required.
     * @throws HttpResponseException  if the call to the DKG API returns an error status code.
     * @throws UnexpectedException    if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions)
            throws HttpResponseException, UnexpectedException {
        return searchApiService.entitiesSearch(entitySearchOptions);
    }

    /**
     * Get the result of a previous entities search request.
     *
     * @param handlerId The {@code handler_id} returned in the entities search response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return searchApiService.getEntitiesSearchResult(handlerId);
    }

    /**
     * Search for assertions on the DKG.
     *
     * @param assertionSearchOptions {@link AssertionSearchOptions} containing query parameters required for search.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG assertions search.
     * @throws ClientRequestException if {@code AssertionSearchOptions} is not valid. The {@code query} parameter is required.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions)
            throws HttpResponseException, UnexpectedException {
        return searchApiService.assertionsSearch(assertionSearchOptions);
    }

    /**
     * Get the result of a previous assertions search request.
     *
     * @param handlerId The {@code handler_id} returned in the assertions search response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return searchApiService.getAssertionsSearchResult(handlerId);
    }

    /**
     * Run a SPARQL query on the local DKG node.
     *
     * @param type The {@code SparqlQueryType} of the SPARQL query.
     * @param sparqlQuery The SPARQL query as a {@code String}.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG SPARQL query.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> query(SparqlQueryType type, String sparqlQuery)
            throws HttpResponseException, UnexpectedException {
        return queryApiService.query(type, sparqlQuery);
    }

    /**
     * Run a SPARQL query on the local DKG node.
     *
     * @param type The {@code SparqlQueryType} of the SPARQL query.
     * @param sparqlQueryBuilder The Apache Jena {@code AbstractQueryBuilder} used to build a SPARQL query.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG SPARQL query.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> query(SparqlQueryType type, AbstractQueryBuilder<?> sparqlQueryBuilder)
            throws HttpResponseException, UnexpectedException {
        Query query = sparqlQueryBuilder.build();
        return queryApiService.query(type, query.toString());
    }

    /**
     * Get the result of a previous SPARQL query.
     *
     * @param handlerId The {@code handler_id} returned in the SPARQL query response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getQueryResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return queryApiService.getQueryResult(handlerId);
    }

    /**
     * Query proofs for RDF triples in n-quads format.
     *
     * @param nQuads {@code List<NQuad>} collection of RDF triples.
     * @param assertionIds the assertion ids to query.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG proofs query.
     * @throws ClientRequestException if {@code nQuads} cannot be serialized as a JSON array.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> proofs(List<NQuad> nQuads, List<String> assertionIds)
            throws HttpResponseException, UnexpectedException {
        return queryApiService.proofs(nQuads, assertionIds);
    }

    /**
     * Get the result of a previous proofs query.
     *
     * @param handlerId The {@code handler_id} returned in the proofs query response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws HttpResponseException if the call to the DKG API returns an error status code.
     * @throws UnexpectedException   if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getProofsResult(String handlerId)
            throws HttpResponseException, UnexpectedException {
        return queryApiService.getProofsResult(handlerId);
    }
}
