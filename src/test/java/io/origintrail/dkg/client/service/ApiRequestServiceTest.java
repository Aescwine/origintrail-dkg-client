package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ApiRequestServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestService.class);

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

        apiRequestService = new ApiRequestService(httpClient, httpUrlOptions, LOGGER);
    }

    @Test
    void sendAsyncRequest_unsuccessfulResponseStatus_throwsHttpResponseException()  {
        // given
        String responseBody = "{ \"error\" : \"Not found\"}";
        mockWebServer.enqueue(new MockResponse().setBody(responseBody).setResponseCode(404));

        // when
        CompletionException throwable = catchThrowableOfType(() -> apiRequestService.sendAsyncRequest(httpRequest).join(), CompletionException.class);

        // then
        assertThat(throwable.getCause() instanceof HttpResponseException).isTrue();
        assertThat(((HttpResponseException)throwable.getCause()).getStatusCode()).isEqualTo(404);
        assertThat(((HttpResponseException)throwable.getCause()).getReasonPhrase()).isEqualTo(responseBody);
    }

    @Test
    void sendAsyncRequest_requestTimedOut_throwsUnexpectedException()  {
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