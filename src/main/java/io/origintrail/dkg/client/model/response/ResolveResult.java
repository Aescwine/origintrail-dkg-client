package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class ResolveResult {
    private String status;
    private List<ResolveResultData> data;
}
