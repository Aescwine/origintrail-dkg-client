package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.DkgClient;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.model.SparqlQueryType;
import org.apache.commons.io.FileUtils;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for testing the client integration a running DKG node
 */
public class DkgClientIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DkgClientIntegrationTest.class);

    private static final String HOST = "172.17.119.10"; // your local node instance
    private static final int PORT = 8900;

    private static final String HANDLER_ID_REGEX = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    private static final String COMPLETED = "COMPLETED";

    private final DkgClient dkgClient = new DkgClient(HOST, PORT);

    @Test
    void info_getNodeInformation_returnsNodeInfo() {
        // given

        // when
        CompletableFuture<NodeInfo> result = dkgClient.getNodeInfo();

        // then
        NodeInfo info = result.join();
        assertThat(info).isNotNull();
    }

    @Test
    void proofs_multipleAssertions_resultContainsDataForTwoAssertions() throws IOException, InterruptedException {
        String randomKeywordForTest = "keyword" + UUID.randomUUID().toString().substring(0, 5);
        String randomAssetForTest = "asset" + UUID.randomUUID().toString().substring(0, 5);

        // publish assertions
        HandlerId publishHandlerId1 = publishAssertion(randomKeywordForTest, randomAssetForTest);
        assertThat(publishHandlerId1.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));

        // sleep for 5 seconds to allow publish to complete
        Thread.sleep(5000);

        HandlerId publishHandlerId2 = publishAssertion(randomKeywordForTest, randomAssetForTest);
        assertThat(publishHandlerId2.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));

        // sleep for 10 seconds to allow publish to complete
        Thread.sleep(10000);

        // get publish results
        JsonNode publishedAssertionJson1 = getPublishResult(publishHandlerId1);
        assertThat(publishedAssertionJson1.get("status").asText()).isEqualTo(COMPLETED);
        String assertionId1 = publishedAssertionJson1.path("data").path("id").asText();

        JsonNode publishedAssertionJson2 = getPublishResult(publishHandlerId2);
        assertThat(publishedAssertionJson2.get("status").asText()).isEqualTo(COMPLETED);
        String assertionId2 = publishedAssertionJson2.path("data").path("id").asText();


        // get proofs
        HandlerId proofsQueryHandlerId = proofsQuery(randomKeywordForTest, Arrays.asList(assertionId1, assertionId2));
        assertThat(proofsQueryHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Proofs query sent");

        // sleep for 5 second to allow proofs query to complete
        Thread.sleep(5000);

        // get proofs result - should contain proofs for two assertions
        JsonNode proofsQueryResult = proofsQueryResult(proofsQueryHandlerId);
        assertThat(proofsQueryResult.get("status").asText()).isEqualTo(COMPLETED);
        assertThat(proofsQueryResult.get("data").size()).isEqualTo(2);
    }

    @Test
    void resolve_multipleAssertions_resultContainsDataForTwoAssertions() throws IOException, InterruptedException {
        String randomKeywordForTest = "keyword" + UUID.randomUUID().toString().substring(0, 5);
        String randomAssetForTest = "asset" + UUID.randomUUID().toString().substring(0, 5);

        // publish assertions
        HandlerId publishHandlerId1 = publishAssertion(randomKeywordForTest, randomAssetForTest);
        assertThat(publishHandlerId1.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));

        // sleep for 5 seconds to allow publish to complete
        Thread.sleep(5000);

        HandlerId publishHandlerId2 = publishAssertion(randomKeywordForTest, randomAssetForTest);
        assertThat(publishHandlerId2.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));

        // sleep for 10 seconds to allow publish to complete
        Thread.sleep(10000);

        // get publish results
        JsonNode publishedAssertionJson1 = getPublishResult(publishHandlerId1);
        assertThat(publishedAssertionJson1.get("status").asText()).isEqualTo(COMPLETED);
        String assertionId1 = publishedAssertionJson1.path("data").path("id").asText();

        JsonNode publishedAssertionJson2 = getPublishResult(publishHandlerId2);
        assertThat(publishedAssertionJson2.get("status").asText()).isEqualTo(COMPLETED);
        String assertionId2 = publishedAssertionJson2.path("data").path("id").asText();


        // resolve assertions
        HandlerId resolveHandlerId = resolveAssertions(Arrays.asList(assertionId1, assertionId2));
        assertThat(resolveHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));

        // sleep for 3 seconds to allow resolve to complete
        Thread.sleep(3000);

        // get resolve result - should contain two resolved assertions
        JsonNode resolvedAssertionJson = getResolveResult(resolveHandlerId);
        assertThat(resolvedAssertionJson.get("status").asText()).isEqualTo(COMPLETED);
        assertThat(resolvedAssertionJson.get("data").size()).isEqualTo(2);
    }

    @Test
    void end_to_end_integration_test() throws IOException, InterruptedException {
        String randomKeywordForTest = "keyword" + UUID.randomUUID().toString().substring(0, 5);
        String randomAssetForTest = "asset" + UUID.randomUUID().toString().substring(0, 5);

        // publish assertion
        HandlerId publishHandlerId = publishAssertion(randomKeywordForTest, randomAssetForTest);

        assertThat(publishHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Assertion published");

        // sleep for 10 seconds to allow publish to complete
        Thread.sleep(10000);

        // get publish result
        JsonNode publishedAssertionJson = getPublishResult(publishHandlerId);
        assertThat(publishedAssertionJson.get("status").asText()).isEqualTo(COMPLETED);
        String assertionId = publishedAssertionJson.path("data").path("id").asText();
        LOGGER.info("Assertion received");


        // resolve assertion
        HandlerId resolveHandlerId = resolveAssertion(assertionId);
        assertThat(resolveHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Assertion resolved");

        // sleep for 3 seconds to allow resolve to complete
        Thread.sleep(3000);

        // get resolve result
        JsonNode resolvedAssertionJson = getResolveResult(resolveHandlerId);
        assertThat(resolvedAssertionJson.get("status").asText()).isEqualTo(COMPLETED);
        LOGGER.info("Assertion resolve result received");


        // entities search
        HandlerId entitiesSearchHandlerId = entitiesSearch(randomKeywordForTest);
        assertThat(entitiesSearchHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Entity search sent");

        // sleep for 3 seconds to allow entities search to complete
        Thread.sleep(3000);

        // entities search result
        JsonNode entitiesSearchResult = getEntitiesSearchResult(entitiesSearchHandlerId);
        assertThat(entitiesSearchResult.has("itemListElement")).isTrue();
        assertThat(entitiesSearchResult.get("itemListElement").isArray()).isTrue();
        assertThat(entitiesSearchResult.get("itemListElement").size()).isEqualTo(1);
        LOGGER.info("Entity search result received");


        // assertion search
        HandlerId assertionSearchHandlerId = assertionsSearch(randomAssetForTest);
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Assertion search sent");

        // sleep for 3 seconds to allow assertion search to complete
        Thread.sleep(3000);

        // assertion search result
        JsonNode assertionSearchResult = getAssertionsSearchResult(assertionSearchHandlerId);
        assertThat(assertionSearchResult.has("itemListElement")).isTrue();
        assertThat(assertionSearchResult.get("itemListElement").isArray()).isTrue();
        assertThat(assertionSearchResult.get("itemListElement").size()).isEqualTo(1);
        LOGGER.info("Assertion search result received");


        // SPARQL query
        HandlerId sparqlQueryHandlerId = sparqlQuery(randomKeywordForTest);
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("SPARQL query sent");

        // sleep for 1 second to allow query to complete
        Thread.sleep(1000);

        // SPARQL query result
        JsonNode sparqlQueryResult = sparqlQueryResult(sparqlQueryHandlerId);
        assertThat(sparqlQueryResult.get("status").asText()).isEqualTo(COMPLETED);
        assertThat(sparqlQueryResult.has("data")).isTrue();
        assertThat(sparqlQueryResult.get("data").size()).isEqualTo(3);
        LOGGER.info("SPARQL query result received");


        // get proofs
        HandlerId proofsQueryHandlerId = proofsQuery(randomKeywordForTest, Collections.singletonList(assertionId));
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches(HANDLER_ID_REGEX));
        LOGGER.info("Proofs query sent");

        // sleep for 5 second to allow proofs query to complete
        Thread.sleep(5000);

        JsonNode proofsQueryResult = proofsQueryResult(proofsQueryHandlerId);
        assertThat(proofsQueryResult.get("status").asText()).isEqualTo(COMPLETED);
        LOGGER.info("Proofs query result received");
    }

    private HandlerId publishAssertion(String testKeyword, String randomAssetForTest) throws IOException {
        String assertionFileName = "assertion-example.json";
        ClassLoader classLoader = DkgClientIntegrationTest.class.getClassLoader();

        URL assertionFileUrl = classLoader.getResource(assertionFileName);
        if (assertionFileUrl == null) {
            throw new IllegalArgumentException("File not found: " + assertionFileName);
        }

        File file = new File(assertionFileUrl.getFile());
        byte[] fileData = FileUtils.readFileToByteArray(file);

        PublishOptions publishOptions = PublishOptions
                .builder("[\"" + randomAssetForTest + "\"]")
                .keywords("[\"" + testKeyword + "\"]").build();

        CompletableFuture<HandlerId> publishHandlerIdCompletableFuture = dkgClient.publish(assertionFileName, fileData, publishOptions);
        return publishHandlerIdCompletableFuture.join();
    }

    private JsonNode getPublishResult(HandlerId publishHandlerId) {
        CompletableFuture<JsonNode> publishResult = dkgClient.getPublishResult(publishHandlerId.getHandlerId());
        return publishResult.join();
    }

    private HandlerId resolveAssertion(String assertionId) {
        CompletableFuture<HandlerId> resolveHandlerIdCompletableFuture = dkgClient.resolve(Collections.singletonList(assertionId));
        return resolveHandlerIdCompletableFuture.join();
    }

    private HandlerId resolveAssertions(List<String> assertionIds) {
        CompletableFuture<HandlerId> resolveHandlerIdCompletableFuture = dkgClient.resolve(assertionIds);
        return resolveHandlerIdCompletableFuture.join();
    }

    private JsonNode getResolveResult(HandlerId resolveHandlerId) {
        CompletableFuture<JsonNode> resolveResult = dkgClient.getResolveResult(resolveHandlerId.getHandlerId());
        return resolveResult.join();
    }

    private HandlerId entitiesSearch(String keyword) {
        EntitySearchOptions entitySearchOptions = EntitySearchOptions.builder().query(keyword).build();
        CompletableFuture<HandlerId> result = dkgClient.entitiesSearch(entitySearchOptions);
        return result.join();
    }

    private JsonNode getEntitiesSearchResult(HandlerId entitiesSearchHandlerId) {
        CompletableFuture<JsonNode> entitiesSearchResult = dkgClient.getEntitiesSearchResult(entitiesSearchHandlerId.getHandlerId());
        return entitiesSearchResult.join();
    }

    private HandlerId assertionsSearch(String keyword) {
        AssertionSearchOptions assertionSearchOptions = AssertionSearchOptions.builder(keyword).build();
        CompletableFuture<HandlerId> result = dkgClient.assertionsSearch(assertionSearchOptions);
        return result.join();
    }

    private JsonNode getAssertionsSearchResult(HandlerId assertionSearchHandlerId) {
        CompletableFuture<JsonNode> assertionSearchResult = dkgClient.getAssertionsSearchResult(assertionSearchHandlerId.getHandlerId());
        return assertionSearchResult.join();
    }

    private HandlerId sparqlQuery(String keyword) {
        ConstructBuilder constructBuilder = new ConstructBuilder()
                .addPrefix("schema", "http://schema.org/")
                .addConstruct("?s", "schema:hasKeyword", "?o")
                .addGraph("?g", "?s", "schema:hasKeyword", "?o")
                .addWhere("?s", "schema:hasKeyword", keyword);

        CompletableFuture<HandlerId> queryHandlerId = dkgClient.query(SparqlQueryType.CONSTRUCT, constructBuilder);
        return queryHandlerId.join();
    }

    private JsonNode sparqlQueryResult(HandlerId sparqlQueryHandlerId) {
        CompletableFuture<JsonNode> result = dkgClient.getQueryResult(sparqlQueryHandlerId.getHandlerId());
        return result.join();
    }

    private HandlerId proofsQuery(String randomKeywordForTest, List<String> assertionIds) {
        List<NQuad> nQuads = assertionIds.stream()
                .map(s -> NQuad.builder("<did:dkg:" + s + ">", "<http://schema.org/hasKeyword>", randomKeywordForTest).build())
                .collect(Collectors.toList());

        CompletableFuture<HandlerId> result = dkgClient.proofs(nQuads, assertionIds);
        return result.join();
    }

    private JsonNode proofsQueryResult(HandlerId proofsHandlerId) {
        CompletableFuture<JsonNode> proofsResult = dkgClient.getProofsResult(proofsHandlerId.getHandlerId());
        return proofsResult.join();
    }
}