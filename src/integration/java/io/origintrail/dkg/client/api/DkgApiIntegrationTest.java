package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import org.apache.commons.io.FileUtils;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

public class DkgApiIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DkgApiIntegrationTest.class);

    private static final String HOST = "localhost"; // your local node instance
    private static final int PORT = 8900;

    private final DkgClient dkgClient = new DkgClient(HOST, PORT);

    @Test
    void info_getNodeInformation_returnsNodeInfo() {
        // given

        // when
        CompletableFuture<NodeInfo> result = dkgClient.getInfo();

        // then
        NodeInfo info = result.join();
        assertThat(info).isNotNull();
    }

    @Test
    void end_to_end_integration_test() throws IOException, InterruptedException {
        String randomKeywordForTest = "keyword" + UUID.randomUUID().toString().substring(0, 5);
        String randomAssetForTest = "asset" + UUID.randomUUID().toString().substring(0, 5);

        // publish assertion
        HandlerId publishHandlerId = publishAssertion(randomKeywordForTest, randomAssetForTest);

        assertThat(publishHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
        LOGGER.info("Assertion published");

        // sleep for 5 seconds to allow publish to complete
        Thread.sleep(5000);

        // get publish result
        JsonNode publishedAssertionJson = getPublishResult(publishHandlerId);
        assertThat(publishedAssertionJson.get("status").toString()).containsAnyOf("COMPLETED", "PENDING");
        String assertionId = publishedAssertionJson.path("data").path("id").asText();
        LOGGER.info("Assertion received");


        // resolve assertion
        HandlerId resolveHandlerId = resolveAssertion(assertionId);
        assertThat(resolveHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
        LOGGER.info("Assertion resolved");

        // sleep for 3 seconds to allow resolve to complete
        Thread.sleep(3000);

        // get resolve result
        JsonNode resolvedAssertionJson = getResolveResult(resolveHandlerId);
        assertThat(resolvedAssertionJson.get("status").toString()).containsAnyOf("COMPLETED", "PENDING");
        LOGGER.info("Assertion resolve result received");


        // entities search
        HandlerId entitiesSearchHandlerId = entitiesSearch(randomKeywordForTest);
        assertThat(entitiesSearchHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
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
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
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
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
        LOGGER.info("SPARQL query sent");

        // sleep for 1 second to allow query to complete
        Thread.sleep(1000);

        // SPARQL query result
        JsonNode sparqlQueryResult = sparqlQueryResult(sparqlQueryHandlerId);
        assertThat(sparqlQueryResult.get("status").toString()).containsAnyOf("COMPLETED");
        assertThat(sparqlQueryResult.has("data")).isTrue();
        assertThat(sparqlQueryResult.get("data").size()).isEqualTo(3);
        LOGGER.info("SPARQL query result received");


        // get proofs
        HandlerId proofsQueryHandlerId = proofsQuery(randomKeywordForTest, assertionId);
        assertThat(assertionSearchHandlerId.getHandlerId()).matches(v -> v.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
        LOGGER.info("Proofs query sent");

        // sleep for 5 second to allow proofs query to complete
        Thread.sleep(5000);

        JsonNode proofsQueryResult = proofsQueryResult(proofsQueryHandlerId);
        assertThat(proofsQueryResult.get("status").toString()).containsAnyOf("COMPLETED");
        LOGGER.info("Proofs query result received");
    }

    private HandlerId publishAssertion(String testKeyword, String randomAssetForTest) throws IOException {
        String assertionFileName = "assertion-example.json";
        ClassLoader classLoader = DkgApiIntegrationTest.class.getClassLoader();

        URL assertionFileUrl = classLoader.getResource(assertionFileName);
        if (assertionFileUrl == null ){
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
        CompletableFuture<HandlerId> resolveHandlerIdCompletableFuture = dkgClient.resolve(assertionId);
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
        return  entitiesSearchResult.join();
    }

    private HandlerId assertionsSearch(String keyword) {
        AssertionSearchOptions assertionSearchOptions = AssertionSearchOptions.builder().query(keyword).build();
        CompletableFuture<HandlerId> result = dkgClient.assertionsSearch(assertionSearchOptions);
        return result.join();
    }

    private JsonNode getAssertionsSearchResult(HandlerId assertionSearchHandlerId) {
        CompletableFuture<JsonNode> assertionSearchResult = dkgClient.getAssertionsSearchResult(assertionSearchHandlerId.getHandlerId());
        return  assertionSearchResult.join();
    }

    private HandlerId sparqlQuery(String keyword) {
        ConstructBuilder constructBuilder = new ConstructBuilder()
                .addPrefix("schema", "http://schema.org/")
                .addConstruct("?s", "schema:hasKeyword", "?o")
                .addGraph("?g", "?s", "schema:hasKeyword", "?o")
                .addWhere("?s", "schema:hasKeyword", keyword);

        CompletableFuture<HandlerId> queryHandlerId = dkgClient.query("construct", constructBuilder);
        return queryHandlerId.join();
    }

    private JsonNode sparqlQueryResult(HandlerId sparqlQueryHandlerId) {
        CompletableFuture<JsonNode> result = dkgClient.getQueryResult(sparqlQueryHandlerId.getHandlerId());
        return result.join();
    }

    private HandlerId proofsQuery(String randomKeywordForTest, String assertionId) {
        String nquadsData = "[\"<did:dkg:" + assertionId + "> <http://schema.org/hasKeyword> \\"+ randomKeywordForTest + "\" .\"]";

        CompletableFuture<HandlerId> result = dkgClient.proofs(nquadsData, assertionId);
        return result.join();
    }

    private JsonNode proofsQueryResult(HandlerId proofsHandlerId) {
        CompletableFuture<JsonNode> proofsResult = dkgClient.getProofsResult(proofsHandlerId.getHandlerId());
        return proofsResult.join();
    }
}
