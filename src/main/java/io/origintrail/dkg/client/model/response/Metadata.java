package io.origintrail.dkg.client.model.response;

import lombok.Data;

@Data
public class Metadata {
    private String type;
    private String timestamp;
    private String issuer;
    private Boolean visibility;
    private String dataHash;
}
