package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.ResponseBodyException;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.NodeInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class InfoServiceTest {

    private InfoService infoService;
    private MockWebServer mockWebServer;

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();

        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(mockWebServer.getHostName(), mockWebServer.getPort(), "http");
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        infoService = new InfoService(httpClient, httpUrlOptions);
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
        CompletableFuture<NodeInfo> nodeInfoCompletableFuture = infoService.getInfo();
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
    void getInfo_invalidResponseBody_throwsUnexpectedException()  {
        // given
        String responseBody = "{ \"error\" : \"Server error response\"}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(200));

        // when
        CompletionException throwable = catchThrowableOfType(() -> infoService.getInfo().join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof ResponseBodyException).isTrue();
        assertThat(((throwable.getCause()).getMessage())).isEqualTo("Exception parsing response body content.");
    }
}