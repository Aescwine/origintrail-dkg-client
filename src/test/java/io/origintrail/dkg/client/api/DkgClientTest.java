package io.origintrail.dkg.client.api;

import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.NodeInfo;
import io.origintrail.dkg.client.model.PublishOptions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class DkgClientTest {
    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8700;

    private DkgClient dkgClient = new DkgClient(HOST, PORT);
    private MockWebServer mockWebServer;

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();
        dkgClient = new DkgClient(mockWebServer.getHostName(), mockWebServer.getPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getInfo_responseIsSuccessful_returnsNodeInfo() throws InterruptedException {
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
        assertThat(nodeInfo.getVersion()).isEqualTo("6.0.0-beta.1.20");
        assertThat(nodeInfo.isAutoUpdate()).isFalse();
        assertThat(nodeInfo.isTelemetry()).isTrue();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/info");
    }

    @Test
    void publish_publishAssertionFromFilePath_returnsHandlerId() throws InterruptedException {
        // given
        String expectedHandlerId = "ffd8a00e-bf22-4432-8d88-804f4f9baa27";
        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \""+ expectedHandlerId + "\"\n" +
                "}").setResponseCode(200));

        String assertionFilePath = "src/test/resources/assertion-example.json";

        PublishOptions publishOptions = PublishOptions.builder("test_asset").build();

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = dkgClient.publish(assertionFilePath, publishOptions);
        HandlerId nodeInfo = handlerIdCompletableFuture.join();

        // then
        assertThat(nodeInfo.getHandlerId()).isEqualTo(expectedHandlerId);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/publish");
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data;");
    }
}