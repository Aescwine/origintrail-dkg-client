package io.origintrail.dkg.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.model.SparqlQueryType;
import io.origintrail.dkg.client.model.response.HandlerId;
import io.origintrail.dkg.client.model.response.NodeInfo;
import io.origintrail.dkg.client.model.response.ProofsResult;
import io.origintrail.dkg.client.model.response.PublishResult;
import io.origintrail.dkg.client.model.response.QueryResult;
import io.origintrail.dkg.client.model.response.ResolveResult;
import io.origintrail.dkg.client.service.ApiRequestService;
import io.origintrail.dkg.client.service.InfoService;
import io.origintrail.dkg.client.service.PublishService;
import io.origintrail.dkg.client.service.QueryService;
import io.origintrail.dkg.client.service.ResolveService;
import io.origintrail.dkg.client.service.SearchService;
import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The {@code DkgClient} provides a client interface for interacting with the OriginTrail Decentralized Knowledge Graph API.
 * See https://origintrail.io/ and https://app.swaggerhub.com/apis/TraceLabs/ot-node-v6/
 */
public class DkgClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DkgClient.class);

    private final InfoService infoService;
    private final PublishService publishService;
    private final ResolveService resolveService;
    private final SearchService searchService;
    private final QueryService queryService;

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8900;

    /**
     * Creates a DkgClient with default URL options: schema=http, HOST=localhost, PORT=8900.
     */
    public DkgClient() {
        this(DEFAULT_HOST, DEFAULT_PORT, false);
    }

    public DkgClient(String host, int port) {
        this(host, port, false);
    }

    public DkgClient(String host, int port, boolean sshEnabled) {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(host, port, sshEnabled ? "https" : "http");
        ApiRequestService apiRequestService = new ApiRequestService(httpClient, httpUrlOptions);

        infoService = new InfoService(apiRequestService);
        publishService = new PublishService(apiRequestService);
        resolveService = new ResolveService(apiRequestService);
        searchService = new SearchService(apiRequestService);
        queryService = new QueryService(apiRequestService);
    }

    /**
     * Get DKG node information.
     *
     * @return {@code NodeInfo} representing the response body containing node information.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<NodeInfo> getNodeInfo() throws CompletionException {
        return infoService.getNodeInfo();
    }

    /**
     * Initiates publishing of an Assertion on the DKG.
     *
     * @param fileName       The file name for the data being published. Must have file extension {@code .json}.
     * @param fileData       {@code byte[]} of the file data being published.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the published DKG Assertion.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json, or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> publishAssertion(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        return publishService.publish(fileName, fileData, publishOptions);
    }

    /**
     * Initiates publishing of an Assertion on the DKG.
     *
     * @param filePath       A {@code String} containing the path to the assertion data to publish.
     *                       Must have file extension {@code .json}.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the published DKG Assertion.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code filePath} data cannot be read,
     *                                    or {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json,
     *                                    or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> publishAssertion(String filePath, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        Path jsonFilePath = Paths.get(filePath);
        byte[] fileBytes = readFileBytes(jsonFilePath);

        return publishService.publish(jsonFilePath.getFileName().toString(), fileBytes, publishOptions);
    }

    private byte[] readFileBytes(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            LOGGER.error(String.format("Exception occurred reading data from file path: %s", filePath), e);
            throw new RequestValidationException(String.format("Exception reading file: %s", filePath), e.getCause());
        }
    }

    /**
     * Get the result of a previous publish request.
     *
     * @param handlerId The {@code handler_id} returned in the publish response you want to retrieve.
     * @return A {@code CompletableFuture<PublishResult>} containing a {@code PublishResult} representing the publish result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<PublishResult> getPublishAssertionResult(String handlerId)
            throws CompletionException {
        return publishService.getPublishResult(handlerId);
    }

    /**
     * Initiates provisioning of an asset on the DKG.
     *
     * @param fileName       The file name for the data being published. Must have file extension {@code .json}.
     * @param fileData       {@code byte[]} of the file data being provision.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the provisioned DKG Asset.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json, or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> provisionAsset(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        return publishService.provision(fileName, fileData, publishOptions);
    }

    /**
     * Initiates provisioning of an asset on the DKG.
     *
     * @param filePath       A {@code String} containing the path to the asset data to provision.
     *                       Must have file extension {@code .json}.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the provisioned DKG Asset.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code filePath} data cannot be read,
     *                                    or {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json,
     *                                    or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> provisionAsset(String filePath, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        Path jsonFilePath = Paths.get(filePath);
        Path fileName = jsonFilePath.getFileName();
        byte[] fileBytes = readFileBytes(fileName);

        return publishService.provision(fileName.toString(), fileBytes, publishOptions);
    }

    /**
     * Get the result of a previous provision request.
     *
     * @param handlerId The {@code handler_id} returned in the provision response you want to retrieve.
     * @return A {@code CompletableFuture<PublishResult>} containing a {@code PublishResult} representing the provision result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<PublishResult> getProvisionAssetResult(String handlerId)
            throws CompletionException {
        return publishService.getProvisionResult(handlerId);
    }

    /**
     * Provision an Asset on the DKG.
     *
     * @param fileName       The file name for the data being published. Must have file extension {@code .json}.
     * @param fileData       {@code byte[]} of the file data being provision.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the provisioned DKG Asset.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json, or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> updateAsset(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        return publishService.update(fileName, fileData, publishOptions);
    }

    /**
     * Provision an Asset on the DKG.
     *
     * @param filePath       A {@code String} containing the path to the asset data to provision.
     *                       Must have file extension {@code .json}.
     * @param publishOptions {@link PublishOptions} containing additional request properties.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the provisioned DKG Asset.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code filePath} data cannot be read,
     *                                    or {@code fileName} does not have file extension '.json',
     *                                    or {@code fileData} is not valid json,
     *                                    or {@code publishOptions} null.
     */
    public CompletableFuture<HandlerId> updateAsset(String filePath, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {
        Path jsonFilePath = Paths.get(filePath);
        Path fileName = jsonFilePath.getFileName();
        byte[] fileBytes = readFileBytes(fileName);

        return publishService.update(fileName.toString(), fileBytes, publishOptions);
    }

    /**
     * Get the result of a previous provision request.
     *
     * @param handlerId The {@code handler_id} returned in the provision response you want to retrieve.
     * @return A {@code CompletableFuture<PublishResult>} containing a {@code PublishResult} representing the provision result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<PublishResult> getUpdateAssetResult(String handlerId)
            throws CompletionException {
        return publishService.getUpdateResult(handlerId);
    }

    /**
     * Resolve assertions on the DKG
     *
     * @param assertionIds {@code List<String>} of assertion ids to resolve.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the resolved DKG assertions.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> resolve(List<String> assertionIds)
            throws CompletionException {
        return resolveService.resolve(assertionIds);
    }

    /**
     * Get the result of a previous resolve request.
     *
     * @param handlerId The {@code handler_id} returned in the resolve response you want to retrieve.
     * @return A {@code CompletableFuture<ResolveResult>} containing a {@code ResolveResult} representing the resolve result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<ResolveResult> getResolveResult(String handlerId)
            throws CompletionException {
        return resolveService.getResolveResult(handlerId);
    }

    /**
     * Search for entities on the DKG.
     *
     * @param entitySearchOptions {@link EntitySearchOptions} containing query parameters required for search.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG entities search.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code EntitySearchOptions} is not valid. Either the {@code query} or {@code ids} parameter is required.
     */
    public CompletableFuture<HandlerId> entitiesSearch(EntitySearchOptions entitySearchOptions)
            throws CompletionException, RequestValidationException {
        return searchService.entitiesSearch(entitySearchOptions);
    }

    /**
     * Get the result of a previous entities search request.
     *
     * @param handlerId The {@code handler_id} returned in the entities search response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getEntitiesSearchResult(String handlerId)
            throws CompletionException {
        return searchService.getEntitiesSearchResult(handlerId);
    }

    /**
     * Search for assertions on the DKG.
     *
     * @param assertionSearchOptions {@link AssertionSearchOptions} containing query parameters required for search.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG assertions search.
     * @throws CompletionException        if the call to the DKG API returns an error status code,
     *                                    or if the response body is not in the expected format,
     *                                    or if an unexpected exception occurs during processing of the request/response.
     * @throws RequestValidationException if {@code AssertionSearchOptions} is not valid. The {@code query} parameter is required.
     */
    public CompletableFuture<HandlerId> assertionsSearch(AssertionSearchOptions assertionSearchOptions)
            throws CompletionException, RequestValidationException {
        return searchService.assertionsSearch(assertionSearchOptions);
    }

    /**
     * Get the result of a previous assertions search request.
     *
     * @param handlerId The {@code handler_id} returned in the assertions search response you want to retrieve.
     * @return A {@code CompletableFuture<JsonNode>} containing a {@code JsonNode} representing the JSON response.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<JsonNode> getAssertionsSearchResult(String handlerId)
            throws CompletionException {
        return searchService.getAssertionsSearchResult(handlerId);
    }

    /**
     * Run a SPARQL query on the local DKG node.
     *
     * @param type        The {@code SparqlQueryType} of the SPARQL query.
     * @param sparqlQuery The SPARQL query as a {@code String}.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG SPARQL query.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> query(SparqlQueryType type, String sparqlQuery)
            throws CompletionException {
        return queryService.query(type, sparqlQuery);
    }

    /**
     * Run a SPARQL query on the local DKG node.
     *
     * @param type               The {@code SparqlQueryType} of the SPARQL query.
     * @param sparqlQueryBuilder The Apache Jena {@code AbstractQueryBuilder} used to build a SPARQL query.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG SPARQL query.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> query(SparqlQueryType type, AbstractQueryBuilder<?> sparqlQueryBuilder)
            throws CompletionException {
        Query query = sparqlQueryBuilder.build();
        return queryService.query(type, query.toString());
    }

    /**
     * Get the result of a previous SPARQL query.
     *
     * @param handlerId The {@code handler_id} returned in the SPARQL query response you want to retrieve.
     * @return A {@code CompletableFuture<QueryResult>} containing a {@code QueryResult} representing the query result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<QueryResult> getQueryResult(String handlerId)
            throws CompletionException {
        return queryService.getQueryResult(handlerId);
    }

    /**
     * Query proofs for RDF triples in n-quads format.
     *
     * @param nQuads       {@code List<NQuad>} collection of RDF triples.
     * @param assertionIds the assertion ids to query.
     * @return A {@code CompletableFuture<HandlerId>} containing the {@link HandlerId} for the DKG proofs query.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<HandlerId> proofs(List<NQuad> nQuads, List<String> assertionIds)
            throws CompletionException {
        return queryService.proofs(nQuads, assertionIds);
    }

    /**
     * Get the result of a previous proofs query.
     *
     * @param handlerId The {@code handler_id} returned in the proofs query response you want to retrieve.
     * @return A {@code CompletableFuture<ProofsResult>} containing a {@code ProofsResult} representing the proofs result.
     * @throws CompletionException if the call to the DKG API returns an error status code,
     *                             or if the response body is not in the expected format,
     *                             or if an unexpected exception occurs during processing of the request/response.
     */
    public CompletableFuture<ProofsResult> getProofsResult(String handlerId)
            throws CompletionException {
        return queryService.getProofsResult(handlerId);
    }
}
