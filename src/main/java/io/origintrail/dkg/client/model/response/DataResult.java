package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class DataResult {
    private Metadata metadata;
    private JsonNode data;
}
