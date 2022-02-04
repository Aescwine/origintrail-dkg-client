package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.http.HttpMediaType;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.util.UriUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ApiRequestServiceTest {

    private ApiRequestService apiRequestService;
    private MockWebServer mockWebServer;
    private HttpRequest httpRequest;

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();

        httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/path"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(mockWebServer.getHostName(), mockWebServer.getPort(), "http");
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        apiRequestService = new ApiRequestService(httpClient, httpUrlOptions);
    }

    @Test
    void createHttpGETRequest_createsHttpRequest_methodIsGET() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpGETRequest = apiRequestService.createHttpGETRequest(uri);

        // then
        assertThat(httpGETRequest.method()).isEqualTo("GET");
    }

    @Test
    void createHttpGETRequest_createsHttpRequest_timeoutIs10Seconds() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpGETRequest = apiRequestService.createHttpGETRequest(uri);

        // then
        assertThat(httpGETRequest.timeout()).get().isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void createHttpGETRequest_createsHttpRequest_uriIsSet() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpGETRequest = apiRequestService.createHttpGETRequest(uri);

        // then
        assertThat(httpGETRequest.uri()).isEqualTo(uri);
    }

    @Test
    void createHttpPOSTRequest_createsHttpRequest_methodIsPOST() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpPOSTRequest = apiRequestService.createHttpPOSTRequest(uri, "test body");

        // then
        assertThat(httpPOSTRequest.method()).isEqualTo("POST");
    }

    @Test
    void createHttpPOSTRequest_createsHttpRequest_timeoutIs10Seconds() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpPOSTRequest = apiRequestService.createHttpPOSTRequest(uri, "test body");

        // then
        assertThat(httpPOSTRequest.timeout()).get().isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void createHttpPOSTRequest_createsHttpRequest_uriIsSet() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpPOSTRequest = apiRequestService.createHttpPOSTRequest(uri, "test body");

        // then
        assertThat(httpPOSTRequest.uri()).isEqualTo(uri);
    }

    @Test
    void createHttpPOSTRequest_createsHttpRequest_hasContentTypeHeader() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpGETRequest = apiRequestService.createHttpPOSTRequest(uri, "test body");

        // then
        assertThat(httpGETRequest.headers().map().get("Content-Type")).containsOnly(HttpMediaType.APPLICATION_JSON.value());
    }

    @Test
    void createMultiPartFormRequest_createsHttpRequest_methodIsPOST() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpMultiPartFormRequest = apiRequestService.createMultiPartFormRequest(uri, MultiPartBody.builder().addPart("key", "value"));

        // then
        assertThat(httpMultiPartFormRequest.method()).isEqualTo("POST");
    }

    @Test
    void createMultiPartFormRequest_createsHttpRequest_timeoutIs10Seconds() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpMultiPartFormRequest = apiRequestService.createMultiPartFormRequest(uri, MultiPartBody.builder().addPart("key", "value"));

        // then
        assertThat(httpMultiPartFormRequest.timeout()).get().isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void createMultiPartFormRequest_createsHttpRequest_uriIsSet() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpMultiPartFormRequest = apiRequestService.createMultiPartFormRequest(uri, MultiPartBody.builder().addPart("key", "value"));

        // then
        assertThat(httpMultiPartFormRequest.uri()).isEqualTo(uri);
    }

    @Test
    void createMultiPartFormRequest_createsHttpRequest_hasContentTypeHeader() {
        // given
        URI uri = UriUtil.builder()
                .httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path("/path")
                .build();

        // when
        HttpRequest httpMultiPartFormRequest = apiRequestService.createMultiPartFormRequest(uri, MultiPartBody.builder().addPart("key", "value"));

        // then
        assertThat(httpMultiPartFormRequest.headers().map().get("Content-Type").get(0)).startsWith(HttpMediaType.MULTIPART_FORM_DATA.value() + "; boundary=");
    }

    @Test
    void sendAsyncRequest_unsuccessfulResponseStatus_throwsHttpResponseException() {
        // given
        String responseBody = "{ \"error\" : \"Not found\"}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(404));

        // when
        CompletionException throwable = catchThrowableOfType(() -> apiRequestService.sendAsyncRequest(httpRequest).join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof HttpResponseException).isTrue();
        assertThat(((HttpResponseException) throwable.getCause()).getStatusCode()).isEqualTo(404);
        assertThat(((HttpResponseException) throwable.getCause()).getReasonPhrase()).isEqualTo(responseBody);
    }

    @Test
    void sendAsyncRequest_requestTimedOut_throwsUnexpectedException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

        // when
        CompletionException throwable = catchThrowableOfType(() -> apiRequestService.sendAsyncRequest(httpRequest).join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof UnexpectedException).isTrue();
        assertThat(throwable.getCause().getMessage()).isEqualTo("java.net.http.HttpTimeoutException: request timed out");
    }
}