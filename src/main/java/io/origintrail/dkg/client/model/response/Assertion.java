package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class Assertion {
    private Metadata metadata;
    private Blockchain blockchain;
    private String id;
    private String signature;
    private JsonNode data;
}
