package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class ProofsResult {
    private String status;
    private List<ProofsResultData> data;
}
