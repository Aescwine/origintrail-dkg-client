package io.origintrail.dkg.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NodeInfo {

    private String version;
    @JsonProperty("auto_update")
    private boolean autoUpdate;
    private boolean telemetry;
}
