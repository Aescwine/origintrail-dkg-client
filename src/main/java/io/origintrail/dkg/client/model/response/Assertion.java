package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class Assertion {
    private JsonNode data;
    private String id;
    private Metadata metadata;
    private String metadataHash;
    private String signature;
    private String rootHash;
    private Blockchain blockchain;
}
