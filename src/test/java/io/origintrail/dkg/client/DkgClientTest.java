package io.origintrail.dkg.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.exception.ResponseBodyException;
import io.origintrail.dkg.client.model.AssertionSearchOptions;
import io.origintrail.dkg.client.model.EntitySearchOptions;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.NQuad;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.model.SparqlQueryType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.FileUtils;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@ExtendWith(MockitoExtension.class)
class DkgClientTest {

    private static final String HANDLER_ID = "ffd8a00e-bf22-4432-8d88-804f4f9baa27";
    private static final String PUBLISH_ASSERTION_FILE_NAME = "assertion-example.json";
    private static final PublishOptions PUBLISH_OPTIONS = PublishOptions
            .builder("[\"test_asset\"]")
            .keywords("[\"test_keyword\"]").build();
    private final byte[] publishFileData = getFileData(PUBLISH_ASSERTION_FILE_NAME);

    private DkgClient dkgClient;
    private MockWebServer mockWebServer;

    DkgClientTest() throws IOException {
    }

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();
        dkgClient = new DkgClient(mockWebServer.getHostName(), mockWebServer.getPort());
    }

    @Test
    void getNodeInfo_retrievesNodeInfo_returnsNodeInformation() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"version\": \"6.0.0-beta.1.20\",\n" +
                "    \"auto_update\": false,\n" +
                "    \"telemetry\": true\n" +
                "}").setResponseCode(200));

        // when
        CompletableFuture<NodeInfo> nodeInfoCompletableFuture = dkgClient.getNodeInfo();
        NodeInfo nodeInfo = nodeInfoCompletableFuture.join();

        // then
        assertThat(nodeInfo).isNotNull();
        assertThat(nodeInfo.getVersion()).isEqualTo("6.0.0-beta.1.20");
        assertThat(nodeInfo.isAutoUpdate()).isFalse();
        assertThat(nodeInfo.isTelemetry()).isTrue();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/info");
    }

    @Test
    void getNodeInfo_responseCannotBeSerialized_throwCompletionException() {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"version\": \"6.0.0-beta.1.20\",\n" +
                "    \"auto_update\": false,\n" +
                "    \"invalid_field\": true\n" +
                "}").setResponseCode(200));

        // when
        CompletionException throwable = catchThrowableOfType(() -> dkgClient.getNodeInfo().join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof ResponseBodyException).isTrue();
        assertThat(throwable.getCause().getMessage()).isEqualTo("Exception parsing response body content.");
    }

    @Test
    void publish_fromFileData_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.publish(PUBLISH_ASSERTION_FILE_NAME, publishFileData, PUBLISH_OPTIONS);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/publish");
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data;");
        assertThat(request.getBody().readUtf8()).contains(
                "Content-Disposition: form-data; name=file",
                "Content-Disposition: form-data; name=assets\r\n\r\n[\"test_asset\"]",
                "Content-Disposition: form-data; name=keywords\r\n\r\n[\"test_keyword\"]",
                "Content-Disposition: form-data; name=visibility\r\n\r\ntrue");
    }

    private byte[] getFileData(String fileName) throws IOException {
        ClassLoader classLoader = DkgClientTest.class.getClassLoader();
        URL assertionFileUrl = classLoader.getResource(fileName);
        if (assertionFileUrl == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        File file = new File(assertionFileUrl.getFile());
        return FileUtils.readFileToByteArray(file);
    }

    @Test
    void publish_withNoKeywordsFormDataPart_returnsHandlerId() throws InterruptedException {
        // given
       mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        // when
        PublishOptions publishOptions = PublishOptions
                .builder("[\"test_asset\"]").build();

        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.publish(PUBLISH_ASSERTION_FILE_NAME, publishFileData, publishOptions);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/publish");
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data;");
        assertThat(request.getBody().readUtf8()).doesNotContain(
                "Content-Disposition: form-data; name=keywords");
    }

    @Test
    void publish_fromFilePath_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.publish("src/test/resources/assertion-example.json", PUBLISH_OPTIONS);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/publish");
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data;");
        assertThat(request.getBody().readUtf8()).contains(
                "Content-Disposition: form-data; name=file",
                "Content-Disposition: form-data; name=assets\r\n\r\n[\"test_asset\"]",
                "Content-Disposition: form-data; name=keywords\r\n\r\n[\"test_keyword\"]",
                "Content-Disposition: form-data; name=visibility\r\n\r\ntrue");
    }

    @Test
    void publish_fileDataCannotBeRead_returnsRequestValidationException() {
        // when
        RequestValidationException throwable = catchThrowableOfType(() -> dkgClient.publish("src/test/resources/invalid-path.json", PUBLISH_OPTIONS).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Exception reading publish file: src/test/resources/invalid-path.json");
    }

    @Test
    void getPublishResult_byHandlerId_returnsAssertionJsonNode() throws IOException, InterruptedException {
        // given
        String publishResponse = "example-responses/publish-response-body.json";
        byte[] fileData = getFileData(publishResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getPublishResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("data").get("id").textValue()).isEqualTo("797e375dd9c38b05f96803acf5666538139310346aeb8f162abe4e0d4d0dff99");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/publish/result/" + HANDLER_ID);
    }

    @Test
    void resolve_resolveAssertionByAssertionIds_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        String assertionId = "797e375dd9c38b05f96803acf5666538139310346aeb8f162abe4e0d4d0dff99";

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.resolve(Collections.singletonList(assertionId));
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/resolve?ids=" + assertionId);
    }

    @Test
    void getResolveResult_byHandlerId_returnsResolvedAssertionJsonNode() throws IOException, InterruptedException {
        // given
        String resolveResponse = "example-responses/resolve-response-body.json";
        byte[] fileData = getFileData(resolveResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getResolveResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("data").get(0).fieldNames().next()).isEqualTo("74001d8da754467ebd785d12e8aed495cf7dec91d4006eff3daf164dc0be4bec");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/resolve/result/" + HANDLER_ID);
    }

    @Test
    void entitiesSearch_searchEntitiesByQuery_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        String query = "aKeyword";
        EntitySearchOptions entitySearchOptions = EntitySearchOptions.builder().query(query).build();

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.entitiesSearch(entitySearchOptions);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/entities:search?query=" + query);
    }

    @Test
    void getEntitiesSearchResult_byHandlerId_returnsEntitySearchResultJsonNode() throws InterruptedException, IOException {
        // given
        String entitiesResponse = "example-responses/entities-search-response-body.json";
        byte[] fileData = getFileData(entitiesResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getEntitiesSearchResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("itemListElement").size()).isEqualTo(1);
        assertThat(jsonNode.get("itemListElement").get(0).get("@type").textValue()).isEqualTo("EntitySearchResult");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/entities:search/result/" + HANDLER_ID);
    }

    @Test
    void assertionsSearch_searchAssertionsByQuery_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        String query = "aKeyword";
        AssertionSearchOptions assertionSearchOptions = AssertionSearchOptions.builder(query).build();

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.assertionsSearch(assertionSearchOptions);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/assertions:search?query=" + query);
    }

    @Test
    void getAssertionsSearchResult_byHandlerId_returnsAssertionsSearchResultJsonNode() throws InterruptedException, IOException {
        // given
        String assertionsResponse = "example-responses/assertions-search-response-body.json";
        byte[] fileData = getFileData(assertionsResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getAssertionsSearchResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("itemListElement").size()).isEqualTo(6);
        assertThat(jsonNode.get("itemListElement").get(0).get("@type").textValue()).isEqualTo("AssertionSearchResult");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/assertions:search/result/" + HANDLER_ID);
    }

    @Test
    void query_queryBySparqlString_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        String query = "{\"query\": \"PREFIX  schema: <http://schema.org/> CONSTRUCT { ?s schema:hasKeyword ?o . } "
                + "WHERE { GRAPH ?g { ?s  schema:hasKeyword  ?o } ?s  schema:hasKeyword  \\\"aKeyword\\\"}}";

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.query(SparqlQueryType.CONSTRUCT, query);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/query?type=construct");
    }

    @Test
    void query_queryBySparqlConstructBuilder_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        ConstructBuilder constructBuilder = new ConstructBuilder()
                .addPrefix("schema", "http://schema.org/")
                .addConstruct("?s", "schema:hasKeyword", "?o")
                .addGraph("?g", "?s", "schema:hasKeyword", "?o")
                .addWhere("?s", "schema:hasKeyword ", "aKeyword");

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.query(SparqlQueryType.CONSTRUCT, constructBuilder);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/query?type=construct");
    }

    @Test
    void getQueryResult_byHandlerId_returnsQueryResultJsonNode() throws InterruptedException, IOException {
        // given
        String queryResponse = "example-responses/query-response-body.json";
        byte[] fileData = getFileData(queryResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getQueryResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("status").textValue()).isEqualTo("COMPLETED");
        assertThat(jsonNode.get("data").size()).isEqualTo(18);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/query/result/" + HANDLER_ID);
    }

    @Test
    void proofs_getProofsForTriples_returnsHandlerId() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + HANDLER_ID + "\"\n" +
                "}").setResponseCode(200));

        String assertionId = "5285f5da78edeaa9a7cb853ebd85121fae7d93191490d182490464668f2d21cc";
        List<NQuad> nQuads = Collections.singletonList(NQuad.builder("<did:dkg:"+ assertionId + ">", "<http://schema.org/hasKeyword>", "aKeyword").build());

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.proofs(nQuads, Collections.singletonList(assertionId));
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(HANDLER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/proofs:get?assertions=" + assertionId);
    }

    @Test
    void getProofsResult_byHandlerId_returnsProofsResultJsonNode() throws InterruptedException, IOException {
        // given
        String proofsResponse = "example-responses/proofs-response-body.json";
        byte[] fileData = getFileData(proofsResponse);
        mockWebServer.enqueue(new MockResponse().setBody(new String(fileData)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> jsonNodeCompletableFuture = dkgClient.getProofsResult(HANDLER_ID);
        JsonNode jsonNode = jsonNodeCompletableFuture.join();

        // then
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.get("status").textValue()).isEqualTo("COMPLETED");
        assertThat(jsonNode.get("data").size()).isEqualTo(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/proofs:get/result/" + HANDLER_ID);
    }
}