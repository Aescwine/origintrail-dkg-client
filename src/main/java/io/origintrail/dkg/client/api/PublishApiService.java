package io.origintrail.dkg.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.origintrail.dkg.client.exception.ClientRequestException;
import io.origintrail.dkg.client.exception.HttpResponseException;
import io.origintrail.dkg.client.exception.UnexpectedException;
import io.origintrail.dkg.client.http.HttpMediaType;
import io.origintrail.dkg.client.http.MultiPartBody;
import io.origintrail.dkg.client.model.HandlerId;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import io.origintrail.dkg.client.model.MultiPartData;
import io.origintrail.dkg.client.model.PublishOptions;
import io.origintrail.dkg.client.util.JsonUtil;
import io.origintrail.dkg.client.util.UriUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

class PublishApiService extends ApiRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishApiService.class);

    private static final String PUBLISH_PATH = "publish";
    private static final String PUBLISH_RESULT_PATH = "publish/result";

    public PublishApiService(HttpClient httpClient, HttpUrlOptions httpUrlOptions) {
        super(httpClient, httpUrlOptions, LOGGER);
    }

    public CompletableFuture<HandlerId> publish(String fileName, byte[] fileData, PublishOptions publishOptions)
            throws ClientRequestException, HttpResponseException, UnexpectedException {

        validateRequest(fileName, fileData, publishOptions);

        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
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

        HttpRequest request = createMultiPartFormRequest(uri, bodyPublisher);

        return sendAsyncRequest(request, HandlerId.class);
    }

    private void validateRequest(String fileName, byte[] fileData, PublishOptions publishOptions) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (!fileExtension.equals("json")) {
            throw new ClientRequestException(format("File extension not supported: %s", fileExtension));
        }

        if (!JsonUtil.isJsonValid(fileData)) {
            throw new ClientRequestException("Publish data is not valid JSON");
        }

        if (publishOptions == null) {
            throw new ClientRequestException("Publish options cannot be null");
        }
    }

    public CompletableFuture<JsonNode> getPublishResult(String handlerId) throws HttpResponseException, UnexpectedException {
        URI uri = UriUtil.builder().httpUrlOptions(getHttpUrlOptions())
                .pathSegments(List.of(PUBLISH_RESULT_PATH, handlerId))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return sendAsyncRequest(request, JsonNode.class);
    }
}
