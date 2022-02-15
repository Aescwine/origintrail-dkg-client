package io.origintrail.dkg.client.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static io.origintrail.dkg.client.service.ApiRequestService.OBJECT_MAPPER;
import static java.lang.String.format;

public class PublishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishService.class);

    private static final String PUBLISH_PATH = "publish";
    private static final String PUBLISH_RESULT_PATH = "publish/result";
    private static final String PROVISION_PATH = "provision";
    private static final String PROVISION_RESULT_PATH = "provision/result";
    private static final String UPDATE_PATH = "update";
    private static final String UPDATE_RESULT_PATH = "update/result";

    private final ApiRequestService apiRequestService;

    public PublishService(ApiRequestService apiRequestService) {
        this.apiRequestService = apiRequestService;
    }

    public CompletableFuture<HandlerId> publish(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {

        return publishData(fileName, fileData, publishOptions, PUBLISH_PATH);
    }

    public CompletableFuture<PublishResult> getPublishResult(String handlerId) throws CompletionException {
        return getPublishResult(handlerId, PUBLISH_RESULT_PATH);
    }

    public CompletableFuture<HandlerId> provision(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {

        return publishData(fileName, fileData, publishOptions, PROVISION_PATH);
    }

    public CompletableFuture<PublishResult> getProvisionResult(String handlerId) throws CompletionException {
        return getPublishResult(handlerId, PROVISION_RESULT_PATH);
    }

    public CompletableFuture<HandlerId> update(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws CompletionException, RequestValidationException {

        return publishData(fileName, fileData, publishOptions, UPDATE_PATH);
    }

    public CompletableFuture<PublishResult> getUpdateResult(String handlerId) throws CompletionException {
        return getPublishResult(handlerId, UPDATE_RESULT_PATH);
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

    private CompletableFuture<HandlerId> publishData(String fileName, byte[] fileData, PublishOptions publishOptions, String path) {
        validateRequest(fileName, fileData, publishOptions);

        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .path(path)
                .build();

        ArrayNode keywordArray = OBJECT_MAPPER.createArrayNode();
        publishOptions.getKeywords().forEach(keywordArray::add);

        MultiPartData multiPartData = new MultiPartData(HttpMediaType.APPLICATION_JSON_LD.value(), fileData);
        MultiPartBody.MultiPartBodyBuilder bodyPublisher = MultiPartBody
                .builder()
                .addFilePart("file", fileName, multiPartData)
                .addPart("keywords", keywordArray.toString())
                .addPart("visibility", publishOptions.getVisibility().getValue())
                .addPart("ual", publishOptions.getUal());

        HttpRequest request = apiRequestService.createMultiPartFormRequest(uri, bodyPublisher);
        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, HandlerId.class));
    }

    private CompletableFuture<PublishResult> getPublishResult(String handlerId, String path) {
        URI uri = UriUtil.builder().httpUrlOptions(apiRequestService.getHttpUrlOptions())
                .pathSegments(List.of(path, handlerId))
                .build();

        HttpRequest request = apiRequestService.createHttpGETRequest(uri);
        return apiRequestService.sendAsyncRequest(request)
                .thenApply(body -> apiRequestService.transformBody(body, PublishResult.class));
    }
}
