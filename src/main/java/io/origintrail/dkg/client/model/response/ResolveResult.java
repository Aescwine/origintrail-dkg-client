package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResolveResult {
    private String status;
    private List<Map<String, ResolveResultData>> data;
}
