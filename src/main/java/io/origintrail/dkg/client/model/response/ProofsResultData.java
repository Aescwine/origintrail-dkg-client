package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class ProofsResultData {
    private String assertionId;
    private List<Proof> proofs;
}
