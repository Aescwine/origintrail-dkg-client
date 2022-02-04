package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the node information response from the DKG GET /info API endpoint.
 */
@Getter
@Setter
@ToString
public class NodeInfo {

    private String version;
    @JsonProperty("auto_update")
    private boolean autoUpdate;
    private boolean telemetry;
}
