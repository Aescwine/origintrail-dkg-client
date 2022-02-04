package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class ResolveResultData {

    private Metadata metadata;
    private Blockchain blockchain;
    private List<String> assets;
    private List<String> keywords;
    private String signature;
    private String rootHash;
    private String id;
    private JsonNode data;
}
