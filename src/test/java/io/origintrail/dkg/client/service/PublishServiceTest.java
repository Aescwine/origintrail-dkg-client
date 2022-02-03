package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.ClientRequestException;
import io.origintrail.dkg.client.exception.ResponseBodyException;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.PublishOptions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PublishServiceTest {

    private PublishService publishService;
    private MockWebServer mockWebServer;

    private final String assertionFileName = "assertion-example.json";
    private final byte[] fileData = getFileData(assertionFileName);
    private final PublishOptions publishOptions = PublishOptions
            .builder("[\"test_asset\"]")
            .keywords("[\"test_keyword\"]").build();

    PublishServiceTest() throws IOException {
    }

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();

        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(mockWebServer.getHostName(), mockWebServer.getPort(), "http");
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        publishService = new PublishService(httpClient, httpUrlOptions);
    }

    @Test
    void publish_withAllFormDataParts_returnsHandlerId() throws InterruptedException {
        // given
        String expectedHandlerId = "ffd8a00e-bf22-4432-8d88-804f4f9baa27";

        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + expectedHandlerId + "\"\n" +
                "}").setResponseCode(200));

        // when
        CompletableFuture<HandlerId> handlerIdCompletableFuture = publishService.publish(assertionFileName, fileData, publishOptions);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(expectedHandlerId);

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
    void publish_withNoKeywordsFormDataPart_returnsHandlerId() throws InterruptedException {
        // given
        String expectedHandlerId = "ffd8a00e-bf22-4432-8d88-804f4f9baa27";

        mockWebServer.enqueue(new MockResponse().setBody("{\n" +
                "    \"handler_id\": \"" + expectedHandlerId + "\"\n" +
                "}").setResponseCode(200));

        // when
        PublishOptions publishOptions = PublishOptions
                .builder("[\"test_asset\"]").build();

        CompletableFuture<HandlerId> handlerIdCompletableFuture = publishService.publish(assertionFileName, fileData, publishOptions);
        HandlerId handlerId = handlerIdCompletableFuture.join();

        // then
        assertThat(handlerId).isNotNull();
        assertThat(handlerId.getHandlerId()).isEqualTo(expectedHandlerId);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/publish");
        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data;");
        assertThat(request.getBody().readUtf8()).doesNotContain(
                "Content-Disposition: form-data; name=keywords");
    }

    private byte[] getFileData(String fileName) throws IOException {
        ClassLoader classLoader = PublishServiceTest.class.getClassLoader();
        URL assertionFileUrl = classLoader.getResource(fileName);
        if (assertionFileUrl == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        File file = new File(assertionFileUrl.getFile());
        return FileUtils.readFileToByteArray(file);
    }

    @Test
    void publish_invalidResponseBody_throwsUnexpectedException() {
        // given
        String responseBody = "{ \"error\" : \"Server error response\"}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(200));

        // when
        CompletionException throwable = catchThrowableOfType(() -> publishService.publish(assertionFileName, fileData, publishOptions).join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof ResponseBodyException).isTrue();
        assertThat(((throwable.getCause()).getMessage())).isEqualTo("Exception parsing response body content.");
    }

    @Test
    void publish_fileExtensionIsNotJson_throwsClientRequestException() {
        // given
        String assertionFileName = "assertion-example.xml";

        // when
        ClientRequestException throwable = catchThrowableOfType(() -> publishService.publish(assertionFileName, fileData, publishOptions).join(), ClientRequestException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo(format("File extension not supported: %s", "xml"));
    }

    @Test
    void publish_fileDataIsNotValidJson_throwsClientRequestException() {
        // given
        String invalidJsonString = "This isn't valid JSON";

        // when
        ClientRequestException throwable = catchThrowableOfType(() -> publishService.publish(assertionFileName, invalidJsonString.getBytes(StandardCharsets.UTF_8), publishOptions).join(), ClientRequestException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Publish data is not valid JSON");
    }

    @Test
    void publish_publishOptionsAreNull_throwsClientRequestException() {
        // given

        // when
        ClientRequestException throwable = catchThrowableOfType(() -> publishService.publish(assertionFileName, fileData, null).join(), ClientRequestException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Publish options cannot be null");
    }

    @Test
    void getPublishResult_responseIsSuccessful_returnsPublishResultJsonNode() throws InterruptedException, IOException {
        // given
        String handlerId = "ffd8a00e-bf22-4432-8d88-804f4f9baa27";

        byte[] examplePublishResult = getFileData("publish-response-body.json");
        mockWebServer.enqueue(new MockResponse().setBody(new String(examplePublishResult)).setResponseCode(200));

        // when
        CompletableFuture<JsonNode> publishResultCompletableFuture = publishService.getPublishResult(handlerId);
        JsonNode publishResult = publishResultCompletableFuture.join();

        // then
        assertThat(publishResult).isNotNull();
        assertThat(publishResult.get("data").get("id").textValue()).isEqualTo("797e375dd9c38b05f96803acf5666538139310346aeb8f162abe4e0d4d0dff99");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/publish/result/" + handlerId);
    }
}