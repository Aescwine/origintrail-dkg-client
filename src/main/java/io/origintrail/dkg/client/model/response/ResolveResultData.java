package io.origintrail.dkg.client.model.response;

import lombok.Data;

@Data
public class ResolveResultData {
    private String type;
    private String id;
    private Assertion assertion;
    private DataResult result;
}
