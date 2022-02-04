package io.origintrail.dkg.client.service;

import io.origintrail.dkg.client.exception.RequestValidationException;
import io.origintrail.dkg.client.http.HttpMediaType;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.http.MultiPartData;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.model.response.HandlerId;
import io.origintrail.dkg.client.model.response.PublishResult;
import io.origintrail.dkg.client.util.JsonUtil;
import io.origintrail.dkg.client.util.UriUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.lang.String.format;

public class PublishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishService.class);

    private static final String PUBLISH_PATH = "publish";
    private static final String PUBLISH_RESULT_PATH = "publish/result";

    private final ApiRequestService apiRequestService;

    public PublishService(ApiRequestService apiRequestService) {
        this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<HandlerId> publish(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {

        validateRequest(fileName, fileData, publishOptions);

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(PUBLISH_PATH)
                .build();

        MultiPartData multiPartData = new MultiPartData(HttpMediaType.APPLICATION_JSON_LD.value(), fileData);
        MultiPartBody.MultiPartBodyBuilder bodyPublisher = MultiPartBody
                .builder()
                .addFilePart("file", fileName, multiPartData)
                .addPart("assets", publishOptions.getAssets())
                .addPart("visibility", publishOptions.isVisibility() ? "true" : "false");

        if (StringUtils.isNotBlank(publishOptions.getKeywords())) {
            bodyPublisher.addPart("keywords", publishOptions.getKeywords());
        }

        HttpRequest request = apiRequestService.createMultiPartFormRequest(uri, bodyPublisher);
        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private void validateRequest(String fileName, byte[] fileData, PublishOptions publishOptions) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (!fileExtension.equals("json")) {
            LOGGER.error(String.format("File extension not supported: %s", fileExtension));
            throw new RequestValidationException(format("File extension not supported: %s", fileExtension));
        }

        if (!JsonUtil.isJsonValid(fileData)) {
            LOGGER.error("Publish data is not valid JSON");
            throw new RequestValidationException("Publish data is not valid JSON");
        }

        if (publishOptions == null) {
            LOGGER.error("Publish options cannot be null");
            throw new RequestValidationException("Publish options cannot be null");
        }
    }

    public CompletableFuture<PublishResult> getPublishResult(String handlerId) throws CompletionException {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(PUBLISH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);
        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, PublishResult.class));
    }
}
