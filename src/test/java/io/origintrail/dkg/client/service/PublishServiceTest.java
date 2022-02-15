package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.model.PublishOptions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PublishServiceTest {

    private final String assertionFileName = "assertion-example.json";
    private final byte[] fileData = getFileData();
    private final PublishOptions publishOptions = PublishOptions
            .builder(Collections.singletonList("[\"test_keyword\"]"))
            .build();

    @Mock
    private ApiRequestService apiRequestService;

    private PublishService publishService;

    PublishServiceTest() throws IOException {
    }

    @BeforeEach
    void init() {
        publishService = new PublishService(apiRequestService);
    }

    private byte[] getFileData() throws IOException {
        ClassLoader classLoader = PublishServiceTest.class.getClassLoader();
        URL assertionFileUrl = classLoader.getResource(assertionFileName);
        if (assertionFileUrl == null) {
            throw new IllegalArgumentException("File not found: " + assertionFileName);
        }
        File file = new File(assertionFileUrl.getFile());
        return FileUtils.readFileToByteArray(file);
    }

    @Test
    void publish_fileExtensionIsNotJson_throwsClientRequestException() {
        // given
        String assertionFileName = "assertion-example.xml";

        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> publishService.publish(assertionFileName, fileData, publishOptions).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo(format("File extension not supported: %s", "xml"));
        then(apiRequestService).shouldHaveNoInteractions();
    }

    @Test
    void publish_fileDataIsNotValidJson_throwsClientRequestException() {
        // given
        String invalidJsonString = "This isn't valid JSON";

        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> publishService.publish(assertionFileName, invalidJsonString.getBytes(StandardCharsets.UTF_8), publishOptions).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Publish data is not valid JSON");
        then(apiRequestService).shouldHaveNoInteractions();
    }

    @Test
    void publish_publishOptionsAreNull_throwsClientRequestException() {
        // when
        RequestValidationException throwable = catchThrowableOfType(
                () -> publishService.publish(assertionFileName, fileData, null).join(), RequestValidationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Publish options cannot be null");
        then(apiRequestService).shouldHaveNoInteractions();
    }
}