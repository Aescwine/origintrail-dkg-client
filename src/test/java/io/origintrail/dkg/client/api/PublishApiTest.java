package io.origintrail.dkg.client.api;

import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

class PublishApiTest {
    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8700;

//    private MockWebServer mockWebServer;
//
//    private PublishApi publishApi;
//
//    @BeforeEach
//    void init() {
//        mockWebServer = new MockWebServer();
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//        HttpUrlOptions httpUrlOptions = new HttpUrlOptions(mockWebServer.getHostName(), mockWebServer.getPort());
//
////        publishApi = new PublishApi(okHttpClient, httpUrlOptions);
//    }

    @AfterEach
    void tearDown() throws IOException {
//        mockWebServer.shutdown();
    }

//    @Test
//    void publishApi_test() throws IOException, InterruptedException {
//        // given
//        String expectedHandlerId =  UUID.randomUUID().toString();
//
//        mockWebServer.enqueue(new MockResponse().setBody("{\"handler_id\": \"" + expectedHandlerId +"\"}"));
//
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("assertion-example.json").getFile());
//        byte[] fileData = FileUtils.readFileToByteArray(file);
//
//        PublishOptions publishOptions = PublishOptions.builder(file.getName(), fileData, "[\"Dean Test\"]").build();
//
//        // when
//        HandlerId handlerId = publishApi.publish(publishOptions);
//
//        // then
//        assertThat(handlerId.getHandlerId()).isEqualTo(expectedHandlerId);
//
//        RecordedRequest request = mockWebServer.takeRequest();
//        assertThat(request.getRequestLine()).isEqualTo("POST /publish HTTP/1.1");
//        assertThat(request.getBody()).isNotNull();
//        assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data");
//    }
}