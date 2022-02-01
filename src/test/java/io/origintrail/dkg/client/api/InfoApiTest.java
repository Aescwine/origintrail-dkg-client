package io.origintrail.dkg.client.api;

import io.origintrail.dkg.client.model.NodeInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class InfoApiTest {
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
        CompletableFuture<NodeInfo> nodeInfoCompletableFuture = dkgClient.getInfo();
        NodeInfo nodeInfo = nodeInfoCompletableFuture.join();

        // then
        assertThat(nodeInfo.getVersion()).isEqualTo("6.0.0-beta.1.20");
        assertThat(nodeInfo.isAutoUpdate()).isFalse();
        assertThat(nodeInfo.isTelemetry()).isTrue();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/info");
    }
}