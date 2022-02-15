package io.origintrail.dkg.client.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Metadata {
    private String dataHash;
    private String issuer;
    @JsonProperty("UALs")
    private List<String> UALs;
    private List<String> keywords;
    private String type;
    private LocalDateTime timestamp;
    private String visibility;
    private LocalDateTime latestState;
}
