package io.origintrail.dkg.client.model.response;

import lombok.Data;

@Data
public class PublishResult {
    private String status;
    private PublishResultData data;
}
