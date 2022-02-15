package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class Proof {
    private String triple;
    private String tripleHash;
    private List<ProofData> proof;
}
