package io.origintrail.dkg.client.model.response;

import lombok.Data;

@Data
public class Proof {
    private String triple;
    private String tripleHash;
    private String proof;
}
