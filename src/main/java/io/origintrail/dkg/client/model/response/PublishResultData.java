package io.origintrail.dkg.client.model.response;

import lombok.Data;

@Data
public class PublishResultData {
    private Blockchain blockchain;
    private String id;
    private Metadata metadata;
    private String metadataHash;
    private String rootHash;
    private String signature;
}
