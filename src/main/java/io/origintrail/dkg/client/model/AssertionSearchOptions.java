package io.origintrail.dkg.client.model;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Builder
public class AssertionSearchOptions {

    private final String query;
    private final Boolean load;

    public Map<String, String> getQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();

        if (StringUtils.isNoneBlank(query)) {
            queryParams.put("query", query);
        }
        if (load != null) {
            queryParams.put("load", load.toString());
        }

        return queryParams;
    }
}
