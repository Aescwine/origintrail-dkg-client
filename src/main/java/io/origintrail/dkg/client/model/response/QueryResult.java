package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult {
    private String status;
    private List<String> data;
}
